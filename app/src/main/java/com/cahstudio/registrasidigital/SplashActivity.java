package com.cahstudio.registrasidigital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cahstudio.registrasidigital.main.RegistrasiActivity;

import java.util.Timer;
import java.util.TimerTask;

/*
 * Author by: Arifandis Winata
 * Nickname: Fandis
 *
 * */

public class SplashActivity extends AppCompatActivity {

    private String PREF_NAME = "READ2019";
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        editor = getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext() , RegistrasiActivity.class));
                finish();
            }
        }, 2000);
    }
}
