# WissenUp Frontend Implementation Guide

## Overview

This document outlines the modernization of the WissenUp React frontend from a monolithic, single-file architecture to a scalable, production-grade SaaS application.

### Key Principles

1. **Security First**: JWT claims from backend, no client-side role/org checking
2. **Incremental Migration**: Keep existing functionality working during refactoring
3. **Type Safety**: Full TypeScript coverage
4. **Testability**: All components and features testable
5. **Performance**: Route-level code splitting, lazy loading
6. **Responsive**: Mobile-first design without visual overhaul

---

## Phase 1: Dependencies & Infrastructure (Week 1-2)

### Step 1.1: Update package.json

```bash
cd WissenUp-ui
# Backup current package.json
cp package.json package.json.backup

# Replace with new package.json
cp package.json.new package.json

# Install dependencies
npm install

# Add additional required dependencies
npm install jwt-decode
```

**Added Dependencies:**
- `@tanstack/react-query` - Server state management
- `zustand` - Client state management
- `zod` - Runtime schema validation
- `react-hook-form` - Form handling
- `axios` - HTTP client (better than fetch)
- `jwt-decode` - JWT parsing (client-side, no validation)

**Added Dev Dependencies:**
- `typescript` - Type safety
- `vitest` - Unit testing
- `@testing-library/react` - Component testing
- `@playwright/test` - E2E testing

### Step 1.2: Configure TypeScript

The provided `tsconfig.json` is already configured with:
- `strict: true` - Enable strict type checking
- Path aliases (@components, @services, @stores, etc.)
- JSX support for React 19
- ES2020 target

No action needed - already created.

### Step 1.3: Create Vite Environment File

Create `.env.example`:

```env
# Backend API
VITE_API_BASE_URL=http://localhost:3000/api/v1

# Optional: Individual service URLs (deprecated, use single base URL)
VITE_IDENTITY_SERVICE_URL=http://localhost:3000/api/v1
VITE_PLATFORM_SERVICE_URL=http://localhost:3000/api/v1
VITE_ORGANIZATION_SERVICE_URL=http://localhost:3000/api/v1

# Feature flags
VITE_ENABLE_DEMO_MODE=false
VITE_LOG_LEVEL=info
```

Create `.env.local` (git-ignored):

```env
VITE_API_BASE_URL=http://localhost:3000/api/v1
VITE_LOG_LEVEL=debug
```

### Step 1.4: Update vite.config.js

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': '/src',
      '@components': '/src/components',
      '@features': '/src/features',
      '@hooks': '/src/hooks',
      '@services': '/src/services',
      '@stores': '/src/stores',
      '@types': '/src/types',
      '@utils': '/src/utils',
      '@layouts': '/src/layouts',
      '@pages': '/src/pages',
      '@styles': '/src/styles',
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
```

### Step 1.5: Verify Build

```bash
npm run build
# Should complete without TypeScript errors
```

---

## Phase 2: Core Architecture Files (Week 2)

### Step 2.1: API Client

Already created: `src/services/api/client.ts`

**Key Features:**
- Single base URL from environment
- Automatic JWT injection in Authorization header
- Request ID correlation for debugging
- Retry logic for failed requests
- 401 handling (token expired)
- Structured error handling

**Usage:**

```typescript
import apiClient from '@services/api/client';

// GET request
const response = await apiClient.get<UserData>('/users/me');

// POST request
const response = await apiClient.post<LoginResponse>('/auth/login', {
  email: 'user@example.com',
  password: 'password123'
});

// Set token
apiClient.setToken(jwtToken);
```

### Step 2.2: Authentication API Service

Already created: `src/features/auth/services/authApi.ts`

**Exports:**
- `authApi.login(email, password)` - Step 1 of auth flow
- `authApi.verifyOtp(email, otp)` - Step 2 of auth flow
- `authApi.resendOtp(email)` - Resend OTP
- `authApi.setToken(token)` - Store JWT
- `authApi.clearToken()` - Clear JWT

### Step 2.3: Authentication Store

Already created: `src/stores/authStore.ts`

**Key Features:**
- Stores JWT token in sessionStorage (cleared on browser close)
- Extracts and validates JWT claims (userId, organizationId, roleId)
- No reliance on localStorage for auth data
- Methods: `setToken`, `clearAuth`, `restoreSession`, `isTokenExpired`
- Getters: `getOrganizationId`, `getUserId`, `getRoleId`

**Usage:**

```typescript
import { useAuthStore } from '@stores/authStore';

