package com.sen4ik.cfaapi.enums;

public enum FilePaths {
    prefix("/file"),
    prefixWithSlash("/file/"),
    add("/file/add"),
    getFilesForCategory("/file/category/"),
    getAll("/file/all"),
    findByFileName("/file/findByFileName?fileName="),
    findByFileTitle("/file/findByFileTitle?fileTitle="),
    replaceFile("/file/replaceFile/"),
    updateFileInfo("/file/updateFileInfo/");

    public final String value;

    FilePaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
