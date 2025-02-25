package com.garethevans.church.opensongtablet.openchords;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private String authToken;

    public AuthInterceptor(String authToken) {
        this.authToken = authToken;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + authToken); // Add the Authorization header

        Request modifiedRequest = builder.build();
        return chain.proceed(modifiedRequest);
    }

    @SuppressWarnings("unused")
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}