package com.cahstudio.registrasidigital.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tamu {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("nama")
    @Expose
    private String nama;
    @SerializedName("instansi")
    @Expose
    private String instansi;
    @SerializedName("telp")
    @Expose
    private String telp;
    @SerializedName("ttd")
    @Expose
    private String ttd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getInstansi() {
        return instansi;
    }

    public void setInstansi(String instansi) {
        this.instansi = instansi;
    }

    public String getTelp() {
        return telp;
    }

    public void setTelp(String telp) {
        this.telp = telp;
    }

    public String getTtd() {
        return ttd;
    }

    public void setTtd(String ttd) {
        this.ttd = ttd;
    }
}
