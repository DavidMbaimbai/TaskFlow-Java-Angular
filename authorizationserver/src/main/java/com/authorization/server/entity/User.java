package com.authorization.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userUuid;
    private String email;
    private String qrCodeSecret;
    public boolean mfa;

}
