package com.example.konatsu_p.smartlockiot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Boolean isLock = true;
    TextView textView;
    BluetoothConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textview);

        connectServer();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("DEVICE_ID").child("Status");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                isLock = dataSnapshot.getValue(Boolean.class);
                if(isLock){
                    onA();
                }else{
                    offA();
                }
                textView.setText(isLock.toString());
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });

    }

    private void connectServer() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice selected = null;

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                Log.i("TAG", "DEVICE:" + device.getName());
                if (device.getName().equals("Key")) {
                    selected = device;
                    Toast.makeText(this, "デバイス名:" + device.getName(), Toast.LENGTH_LONG).show();
                }
            }

            mConnection = new BluetoothConnection(selected);
            mConnection.start();


        } else {
            Toast.makeText(getApplicationContext(), "端末がありません", Toast.LENGTH_SHORT).show();
        }
    }

    public void onA() {
        String text = "a";
        mConnection.send(text.getBytes());
    }


    public void offA() {
        String text = "b";
        mConnection.send(text.getBytes());
    }
}