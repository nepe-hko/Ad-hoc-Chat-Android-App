package com.example.bachelorarbeit.types;

import android.util.Log;

import com.google.android.gms.nearby.connection.Payload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class PayloadType {
    String type;

    public Payload serialize() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            os.flush();
            os.close();
            return Payload.fromBytes(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Object deserialize(byte[] data){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
