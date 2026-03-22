package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.Nullable;

public class OpenChordsReturnMessageObject {

    @Nullable private String message;
    @Nullable private String error;
    @Nullable private String code;

    public void setMessage(@Nullable String message) {
        this.message = message;
    }
    public void setError(@Nullable String error) {
        this.error = error;
    }
    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Nullable public String getMessage() {
        return message;
    }
    @Nullable public String getError() {
        return error;
    }
    @Nullable public String getCode() {
        return code;
    }

}
