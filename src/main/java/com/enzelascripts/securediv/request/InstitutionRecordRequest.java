package com.enzelascripts.securediv.request;

import com.enzelascripts.securediv.annotation.ValidFile;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InstitutionRecordRequest {

    /// signatory details
    @NotBlank(message = "Institution name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    private String institutionName;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be 255 characters or less")
    private String address;

    @NotNull(message = "Logo image is required")
    @ValidFile(message = "The image size must be 5KB or less. JPEG/PNG only")
    private MultipartFile logoImage;     // URL to logo image

    private String motto;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
            message = "Please provide a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "Website is required")
    @URL(message = "Please provide a valid website URL")
    private String website;

}