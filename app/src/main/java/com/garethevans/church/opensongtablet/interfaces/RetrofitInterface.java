package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.openchords.OpenChordsFolderObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @GET("{id}")
    public Call<OpenChordsFolderObject> getOpenChordsFolder(@Path("id") String id);
    @POST("folder/{id}")
    public Response<OpenChordsFolderObject> postOpenChordsFolder(@Path("id") String id, @Body OpenChordsFolderObject openChordsFolderObject);

}
