package com.example.konatsu_p.smartlockiot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BluetoothConnection extends Thread {
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private final OutputStream mOutput;
    Boolean isCheck = true;

    private enum State {CONNECT, CONNECTED, DISCONNECT}


    private State mState;

    public BluetoothConnection(BluetoothDevice device) {
        mDevice = device;
        BluetoothSocket socket = null;
        OutputStream out = null;
        try {
            socket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            out = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket = socket;
        mOutput = out;
        mState = State.CONNECT;
    }

    @Override
    public void run() {
        Log.d("LOG", "Client start");
        if (mSocket == null) return;
        while (isCheck) {
            switch (mState) {
                case CONNECT:
                    try {
                        mSocket.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mState = State.DISCONNECT;
                        return;
                    }
                    mState = State.CONNECTED;
                    Log.d("LOG", "Connected!");
                    isCheck = false;
                    break;
                case CONNECTED:
                    isCheck = false;
                    break;
                case DISCONNECT:
                    if (mSocket != null) {
                        try {
                            mOutput.write('E');
                            TimeUnit.MILLISECONDS.sleep(100);
                            mSocket.close();
                            Log.d("LOG", "Disconnected");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isCheck = false;
                    break;

            }
        }
    }

    public void send(byte sendData[]) {
        if (!mState.equals(State.CONNECTED)) return;
        try {
            mOutput.write(sendData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}




