package com.wissenup.domain.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after successful OTP verification.
 * Contains JWT token and user details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private Long organizationId;
    private Long roleId;
    private String role;
    private Long parentId;
    private String email;
}
