package com.garethevans.church.opensongtablet.openchords;

public class OpenChordsLoginRequest {
    private String username;
    private String password;
    private String deviceId;

    public OpenChordsLoginRequest(String username, String password, String deviceId) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getDeviceId() {
        return deviceId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
