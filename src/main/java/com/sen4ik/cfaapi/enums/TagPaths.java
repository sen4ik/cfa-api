package com.sen4ik.cfaapi.enums;

public enum TagPaths {
    add("/tag/add"),
    getAll("/tag/all");

    public final String value;

    TagPaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }

}
