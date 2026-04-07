package com.enzelascripts.securediv.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevokeRequest {
    private String documentNumber;
    private String reason;
    private String revokedBy;
    private boolean notifyStudent;
    private String notificationMessage;
}
