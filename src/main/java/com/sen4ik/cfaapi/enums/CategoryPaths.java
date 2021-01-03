package com.sen4ik.cfaapi.enums;

public enum CategoryPaths {
    prefix("/category"),
    prefixWithSlash("/category/"),
    breadcrumbsWithSlash("/category/breadcrumbs/"),
    getCategoriesByParentId("/category/parent/"),
    add("/category/add"),
    findByCategoryName("/category/findByCategoryName?categoryName="),
    getAll("/category/all");

    public final String value;

    CategoryPaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
