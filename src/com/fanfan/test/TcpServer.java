package com.fanfan.test;

import android.util.Log;
import java.io.*;
import java.net.*;

public class TcpServer extends Thread {
    private static final String TAG = "TcpServer";
    private ServerSocket  mTcpServerSocket = null;
    private H264HwEncoder mH264Encoder     = null;
    private boolean       mExit            = false;
    
    public TcpServer(int port, H264HwEncoder h264enc) {
        try {
            mTcpServerSocket = new ServerSocket(port);
            mTcpServerSocket.setSoTimeout(1000);
            mTcpServerSocket.setReuseAddress(true);
        } catch (Exception e) { e.printStackTrace(); }
        mH264Encoder = h264enc;
    }

    @Override
    public void run() {
        while (!mExit) {
            Socket   client = null;
            OutputStream os = null;
            try {
                client = mTcpServerSocket.accept();
                os     = client.getOutputStream();
                while (!mExit) {
                    byte[] out = mH264Encoder.dequeueOutputBuffer(100 * 1000);
                    if (out != null) {
                        Log.d(TAG, "get h264 buffer: " + out + ", len: " + out.length);
                        os.write(out);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { os.close();        } catch (Exception e) {}
                try { client.close();    } catch (Exception e) {}
                try { Thread.sleep(100); } catch (Exception e) {}
            }
        }
    }

    public void close() {
        mExit = true;
        try { mTcpServerSocket.close(); } catch (Exception e) {}
        try { join(); } catch (Exception e) {}
    }
}
