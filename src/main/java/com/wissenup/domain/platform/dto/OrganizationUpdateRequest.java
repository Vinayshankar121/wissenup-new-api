package com.wissenup.domain.platform.dto;

import lombok.Data;

@Data
public class OrganizationUpdateRequest {
    private String organizationName;
    private String orgType;
    private String registrationNumber;
    private String officialEmail;
    private String officialPhone;
    private String website;
    private String logoUrl;
    private String status;
    private AddressData address;

    @Data
    public static class AddressData {
        private String addressLine1;
        private String addressLine2;
        private String locality;
        private String city;
        private String state;
        private String zipCode;
        private String country;

        public String flattenedAddress() {
            return java.util.stream.Stream.of(addressLine1, addressLine2, locality)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
        }
    }
}
