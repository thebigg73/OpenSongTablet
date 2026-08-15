package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.openchords.OpenChordsAbc;
import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderPermissionsObject;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginRequest;
import com.garethevans.church.opensongtablet.openchords.OpenChordsLoginResponse;
import com.garethevans.church.opensongtablet.openchords.OpenChordsMusicXML;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInterface {

    // Get the server folder
    @GET("folder/{id}")
    Call<OpenChordsFolderObject> getOpenChordsFolder(@Path("id") String id, @Query("userID") String userID);

    // Establish a handshake with auth token
    @POST("login")
    Call<OpenChordsLoginResponse> getAuthToken(@Body OpenChordsLoginRequest loginRequest);

    // Convert OpenSongXML to ABC notation
    @POST("tools/convert")
    Call<OpenChordsAbc> postOpenChordsMusicXML(@Body OpenChordsMusicXML openChordsMusicXML);

    // This method is safer if we get no body returned
    // Upload the new folder contents
    @POST("folder/{id}")
    Call<ResponseBody> postOpenChordsFolder(@Path("id") String id, @Query("userID") String userID, @Body OpenChordsFolderObject openChordsFolderObject);

    // Change the read only status of the folder
    @POST("folder/{id}/permissions/")
    Call<ResponseBody> postOpenChordsFolderReadOnly(@Path("id") String id, @Body OpenChordsFolderPermissionsObject openChordsFolderPermissionsObject);
}
