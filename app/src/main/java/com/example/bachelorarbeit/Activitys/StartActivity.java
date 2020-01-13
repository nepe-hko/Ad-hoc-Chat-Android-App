package com.example.bachelorarbeit.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
        Log.d("test", "erfolgreich gestartet");

        start_btn.setOnClickListener(v -> {

            Intent intent = new Intent(this, ChatOverviewActivity.class);
            String user = spinner.getSelectedItem().toString();
            Log.d("test", "Ausgewählter name:" + user);
            intent.putExtra("User", user); //Put your id to your next Intent
            startActivity(intent);
            //finish();
        });
    }
}
