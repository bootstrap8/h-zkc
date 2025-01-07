package com.github.hbq969.code.zkc.view.request;

import com.github.hbq969.code.common.encrypt.ext.utils.AESUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;

@Data
public class LoginInfo {
    private String username;
    private String password;

    public static void main(String[] args) {
        String str=AESUtil.decrypt("rvONbr5AYjuGvMQP9yXw5XzsfDe1yp0bbZ7ymRLwGpOwJ9ZqaoO7JeqCm6JUVRu0","DC2EE8931E434A44", StandardCharsets.UTF_8);
        System.out.println(str);
    }
}
