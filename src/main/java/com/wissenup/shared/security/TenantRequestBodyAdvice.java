package com.wissenup.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Request body advice that automatically injects the authenticated organization_id
 * into request DTOs that are about to be processed by the service layer.
 *
 * This prevents frontend from being able to override the organization_id.
 * The tenant always comes from the JWT, never from client input.
 */
@RestControllerAdvice
@Slf4j
public class TenantRequestBodyAdvice implements RequestBodyAdvice {

    private final ObjectMapper objectMapper;

    public TenantRequestBodyAdvice() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                          Type targetType,
                                          Class<? extends HttpMessageConverter<?>> converterType)
        throws IOException {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                               Type targetType,
                               Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            // Only inject if user is authenticated
            if (!SecurityContextUtil.isAuthenticated()) {
                return body;
            }

            Long organizationId = SecurityContextUtil.getCurrentOrganizationId();

            // Only process if body is an object (has organizationId field)
            if (body == null) {
                return body;
            }

            // Convert body to JSON node
            JsonNode jsonNode = objectMapper.valueToTree(body);

            if (jsonNode instanceof ObjectNode objectNode) {
                // Inject organization_id (overwrite any client-provided value)
                objectNode.put("organizationId", organizationId);

                // Convert back using the class of the original body
                return objectMapper.treeToValue(objectNode, body.getClass());
            }

            return body;

        } catch (Exception ex) {
            log.warn("Failed to inject organization_id into request body: {}", ex.getMessage());
            return body; // Return unmodified body on error
        }
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                 Type targetType,
                                 Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
