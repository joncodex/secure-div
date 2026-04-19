package com.enzelascripts.securediv.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookPayload {
    private String documentType;
    private String documentNumber;
    private String studentFirstName;
    private String studentLastName;
    private String email;
    private LocalDateTime timeStamp;
}
