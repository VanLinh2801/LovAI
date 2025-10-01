package com.lovai.lovaiapi.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender { 
    male, female, other, prefer_not_say;

    @JsonCreator
    public static Gender from(String value) {
        return Gender.valueOf(value.toLowerCase());
    }
}
