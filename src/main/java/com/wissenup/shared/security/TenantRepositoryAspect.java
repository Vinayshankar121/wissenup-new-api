package com.wissenup.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AOP aspect for automatic tenant filtering on repository calls.
 * Prevents accidental cross-tenant data access by filtering results at the repository level.
 *
 * This is a safety net - the real filtering should happen in repository queries.
 * This aspect catches cases where queries don't include tenant filtering.
 */
@Aspect
@Slf4j
public class TenantRepositoryAspect {

    @Around("execution(* org.springframework.data.repository.Repository+.find*(..))")
    public Object scopeDerivedFinders(ProceedingJoinPoint joinPoint) throws Throwable {
        return scopeResult(joinPoint.proceed());
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.findById(..))")
    public Object scopeFindById(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        return scopeResult(result);
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.findAll(..))")
    public Object scopeFindAll(ProceedingJoinPoint joinPoint) throws Throwable {
        return scopeResult(joinPoint.proceed());
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.deleteById(..))")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object scopeDeleteById(ProceedingJoinPoint joinPoint) throws Throwable {
        if (currentClaims() != null && currentClaims().organizationId() != 0L &&
            joinPoint.getTarget() instanceof CrudRepository repository) {
            Optional<?> entity = repository.findById(joinPoint.getArgs()[0]);
            if (entity.isEmpty() || !isVisible(entity.get())) {
                log.warn("Tenant isolation violation: Attempt to delete entity from different organization");
                throw new AccessDeniedException("Organization access denied");
            }
        }
        return joinPoint.proceed();
    }

    private Object scopeResult(Object result) {
        if (result instanceof Optional<?> optional && optional.isPresent() &&
            !isVisible(optional.get())) {
            log.warn("Tenant isolation violation: Optional contains entity from different organization");
            return Optional.empty();
        }

        if (result instanceof Page<?> page && currentClaims() != null &&
            currentClaims().organizationId() != 0L) {
            List<Object> visible = new ArrayList<>();
            for (Object value : page.getContent()) {
                if (isVisible(value)) {
                    visible.add(value);
                } else {
                    log.warn("Tenant isolation violation: Filtering out cross-tenant entity");
                }
            }
            return new PageImpl<>(visible, page.getPageable(), visible.size());
        }

        if (result instanceof Iterable<?> iterable && currentClaims() != null &&
            currentClaims().organizationId() != 0L) {
            List<Object> visible = new ArrayList<>();
            for (Object value : iterable) {
                if (isVisible(value)) {
                    visible.add(value);
                } else {
                    log.warn("Tenant isolation violation: Filtering out cross-tenant entity");
                }
            }
            return visible;
        }

        return result;
    }

    private boolean isVisible(Object entity) {
        JwtClaims claims = currentClaims();
        if (claims == null || entity == null) {
            return true; // No auth context, allow (will be caught elsewhere)
        }

        // SUPER_ADMIN can see everything
        if (claims.organizationId() == 0L) {
            return true;
        }

        Long organizationId = readOrganizationId(entity);
        return organizationId == null || organizationId.equals(claims.organizationId());
    }

    private JwtClaims currentClaims() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null ?
            null :
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return principal instanceof JwtClaims claims ? claims : null;
    }

    private Long readOrganizationId(Object entity) {
        // Try common accessor method names
        for (String methodName : new String[]{"getOrganizationId", "organizationId", "getOrgId", "orgId"}) {
            try {
                Method method = entity.getClass().getMethod(methodName);
                Object value = method.invoke(entity);
                if (value instanceof Number number) {
                    return number.longValue();
                }
            } catch (ReflectiveOperationException ignored) {
                // Try next method name
            }
        }
        return null;
    }
}
