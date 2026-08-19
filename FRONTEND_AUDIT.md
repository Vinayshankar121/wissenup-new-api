# WissenUp Frontend Audit & Migration Plan

## Current State Analysis

### Project Setup
- **Build Tool**: Vite 8.1.0 ✓ (good - modern, fast)
- **React**: 19.2.7 (latest - good)
- **Routing**: React Router 7.18.0 ✓
- **State Management**: Context API + localStorage/sessionStorage (problematic)
- **Styling**: Single monolithic App.css (14,134 lines)
- **Testing**: None configured
- **TypeScript**: Empty tsconfig.json (not in use)
- **Forms**: Manual useState (no form library)
- **Data Fetching**: Custom fetch wrapper in authService
- **UI Library**: None (custom components)

### Architecture Issues - Critical

1. **Single Route File** - 140 lines, hard to maintain
2. **Hardcoded localStorage** - untrusted for role/organizationId
3. **Multiple Base URLs** - no centralized API client
4. **No Interceptors** - JWT not automatically injected
5. **41 Page Files** - flat structure, no feature organization
6. **14,134 Lines CSS** - impossible to maintain
7. **No Form Validation** - no schema validation
8. **Zero Tests** - Vitest not configured
9. **Client-Side Permission Checks** - localStorage read, not JWT
10. **Organization Switching Risk** - user could modify organizationId

### Security Issues

- JWT stored in localStorage (XSS vulnerability)
- Role checked against localStorage, not backend
- organizationId from localStorage, not JWT claims
- No refresh token mechanism
- No session expiry handling

### Existing Functionality to Preserve

- Two-step login (email + OTP) ✓
- Role-based dashboards (Super Admin, Org Admin, Teacher, Parent, Cashier) ✓
- School management (settings, reports) ✓
- Student management (admission, search, profiles) ✓
- Attendance tracking ✓
- Fees/payment management ✓
- Exam management (timetable, marks, report cards) ✓
- Timetable/calendar ✓
- Staff management ✓
- Notices system ✓
- Academic year selector ✓
- Toast notifications ✓
- Confirmation modals ✓
- Theme support (light/dark) ✓

---

## Target Architecture

### Folder Structure
```
src/
├── app/
│   └── App.tsx
├── routes/
│   ├── index.tsx
│   ├── auth.tsx
│   ├── superadmin.tsx
│   ├── organization.tsx
│   └── shared.tsx
├── layouts/
│   ├── AuthLayout.tsx
│   ├── OrgLayout.tsx
│   └── SuperAdminLayout.tsx
├── pages/
│   ├── auth/
│   │   ├── Login.tsx
│   │   └── VerifyOtp.tsx
│   ├── superadmin/
│   │   ├── Dashboard.tsx
│   │   └── ...
│   └── org/
│       ├── Dashboard.tsx
│       └── ...
├── components/
│   ├── common/
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── Modal.tsx
│   │   ├── Sidebar.tsx
│   │   └── ...
│   ├── layout/
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   └── ...
│   └── features/
│       ├── StudentCard.tsx
│       ├── AttendanceTable.tsx
│       └── ...
├── features/
│   ├── auth/
│   │   ├── hooks/
│   │   │   ├── useLogin.ts
│   │   │   ├── useOtpVerify.ts
│   │   │   └── useAuth.ts
│   │   ├── services/
│   │   │   └── authApi.ts
│   │   ├── schemas/
│   │   │   └── auth.ts
│   │   └── types/
│   │       └── auth.ts
│   ├── students/
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── schemas/
│   │   └── types/
│   └── ...
├── services/
│   ├── api/
│   │   ├── client.ts
│   │   ├── interceptors.ts
│   │   └── errorHandler.ts
│   └── storage.ts
├── hooks/
│   ├── useQuery.ts
│   ├── useMutation.ts
│   ├── useAuth.ts
│   └── ...
├── stores/
│   ├── authStore.ts
│   ├── uiStore.ts
│   └── academicYearStore.ts
├── types/
│   ├── api.ts
│   ├── auth.ts
│   └── entities.ts
├── schemas/
│   ├── auth.ts
│   ├── students.ts
│   └── ...
├── utils/
│   ├── formatters.ts
│   ├── validators.ts
│   └── ...
├── styles/
│   ├── design-tokens.css
│   ├── typography.css
│   ├── layout.css
│   ├── components.css
│   └── pages.css
├── assets/
│   ├── images/
│   ├── icons/
│   └── fonts/
├── main.tsx
└── index.css
```

### API Client Architecture

