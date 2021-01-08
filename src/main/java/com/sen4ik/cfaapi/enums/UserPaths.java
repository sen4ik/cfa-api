package com.sen4ik.cfaapi.enums;

public enum UserPaths {
    signUp("/user/signup"),
    getAll("/user/all"),
    prefix("/user"),
    prefixWithSlash("/user/"),
    me("/user/me");

    public final String value;

    UserPaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
