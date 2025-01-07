package com.github.hbq969.code.zkc.view.request;

import lombok.Data;

@Data
public class PasswordModify {
    private Long id;
    private String newPassword;
    private String oldPassword;
}
