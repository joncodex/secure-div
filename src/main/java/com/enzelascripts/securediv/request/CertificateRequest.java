package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CertificateRequest extends DocumentRequest {
    @NotBlank(message = "class of Degree this student graduated with is required")
    private String classOfDegree;   // First-class Honors

}
