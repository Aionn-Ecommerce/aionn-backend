package com.aionn.ucp.domain.model;

public enum UcpStatus {
    SUCCESS,
    ERROR;

    public String wireValue() {
        return name().toLowerCase();
    }
}
