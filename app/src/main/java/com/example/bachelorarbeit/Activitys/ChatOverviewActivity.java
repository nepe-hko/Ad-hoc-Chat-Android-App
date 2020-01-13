package com.example.bachelorarbeit.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.bachelorarbeit.Network;
import com.example.bachelorarbeit.R;
import com.example.bachelorarbeit.types.User;

public class ChatOverviewActivity extends AppCompatActivity {

    //Network network;
    String user;
    TextView receivedView;
    EditText sendView;
    Button send;
    Spinner receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);
        // get Intents
        Log.d("test", "erfolgreich in chatoverview gewechselt");
        Intent intent = getIntent();
        this.user = intent.getStringExtra("User");
        Log.d("test", user);

        // get objects from view
        receiverName = findViewById(R.id.receivername);
        receiverName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, User.values()));
        send = findViewById(R.id.send_btn);
        receivedView = findViewById(R.id.receivedView);
        sendView = findViewById(R.id.sendView);

        send.setOnClickListener( v -> {
            addToTextView(user, sendView.getText().toString());
        });
        //network = new Network(getApplicationContext());
    }

    private void addToTextView(String name, String message) {
        this.receivedView.append(name + ": " + message);
    }
}