function MyComponent() {
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const user = useAuthStore(state => state.user);
  const clearAuth = useAuthStore(state => state.clearAuth);
  
  return (
    <div>
      {isAuthenticated && <p>{user?.email}</p>}
    </div>
  );
}
```

### Step 2.4: UI Store

Already created: `src/stores/uiStore.ts`

**Features:**
- Theme management (light/dark)
- Sidebar state
- Confirm modal state
- Toast notifications
- Persisted to localStorage

**Convenience Hooks:**
- `useTheme()` - Theme and colors
- `useSidebar()` - Sidebar state
- `useToast()` - Toast notifications
- `useConfirmModal()` - Confirmation dialog

### Step 2.5: Validation Schemas

Already created: `src/features/auth/schemas/authSchemas.ts`

**Exports:**
- `loginSchema` - Email + password
- `otpSchema` - 6-digit OTP
- TypeScript types: `LoginFormData`, `OtpFormData`

### Step 2.6: React Query Hooks

Already created: `src/features/auth/hooks/useAuthMutations.ts`

**Exports:**
- `useLoginMutation()` - Trigger login
- `useOtpVerifyMutation()` - Trigger OTP verification
- `useResendOtpMutation()` - Trigger resend OTP
- `useLogout()` - Logout function

---

## Phase 3: Update Main App Structure (Week 2-3)

### Step 3.1: Create QueryClientProvider

Create `src/app/queryClient.ts`:

```typescript
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      gcTime: 1000 * 60 * 10, // 10 minutes (formerly cacheTime)
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 1,
    },
  },
});
```

### Step 3.2: Update App.tsx

Migrate `src/App.jsx` to `src/App.tsx`:

```typescript
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@app/queryClient';
import AppRoutes from '@routes/index';
import { useEffect } from 'react';
import { useAuthStore } from '@stores/authStore';

function App() {
  const restoreSession = useAuthStore(state => state.restoreSession);

  // Restore session on app load
  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  return (
    <QueryClientProvider client={queryClient}>
      <AppRoutes />
    </QueryClientProvider>
  );
}

export default App;
```

### Step 3.3: Create Protected Route Component

Create `src/components/ProtectedRoute.tsx`:

```typescript
import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@stores/authStore';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRoles?: number[]; // Role IDs that can access this route
}

export function ProtectedRoute({ 
  children, 
  requiredRoles 
}: ProtectedRouteProps) {
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const roleId = useAuthStore(state => state.user?.roleId);
  const isTokenExpired = useAuthStore(state => state.isTokenExpired);

  // Check authentication
  if (!isAuthenticated || isTokenExpired()) {
    return <Navigate to="/login" replace />;
  }

  // Check role if required
  if (requiredRoles && roleId && !requiredRoles.includes(roleId)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
}
```

### Step 3.4: Update Routes

Create `src/routes/index.tsx` (replaces giant AppRoutes.jsx):

```typescript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Suspense, lazy } from 'react';
import { ProtectedRoute } from '@components/ProtectedRoute';

// Public pages
const LoginPage = lazy(() => import('@pages/auth/Login'));
const VerifyOtpPage = lazy(() => import('@pages/auth/VerifyOtp'));
const UnauthorizedPage = lazy(() => import('@pages/Unauthorized'));

// Organization routes
const OrgDashboard = lazy(() => import('@pages/org/Dashboard'));
// ... other org routes

// Superadmin routes  
const SuperAdminDashboard = lazy(() => import('@pages/superadmin/Dashboard'));
// ... other superadmin routes

const LoadingFallback = () => <div>Loading...</div>;

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Suspense fallback={<LoadingFallback />}><LoginPage /></Suspense>} />
        <Route path="/verify-otp" element={<Suspense fallback={<LoadingFallback />}><VerifyOtpPage /></Suspense>} />
        <Route path="/unauthorized" element={<Suspense fallback={<LoadingFallback />}><UnauthorizedPage /></Suspense>} />

        {/* Organization routes */}
        <Route path="/org/*" element={<Suspense fallback={<LoadingFallback />}><OrgRoutes /></Suspense>} />

        {/* Superadmin routes */}
        <Route path="/superadmin/*" element={<Suspense fallback={<LoadingFallback />}><SuperAdminRoutes /></Suspense>} />

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

