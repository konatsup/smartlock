package com.example.konatsu_p.smartlockiot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.support.constraint.Constraints.TAG;

public class BluetoothConnection extends Thread {
    private BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private boolean mIsRunning;

    private enum State {CONNECT, CONNECTED, DISCONNECT}

    private State mState;
    private String text = "";

    public BluetoothConnection(BluetoothDevice device) {
        mDevice = device;
        mIsRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                    mSocket.connect();
                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while (mIsRunning) {
                        bytesRead = mInputStream.read(buffer);
                        final String readMsg = new String(buffer, 0, bytesRead);
                        if (readMsg.trim() != null && !readMsg.trim().equals("")) {
                            Log.i(TAG, "value=" + readMsg.trim());

                            String crlf = System.getProperty("line.separator");
                            text = text + readMsg.trim() + crlf;

                        } else {
                            Log.i(TAG, "value=nodata");
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "error:" + e);
                    try {
                        mSocket.close();
                    } catch (Exception ee) {
                    }
                    mIsRunning = false;
                }
            }
        }).start();

        mState = State.CONNECT;
    }

    @Override
    public void run() {
        Log.d("LOG", "Client start");
        if (mSocket == null) return;
        while (mIsRunning) {
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
                    mIsRunning = false;
                    break;
                case CONNECTED:
                    mIsRunning = false;
                    break;
                case DISCONNECT:
                    if (mSocket != null) {
                        try {
                            mOutputStream.write('E');
                            disconnectDevice();
                            TimeUnit.MILLISECONDS.sleep(100);
                            mSocket.close();
                            Log.d("LOG", "Disconnected");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mIsRunning = false;
                    break;

            }
        }
    }

    public void send(byte sendData[]) {
        if (!mState.equals(State.CONNECTED)) return;
        try {
            mOutputStream.write(sendData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectDevice() {

        if (mDevice == null) {
            return;
        }

        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }

            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }

            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}




