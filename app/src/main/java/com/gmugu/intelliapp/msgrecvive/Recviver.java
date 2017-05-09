package com.gmugu.intelliapp.msgrecvive;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by mugu on 17/5/4.
 */


public class Recviver {

    private static final String TAG = Recviver.class.getSimpleName();

    public interface OnRecviveData {
        void onRecviveData(byte[] data);
    }

    private boolean isRunning;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private final Context context;
    private final int port;
    private OnRecviveData onRecviveData;

    public Recviver(Context context, int port, OnRecviveData onRecviveData) {
        this.context = context;
        this.port = port;
        this.onRecviveData = onRecviveData;
    }

    public boolean startRecvive() {
        if (socket == null) {
            try {
                socket = new DatagramSocket(port);
            } catch (SocketException e) {
                e.printStackTrace();
                Toast.makeText(context, "Open Datagram Socket port: " + port + " fail", Toast.LENGTH_LONG).show();
                return false;
            }
            byte buf[] = new byte[1024];
            packet = new DatagramPacket(buf, buf.length);
            isRunning = true;
            new Thread() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            socket.receive(packet);
                            byte cache[] = packet.getData();
                            byte data[] = new byte[packet.getLength()];
                            for (int i = 0; i < data.length; i++) {
                                data[i] = cache[i];
                            }
                            onRecviveData.onRecviveData(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            Log.d(TAG, "startRecvive port: " + port);
        }
        return true;
    }

    public void stopRecvive() {
        isRunning = false;
        if (socket != null) {
            socket.close();
        }
    }

    public void resetRecive() {
        stopRecvive();
        // Delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startRecvive();
    }

    public boolean isRunning() {
        return isRunning && socket != null;
    }

}