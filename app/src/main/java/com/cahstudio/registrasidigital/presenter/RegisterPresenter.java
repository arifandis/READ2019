package com.cahstudio.registrasidigital.presenter;

import android.os.AsyncTask;
import android.util.Log;

import com.cahstudio.registrasidigital.api.ApiInterface;
import com.cahstudio.registrasidigital.api.ApiService;
import com.cahstudio.registrasidigital.model.Instansi;
import com.cahstudio.registrasidigital.model.ListInsansi;
import com.cahstudio.registrasidigital.model.ListTamu;
import com.cahstudio.registrasidigital.model.RegisterResponse;
import com.cahstudio.registrasidigital.model.Tamu;
import com.cahstudio.registrasidigital.view.RegisterView;

import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterPresenter {
    private RegisterView view;
    private ApiInterface api;
    InitialTask mTask;
    InstansiTask mInstansiTask;

    public RegisterPresenter(RegisterView view, ApiInterface api) {
        this.view = view;
        this.api = api;
    }

    public void retrieveInstansi(){
        if (mInstansiTask == null || mInstansiTask.getStatus() != AsyncTask.Status.RUNNING) {
            mInstansiTask = new InstansiTask(view,api);
            mInstansiTask.execute();
        }
    }

    public void addTamu(RequestBody nama, RequestBody instansi, RequestBody telp, MultipartBody.Part image){
        view.showLoading();
        Call<RegisterResponse> registerResponse = api.registerTamu(nama,instansi,telp,image);
        registerResponse.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                RegisterResponse respon = response.body();
                view.hideLoading();
                Log.d("status",respon.getStatus());
                view.onSuccess("Registrasi berhasil");
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                view.hideLoading();
                 Log.d("failed",t.getMessage());
                 view.onFail("Registrasi gagal, ulangi kembali");
            }
        });
    }

    public void retrieveTamu(){
        if (mTask== null || mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask = new InitialTask(view,api);
            mTask.execute();
        }
    }

    public static class InstansiTask extends AsyncTask<Void, Integer, ListInsansi>{

        private RegisterView taskView;
        private ApiInterface taskApi;

        public InstansiTask(RegisterView taskView, ApiInterface taskApi) {
            this.taskView = taskView;
            this.taskApi = taskApi;
        }

        @Override
        protected ListInsansi doInBackground(Void... voids) {
            try {
                Response<ListInsansi> response = taskApi.getInstansi().execute();
                if (response.isSuccessful()){
                    return response.body();
                }
            }catch (Exception e){
                Log.d("instansi",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ListInsansi listInsansi) {
            super.onPostExecute(listInsansi);
            if (listInsansi != null){
                taskView.loadInstansi(listInsansi.getList());
            }
        }
    }

    public static class InitialTask extends AsyncTask<Void, Integer, ListTamu>{

        private RegisterView taskView;
        private ApiInterface taskApi;

        public InitialTask(RegisterView taskView, ApiInterface taskApi) {
            this.taskView = taskView;
            this.taskApi = taskApi;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskView.showLoading();
        }

        @Override
        protected ListTamu doInBackground(Void... voids) {
            try {
                Response<ListTamu> response = taskApi.getTamu().execute();
                if (response.isSuccessful()){
                    return response.body();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(ListTamu listTamu) {
            super.onPostExecute(listTamu);
            if (listTamu != null){
                taskView.hideLoading();
                taskView.exportToExcel(listTamu.getList());
            }
        }
    }
}