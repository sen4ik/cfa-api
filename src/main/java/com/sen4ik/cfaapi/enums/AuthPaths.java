package com.sen4ik.cfaapi.enums;

public enum AuthPaths {
    signIn("/auth/signin");

    public final String value;

    AuthPaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
