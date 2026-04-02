package com.enzelascripts.securediv.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatoryResponse {
    private String name;
    private String position;
    private String signatureUrl;
}
