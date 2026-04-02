package com.enzelascripts.securediv.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CertificateResponse {
    private String certificateNumber;
    private String firstName;
    private String lastName;
    private String degree;
    private String course;
    private String qrCode;
    private String logoUrl;
    private String address;
    private String classOfDegree;
    private String institutionName;

    private LocalDate graduationDate;

    private List<SignatoryResponse> signatory;

    private boolean isValid;


}
