package com.example.app1;

/**
 * Created by Home on 10-07-2017.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client extends Thread {
    private String serverName;
    private DatagramSocket udpSocket;
    private Socket socket;
    private int udpPort = 7663;
    private int port = 7666;
    private boolean livefeed = true;
    // private InputStream in;
    //private OutputStream out;
    private static SharedPreferences spref_ip;
    Client() {

    }

    public void run() {
        try {
            //serverName = MainActivity.jIP.getText().toString();
            //serverName="192.168.7.2";
            spref_ip = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
            serverName = spref_ip.getString("ip_address","");

            while(!LivefeedFragment.sendMsg(LivefeedFragment.BYTE_START_LIVEFEED)){}

            socket = new Socket(serverName, port);
            socket.setSoTimeout(500);
            udpSocket = new DatagramSocket();

            /*byte[] handshakeBuf = new byte[256];
            DatagramPacket handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length, InetAddress.getByName(serverName), udpPort);
            while(true) {
                System.out.println("Sending handshake....");
                udpSocket.send(handshakePacket);
                try {
                    socket.getInputStream().read();
                    break;
                } catch (SocketTimeoutException e){
                    e.printStackTrace();
                }
            }*/
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            dout.writeInt(udpSocket.getLocalPort());
            dout.flush();

            if (socket.getInputStream().read() != 1){
                Log.d("System is offline!!!!", "");
                return;
            }

            while (true) {

                byte[] buf = new byte[64000];
                DatagramPacket imgPacket = new DatagramPacket(buf, buf.length);

                try {
                    udpSocket.setSoTimeout(5000);
                    udpSocket.receive(imgPacket);
                }catch(SocketTimeoutException e){
                    e.printStackTrace();
                    continue;
                }
                byte[] imgBuf = imgPacket.getData();

                LivefeedFragment.frame = BitmapFactory.decodeByteArray(imgBuf, 0, imgBuf.length);
                LivefeedFragment.frameChanged = true;

                System.out.println("Frame received........");
                if (!livefeed) {
                    livefeed = true;
                    socket.close();
                    System.out.println("CLIENT BANDA JHALA");
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(socket!=null)
                    socket.close();
                if(udpSocket!=null)
                    udpSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void end(){

        livefeed = false;
    }
}

