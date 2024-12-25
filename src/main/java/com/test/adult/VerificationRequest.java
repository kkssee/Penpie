package com.test.adult;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerificationRequest {
    private String identityVerificationId;

    // Getter and Setter
    public String getIdentityVerificationId() {
        return identityVerificationId;
    }

    public void setIdentityVerificationId(String identityVerificationId) {
        this.identityVerificationId = identityVerificationId;
    }
}
