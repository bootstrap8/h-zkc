package com.github.hbq969.code.zkc.view.request;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class PasswordModify {
    private Long id;
    private String newPassword;
    private String oldPassword;

    public void hash(BCryptPasswordEncoder encoder) {
        if (StringUtils.isNotEmpty(newPassword)) {
            this.newPassword = encoder.encode(this.newPassword);
        }
    }
}
