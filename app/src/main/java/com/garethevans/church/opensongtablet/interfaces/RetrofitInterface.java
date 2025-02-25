package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginRequest;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @GET("folder/{id}")
    Call<OpenChordsFolderObject> getOpenChordsFolder(@Path("id") String id);
    @POST("folder/{id}")
    Call<OpenChordsFolderObject> postOpenChordsFolder(@Path("id") String id, @Body OpenChordsFolderObject openChordsFolderObject);
    @POST("login")
    Call<OpenChordsLoginResponse> getAuthToken(@Body OpenChordsLoginRequest loginRequest);
}
