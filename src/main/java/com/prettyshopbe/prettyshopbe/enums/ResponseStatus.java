package com.prettyshopbe.prettyshopbe.enums;

public enum ResponseStatus
{
    SUCCESS("success"), ERROR("error");
    private String value;

    ResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
