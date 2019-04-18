package com.cahstudio.registrasidigital.api;

import com.cahstudio.registrasidigital.model.ListInsansi;
import com.cahstudio.registrasidigital.model.ListTamu;
import com.cahstudio.registrasidigital.model.RegisterResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    @GET("/read2019/api/getInstansi.php")
    Call<ListInsansi> getInstansi();

    @Multipart
    @POST("/read2019/api/addTamu.php")
    Call<RegisterResponse> registerTamu(@Part("name") RequestBody nama,
                                   @Part("instansi") RequestBody instansi,
                                   @Part("telp") RequestBody telp,
                                   @Part MultipartBody.Part image);

    @GET("/read2019/api/getTamu.php")
    Call<ListTamu> getTamu();
}
