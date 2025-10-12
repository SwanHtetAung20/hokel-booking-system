package com.sha.client_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequestResponseDTO {

    private String id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank(message = "Phone is mandatory")
    private String phone;
    @NotBlank(message = "Date of Birth is mandatory")
    private String dateOfBirth;
}
