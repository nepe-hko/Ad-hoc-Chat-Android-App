package com.example.bachelorarbeit.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.bachelorarbeit.Activitys.ChatOverviewActivity;
import com.example.bachelorarbeit.R;
import com.example.bachelorarbeit.types.User;

public class StartActivity extends AppCompatActivity {

    Button start_btn;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        start_btn = findViewById(R.id.start_btn);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, User.values()));
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        start_btn.setOnClickListener(v -> {

            Intent intent = new Intent(this, ChatOverviewActivity.class);
            String user = spinner.getSelectedItem().toString();
            intent.putExtra("User", user); //Put your id to your next Intent
            startActivity(intent);
            //finish();
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