```typescript
// Single centralized client
const apiClient = createApiClient({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
});

// All requests go through single client
apiClient.post('/auth/login', { email, password })
apiClient.get('/users/me', {}, { token: jwtToken })
apiClient.post('/students', studentData)

// Automatic JWT injection
// Automatic retry on failure
// Centralized error handling
// Correlation IDs for debugging
```

### State Management

- **TanStack Query**: Server state (students, teachers, fees, etc.)
  - Automatic caching
  - Background refetching
  - Pagination support
  
- **Zustand**: Client state only
  - `useAuthStore`: currentUser, organizationId (from JWT), logout
  - `useUIStore`: theme, sidebarOpen, modals
  - `useAcademicYearStore`: selectedYear

### Authentication Flow

```
1. User lands on /login
2. Enters email + password
3. Backend sends OTP
4. User enters OTP code
5. Backend returns JWT (signed, includes userId, organizationId, roleId)
6. App validates JWT signature (local)
7. App extracts claims from JWT (not from localStorage)
8. App stores JWT in memory (or sessionStorage with warning)
9. On page refresh:
   - Check sessionStorage for JWT
   - Validate signature
   - If valid, restore session
   - If invalid/expired, redirect to login
```

### Security Implementation

- JWT claims (organizationId, roleId) extracted from token, not localStorage
- Role-based route protection checks JWT, not storage
- API client injects JWT in Authorization header
- Tenant filtering: backend validates organizationId from JWT
- Session expiry: ProtectedRoute checks JWT.exp
- Refresh token: automatic retry with refresh endpoint

---

## Implementation Roadmap

### Week 1: Setup & Infrastructure
- [ ] Install dependencies (TypeScript, TanStack Query, Zustand, Zod, Testing tools)
- [ ] Configure TypeScript + tsconfig
- [ ] Set up testing infrastructure (Vitest, Testing Library, Playwright)
- [ ] Create centralized API client
- [ ] Add environment variable management

### Week 2: Authentication & State
- [ ] Implement useAuth hook with JWT handling
- [ ] Create Zustand stores (auth, UI, academicYear)
- [ ] Fix authentication to use JWT claims (not localStorage)
- [ ] Implement session restoration on app load
- [ ] Implement token refresh mechanism

### Week 3: Routing & Architecture
- [ ] Split AppRoutes into feature modules
- [ ] Implement permission-based route protection
- [ ] Create layouts (AuthLayout, OrgLayout, SuperAdminLayout)
- [ ] Add lazy loading to routes
- [ ] Implement error boundary

### Week 4: Components & Forms
- [ ] Create reusable component library (/common)
- [ ] Migrate forms to React Hook Form + Zod
- [ ] Extract design tokens from CSS
- [ ] Create design system documentation

### Week 5: Responsiveness
- [ ] Implement mobile-first responsive layouts
- [ ] Create touch-friendly navigation
- [ ] Test on multiple viewports (320px, 768px, 1024px+)
- [ ] Optimize performance

### Week 6: Testing & Polish
- [ ] Add unit tests (>70% coverage)
- [ ] Add integration tests
- [ ] Add Playwright E2E tests
- [ ] Performance optimization
- [ ] Accessibility audit

---

## Tech Stack

### Core
- React 19.2.7
- React Router 7.18.0
- Vite 8.1.0
- TypeScript 5

### Data & State
- TanStack Query 5 (server state)
- Zustand 4 (client state)
- Axios (API client)

### Forms & Validation
- React Hook Form 7
- Zod 3

### Testing
- Vitest
- Testing Library
- Playwright

### UI & Styling
- Lucide React (icons)
- CSS Modules (styling)
- Existing theme system (preserve)

### Utilities
- React Icons (fallback)
- Date utilities (react-datepicker, keep existing)
- Excel utilities (keep existing)

---

## Success Criteria

- [ ] All 41 pages working identically to original
- [ ] TypeScript: 0 type errors
- [ ] Tests: >70% code coverage
- [ ] Performance: Lighthouse >80
- [ ] Mobile: Responsive from 320px+
- [ ] Security: Roles from JWT only, not localStorage
- [ ] Accessibility: WCAG 2.1 AA compliant
- [ ] Build: <3s on development, <2s production
- [ ] API: Single client, no repeated fetch logic
- [ ] CSS: Organized into ~10 modules (from 14KB monolith)

---

## Key Decisions

1. **Keep existing CSS initially** - refactor gradually to avoid regressions
2. **TypeScript optional at file level** - migrate .jsx → .tsx incrementally
3. **TanStack Query for all API calls** - replaces custom service files
4. **Zustand for theme/UI only** - not for API data
5. **Preserve visual identity** - no design overhaul, only UX improvements
6. **Mobile-first responsive** - not desktop-first shrinking
7. **Security-first JWT handling** - backend authoritative, not client
8. **Incremental migration** - keep running version during refactor
