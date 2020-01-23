package com.example.bachelorarbeit.Activitys;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bachelorarbeit.Network;
import com.example.bachelorarbeit.R;
import com.example.bachelorarbeit.test.TestServer;
import com.example.bachelorarbeit.types.User;

public class ChatOverviewActivity extends AppCompatActivity {

    private String myID;
    private TextView receivedView;
    private EditText sendView;
    private Button send;
    private Spinner receiverName;
    private Network network;
    private TestServer testServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);

        // get objects from view
        receiverName = findViewById(R.id.receivername);
        receiverName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, User.values()));

        receivedView = findViewById(R.id.receivedView);
        sendView = findViewById(R.id.sendView);

        send = findViewById(R.id.send_btn);
        send.setOnClickListener( v -> send());

        this.myID = getIntent().getStringExtra("User");


        // connect to Websocket
        testServer = new TestServer(myID);
        new Thread(() -> testServer.connect("80.139.92.13", 3333)).start();

        // create network
        network = new Network(getApplicationContext(), myID, testServer, receivedView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testServer.disconnect();
    }

    private void send() {
        String destinationID =  receiverName.getSelectedItem().toString();
        String message = sendView.getText().toString();
        network.sendText(destinationID, message);
        addToTextView(this.myID, destinationID, message);
        testServer.echo(myID + " -> " + destinationID + " : " + message);
    }

    private void addToTextView(String sender, String receiver, String message) {
        this.receivedView.setText(sender + " -> " + receiver + ": " + message + "\n" + receivedView.getText());
    }
}
