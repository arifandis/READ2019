package com.cahstudio.registrasidigital.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListTamu {

    @SerializedName("list")
    @Expose
    private java.util.List<Tamu> list = null;

    public java.util.List<Tamu> getList() {
        return list;
    }

    public void setList(java.util.List<Tamu> list) {
        this.list = list;
    }
}
