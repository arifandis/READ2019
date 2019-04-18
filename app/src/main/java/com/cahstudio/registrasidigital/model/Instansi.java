package com.cahstudio.registrasidigital.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Instansi {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("instansi")
    @Expose
    private String instansi;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstansi() {
        return instansi;
    }

    public void setInstansi(String instansi) {
        this.instansi = instansi;
    }
}
