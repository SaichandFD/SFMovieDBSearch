package com.example.moviesearch.application.model;

import com.google.gson.annotations.SerializedName;

public class Rating {
    @SerializedName("Source")
    public String source;
    @SerializedName("Value")
    public String value;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
