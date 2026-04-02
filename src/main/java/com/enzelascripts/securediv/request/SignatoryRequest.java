package com.enzelascripts.securediv.request;

import com.enzelascripts.securediv.annotation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SignatoryRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must be 100 characters or less")
    private String name;

    @NotBlank(message = "Position cannot be blank")
    @Size(max = 50, message = "Position must be 50 characters or less")
    private String position;

    @NotNull(message = "Logo image is required")
    @ValidFile(message = "Please provide a valid image (png, jpeg) less than 500MB")
    private MultipartFile signatureImage;

}
