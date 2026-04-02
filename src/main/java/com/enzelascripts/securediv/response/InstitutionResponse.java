package com.enzelascripts.securediv.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstitutionResponse {
    private String institutionName;
    private String address;
    private String logoUrl;
    private String motto;
    private String email;
    private String phoneNumber;
    private String website;
}