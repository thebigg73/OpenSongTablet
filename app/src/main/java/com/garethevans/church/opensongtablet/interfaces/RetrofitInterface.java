package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.openchords.OpenChordsAbc;
import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderPermissionsObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginRequest;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginResponse;
import com.garethevans.church.opensongtablet.openchords.OpenChordsMusicXML;
import com.garethevans.church.opensongtablet.openchords.OpenChordsReturnMessageObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInterface {

    @GET("folder/{id}")
    Call<OpenChordsFolderObject> getOpenChordsFolder(@Path("id") String id, @Query("userId") String userId);
    @POST("folder/{id}")
    Call<OpenChordsFolderObject> postOpenChordsFolder(@Path("id") String id, @Body OpenChordsFolderObject openChordsFolderObject);
    @POST("login")
    Call<OpenChordsLoginResponse> getAuthToken(@Body OpenChordsLoginRequest loginRequest);
    @POST("tools/convert")
    Call<OpenChordsAbc> postOpenChordsMusicXML(@Body OpenChordsMusicXML openChordsMusicXML);
    @POST("folder/{id}/permissions/")
    Call<OpenChordsReturnMessageObject> postOpenChordsFolderReadOnly(@Path("id") String id, @Body OpenChordsFolderPermissionsObject openChordsFolderPermissionsObject);
    @POST("folder/{id}/permissions/")
    Call<OpenChordsReturnMessageObject> postOpenChordsFolderOwner(@Path("id") String id,  @Body OpenChordsFolderPermissionsObject openChordsFolderPermissionsObject);
}
