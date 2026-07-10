package com.dww.chat_app.constant;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class UserConstant {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER_DESCRIPTION = "Default user role";
    public static final String ROLE_ADMIN_DESCRIPTION = "System administrator role";

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "Asdf1234!";

    private UserConstant() {
    }
}
