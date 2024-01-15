package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent iHome =new Intent(Splash.this, MainActivity.class);

        new Handler().postDelayed(() -> {

            startActivity(iHome);
            finish();
        }, 4000);

    }
}