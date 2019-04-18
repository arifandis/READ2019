package com.cahstudio.registrasidigital.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListInsansi {
    @SerializedName("list")
    @Expose
    private java.util.List<Instansi> list = null;

    public java.util.List<Instansi> getList() {
        return list;
    }

    public void setList(java.util.List<Instansi> list) {
        this.list = list;
    }
}
