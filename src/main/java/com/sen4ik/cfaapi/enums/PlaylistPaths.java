package com.sen4ik.cfaapi.enums;

public enum PlaylistPaths {
    prefix("/playlist"),
    getAll("/playlist/all"),
    add("/playlist/add"),
    getPlaylistsForUser("/playlist/user/"),
    prefixWithSlash("/playlist/");

    public final String value;

    PlaylistPaths(String value) {
        this.value = value;
    }

    final String value() {
        return value;
    }
}
