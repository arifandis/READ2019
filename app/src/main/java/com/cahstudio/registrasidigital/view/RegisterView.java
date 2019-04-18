package com.cahstudio.registrasidigital.view;

import com.cahstudio.registrasidigital.model.Instansi;
import com.cahstudio.registrasidigital.model.Tamu;

import java.util.List;

public interface RegisterView {

    void loadInstansi(List<Instansi> instansiList);
    void exportToExcel(List<Tamu> tamuList);
    void onSuccess(String message);
    void onFail(String message);
    void showLoading();
    void hideLoading();
}