// Feature-specific route modules
function OrgRoutes() {
  return (
    <Routes>
      <Route path="/dashboard" element={<ProtectedRoute><OrgDashboard /></ProtectedRoute>} />
      {/* Add other org routes */}
    </Routes>
  );
}

function SuperAdminRoutes() {
  return (
    <Routes>
      <Route path="/dashboard" element={<ProtectedRoute><SuperAdminDashboard /></ProtectedRoute>} />
      {/* Add other superadmin routes */}
    </Routes>
  );
}
```

---

## Phase 4: Migrate Existing Pages (Week 3-4)

### Step 4.1: Update Login Page

Rename `src/pages/Login.jsx` → `src/pages/auth/Login.tsx`:

```typescript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loginSchema, LoginFormData } from '@features/auth/schemas/authSchemas';
import { useLoginMutation } from '@features/auth/hooks/useAuthMutations';

export default function LoginPage() {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema)
  });
  const { mutate: login, isPending } = useLoginMutation();

  const onSubmit = async (data: LoginFormData) => {
    login(data, {
      onSuccess: () => {
        navigate('/verify-otp', { replace: true });
      },
      onError: (error: any) => {
        // Error handling
      }
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <input
          {...register('email')}
          type="email"
          placeholder="Email"
        />
        {errors.email && <span>{errors.email.message}</span>}
      </div>

      <div>
        <input
          {...register('password')}
          type="password"
          placeholder="Password"
        />
        {errors.password && <span>{errors.password.message}</span>}
      </div>

      <button type="submit" disabled={isPending}>
        {isPending ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
```

### Step 4.2: Update Verify OTP Page

Rename `src/pages/VerifyOtp.jsx` → `src/pages/auth/VerifyOtp.tsx`:

```typescript
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { otpSchema, OtpFormData } from '@features/auth/schemas/authSchemas';
import { useOtpVerifyMutation, useResendOtpMutation } from '@features/auth/hooks/useAuthMutations';

export default function VerifyOtpPage() {
  const navigate = useNavigate();
  const email = sessionStorage.getItem('pendingUserEmail');
  const { register, handleSubmit, formState: { errors } } = useForm<OtpFormData>({
    resolver: zodResolver(otpSchema)
  });
  const { mutate: verifyOtp, isPending } = useOtpVerifyMutation();
  const { mutate: resendOtp } = useResendOtpMutation();

  useEffect(() => {
    if (!email) {
      navigate('/login', { replace: true });
    }
  }, [email, navigate]);

  const onSubmit = async (data: OtpFormData) => {
    if (!email) return;

    verifyOtp({ email, otp: data.otp }, {
      onSuccess: (data) => {
        // Redirect based on role
        const dashboardMap: Record<number, string> = {
          1: '/superadmin/dashboard',
          2: '/org/dashboard',
          3: '/org/teacher/dashboard',
          // ... other role mappings
        };
        navigate(dashboardMap[data.userId] || '/dashboard');
      }
    });
  };

  const handleResend = () => {
    if (email) {
      resendOtp({ email });
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <input
          {...register('otp')}
          type="text"
          placeholder="000000"
          maxLength={6}
        />
        {errors.otp && <span>{errors.otp.message}</span>}
      </div>

      <button type="submit" disabled={isPending}>
        {isPending ? 'Verifying...' : 'Verify OTP'}
      </button>

      <button type="button" onClick={handleResend}>
        Resend OTP
      </button>
    </form>
  );
}
```

### Step 4.3: Keep Existing Styles

- Do NOT delete App.css
- Gradually extract CSS modules as you refactor pages
- Keep visual consistency

---

## Phase 5: Testing Strategy (Week 5-6)

### Step 5.1: Unit Tests

Create `src/__tests__/stores/authStore.test.ts`:

```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from '@stores/authStore';

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: null,
      token: null,
      isAuthenticated: false,
    });
    sessionStorage.clear();
  });

  it('should set token and extract claims', () => {
    const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'; // Mock JWT
    useAuthStore.getState().setToken(token);
    
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.userId).toBeDefined();
  });

  it('should clear auth on logout', () => {
    useAuthStore.getState().clearAuth();
    const state = useAuthStore.getState();
    
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
  });
});
```

### Step 5.2: Component Tests

Create `src/__tests__/components/Button.test.tsx`:

```typescript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { Button } from '@components/Button';

