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
    private Thread mSendThread;

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
                    BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                    socket.connect();
                    mSocket = socket;
                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while (mIsRunning) {
                        bytesRead = mInputStream.read(buffer);
                        final String readMsg = new String(buffer, 0, bytesRead);
                        String message = readMsg.trim();
                        Log.i(TAG, "readMsg.trim0 value=" + message);
                        if (message != null && !message.equals("")) {
                            Log.i(TAG, "readMsg.trim value=" + message);
                            String[] splitMsgs = message.split(":", 0);
                            Log.i(TAG, "readMsg.trim1 value=" + splitMsgs[1]);
                            if (splitMsgs[1].equals("fin")){
                                // NFCの登録が成功したことを通知
                                Log.d(TAG, "value= カードの登録が完了しました");
                                Thread.sleep(5000);
                                mSendThread.interrupt();
                            }else {
                                switch (message) {
                                    case "ON":
                                        // Lock処理が完了した場合
                                        Log.d(TAG, "value=ロックしました");
                                        break;
                                    case "f":
                                        // Unlock処理が完了した場合
                                        Log.d(TAG, "value=アンロックしました");
                                        break;
                                    default:
                                        break;
                                }
                            }

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

    public void send(final byte sendData[]) {
        if (mOutputStream == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                        Log.d(TAG, "sendData value=" + sendData);
                        mOutputStream.write(sendData);
                } catch (Exception e) {
                    Log.d(TAG, "error sendData value=");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startSendThread(final byte sendData[]) {
        if (mOutputStream == null) {
            return;
        }

        mSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Log.d(TAG, "sendData value=c");
                        mOutputStream.write(sendData);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "error sendData value=c");
                    e.printStackTrace();
                }
            }
        });
        mSendThread.start();
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




