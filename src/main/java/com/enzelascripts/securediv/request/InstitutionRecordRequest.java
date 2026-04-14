package com.enzelascripts.securediv.request;

import com.enzelascripts.securediv.annotation.ValidFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InstitutionRecordRequest {

    // signatory details
    @NotBlank(message = "Institution name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    @Schema(example = "University of Lagos")
    private String institutionName;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be 255 characters or less")
    @Schema(example = "Akoka, Lagos")
    private String address;

    @NotNull(message = "Logo image is required")
    @ValidFile(message = "The image size must be 0.5MB or less. JPEG/PNG only")
    @Schema(type = "string", format = "binary", description = "Logo image (JPEG/PNG, max 500KB)")
    private MultipartFile logoImage;

    @Schema(example = "Knowledge for Service")
    private String motto;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Schema(example = "info@unilag.edu.ng")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
            message = "Please provide a valid phone number")
    @Schema(example = "+2348012345678")
    private String phoneNumber;

    @NotBlank(message = "Website is required")
    @URL(message = "Please provide a valid website URL")
    @Schema(example = "https://www.unilag.edu.ng")
    private String website;

}