describe('Button', () => {
  it('should render button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('should call onClick handler', async () => {
    const onClick = vi.fn();
    render(<Button onClick={onClick}>Click me</Button>);
    
    await userEvent.click(screen.getByText('Click me'));
    expect(onClick).toHaveBeenCalledOnce();
  });

  it('should be disabled when disabled prop is true', () => {
    render(<Button disabled>Click me</Button>);
    expect(screen.getByText('Click me')).toBeDisabled();
  });
});
```

### Step 5.3: E2E Tests (Playwright)

Create `e2e/auth.spec.ts`:

```typescript
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:5173/login');
  });

  test('should login and verify OTP', async ({ page }) => {
    // Enter email
    await page.fill('input[type="email"]', 'test@example.com');
    
    // Enter password
    await page.fill('input[type="password"]', 'password123');
    
    // Submit login
    await page.click('button:has-text("Login")');
    
    // Should navigate to OTP page
    await expect(page).toHaveURL(/\/verify-otp/);
    
    // Enter OTP (mock)
    await page.fill('input[placeholder="000000"]', '123456');
    
    // Submit OTP
    await page.click('button:has-text("Verify OTP")');
    
    // Should navigate to dashboard
    await expect(page).toHaveURL(/\/(org|superadmin)\/dashboard/);
  });
});
```

---

## Migration Checklist

### Week 1
- [ ] Install dependencies
- [ ] Configure TypeScript
- [ ] Update vite.config
- [ ] Create .env.example
- [ ] Verify build

### Week 2
- [ ] Create API client
- [ ] Create auth API service
- [ ] Create auth store
- [ ] Create UI store
- [ ] Create validation schemas
- [ ] Create React Query hooks

### Week 3
- [ ] Create QueryClientProvider
- [ ] Update App.tsx
- [ ] Create ProtectedRoute
- [ ] Split AppRoutes
- [ ] Create route modules
- [ ] Test routes work

### Week 4
- [ ] Migrate Login page
- [ ] Migrate VerifyOtp page
- [ ] Migrate all org pages
- [ ] Migrate all superadmin pages
- [ ] Migrate all shared pages
- [ ] Fix all styling issues

### Week 5
- [ ] Create test setup
- [ ] Write store tests
- [ ] Write component tests
- [ ] Write form tests
- [ ] Aim for >70% coverage

### Week 6
- [ ] Write E2E tests (Playwright)
- [ ] Test on mobile viewports
- [ ] Performance audit
- [ ] Accessibility audit
- [ ] Fix issues

---

## Troubleshooting

### Issue: localStorage still used for auth

**Fix**: Search codebase for:
```bash
grep -r "localStorage.getItem.*token\|localStorage.getItem.*role\|localStorage.getItem.*organizationId" src/
```

Replace with Zustand store access:
```typescript
const { user, token } = useAuthStore();
const organizationId = user?.organizationId;
```

### Issue: API client 401 but no redirect

**Fix**: Listen for unauthorized event:
```typescript
useEffect(() => {
  const handleUnauthorized = () => {
    navigate('/login', { replace: true });
  };
  
  window.addEventListener('auth:unauthorized', handleUnauthorized);
  return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
}, [navigate]);
```

### Issue: JWT token missing in requests

**Fix**: Ensure setToken was called:
```typescript
const { mutate: verifyOtp } = useOtpVerifyMutation();
// setToken is called automatically on success

// Or manually:
apiClient.setToken(token);
```

### Issue: CORS errors

**Fix**: Check vite.config proxy is correct:
```javascript
server: {
  proxy: {
    '/api': {
      target: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
      changeOrigin: true,
    }
  }
}
```

---

## Next Steps

1. **Implement Week 1-2 tasks** in order
2. **Run `npm run build`** to verify TypeScript
3. **Test login flow** works end-to-end
4. **Gradually migrate remaining pages**
5. **Add tests as you migrate**
6. **Performance & accessibility audit**

The codebase is now ready for incremental modernization!
