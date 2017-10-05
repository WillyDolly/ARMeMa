package com.popland.pop.armema;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import Utils.SQLite;

public class SplashActivity extends AppCompatActivity {
    public static SQLite sqlite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sqlite = new SQLite(this, "Info.db", null, 1);
        sqlite.queryData("CREATE TABLE IF NOT EXISTS Marker(id INTEGER PRIMARY KEY AUTOINCREMENT,text VARCHAR," +
                "image BLOB,color INTEGER,latitude DOUBLE,longitude DOUBLE)");
        CountDownTimer cdt = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Intent i = new Intent(SplashActivity.this,MapsActivity.class);
                startActivity(i);
            }
        }.start();
    }
}
