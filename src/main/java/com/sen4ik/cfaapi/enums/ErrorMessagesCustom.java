package com.sen4ik.cfaapi.enums;

public enum ErrorMessagesCustom {

    expired_or_invalid_jwt_token("Expired or invalid JWT token"),
    invalid_username_password_supplied("Invalid username/password supplied"),
    required_request_body_is_missing("Required request body is missing"),
    access_denied("Access Denied");

    public final String value;

    ErrorMessagesCustom(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
