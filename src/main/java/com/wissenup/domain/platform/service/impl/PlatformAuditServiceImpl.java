package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.platform.dto.PlatformAuditLogDto;
import com.wissenup.domain.platform.entity.PlatformAuditLog;
import com.wissenup.domain.platform.repository.PlatformAuditLogRepository;
import com.wissenup.domain.platform.service.PlatformAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformAuditServiceImpl implements PlatformAuditService {

    private final PlatformAuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String entityType, Long entityId, Long organizationId,
                    Map<String, Object> details, Long superAdminId) {
        try {
            String ipAddress = extractIpAddress();
            String userAgent = extractUserAgent();

            PlatformAuditLog log = PlatformAuditLog.builder()
                .super_admin_id(superAdminId)
                .action(action)
                .entity_type(entityType)
                .entity_id(entityId)
                .organization_id(organizationId)
                .details(details != null ? new HashMap<>(details) : new HashMap<>())
                .ip_address(ipAddress)
                .user_agent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Silently fail logging to avoid breaking business operations
            // but log the error for debugging
            System.err.println("Failed to log audit action: " + action + " - " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlatformAuditLogDto> listLogs(Pageable pageable) {
        Page<PlatformAuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        return convertToDto(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlatformAuditLogDto> listByAction(String action, Pageable pageable) {
        Page<PlatformAuditLog> logs = auditLogRepository.findByAction(action, pageable);
        return convertToDto(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlatformAuditLogDto> listByAdmin(Long superAdminId, Pageable pageable) {
        Page<PlatformAuditLog> logs = auditLogRepository.findBySuper_admin_id(superAdminId, pageable);
        return convertToDto(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlatformAuditLogDto> listByOrganization(Long organizationId, Pageable pageable) {
        Page<PlatformAuditLog> logs = auditLogRepository.findByOrganization_id(organizationId, pageable);
        return convertToDto(logs);
    }

    private Page<PlatformAuditLogDto> convertToDto(Page<PlatformAuditLog> logs) {
        return new PageImpl<>(
            logs.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()),
            logs.getPageable(),
            logs.getTotalElements()
        );
    }

    private PlatformAuditLogDto convertToDto(PlatformAuditLog log) {
        return PlatformAuditLogDto.builder()
            .logId(log.getLog_id())
            .superAdminId(log.getSuper_admin_id())
            .action(log.getAction())
            .entityType(log.getEntity_type())
            .entityId(log.getEntity_id())
            .organizationId(log.getOrganization_id())
            .details(log.getDetails())
            .ipAddress(log.getIp_address())
            .userAgent(log.getUser_agent())
            .timestamp(log.getTimestamp())
            .build();
    }

    private String extractIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String xForwardedFor = attributes.getRequest().getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0];
                }
                return attributes.getRequest().getRemoteAddr();
            }
        } catch (Exception e) {
            // Silently fail
        }
        return "UNKNOWN";
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Silently fail
        }
        return "UNKNOWN";
    }
}
