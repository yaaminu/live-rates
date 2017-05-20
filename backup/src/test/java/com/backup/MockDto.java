package com.backup;

/**
 * Created by yaaminu on 5/17/17.
 */

public class MockDto {
    public static final String FIELD_NAME = "name", FIELD_EMAIL = "email";
    public String name;
    public String email;

    public MockDto(String email, String name) {
        this.name = name;
        this.email = email;
    }
}
