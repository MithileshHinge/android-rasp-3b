package com.example.app1;

/**
 * Created by Home on 10-07-2017.
 */
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client extends Thread {
    private String serverName;
    //private DatagramSocket udpSocket;
    private Socket socket;
    //private int udpPort = 7663;
    private int port = 7666;
    private volatile boolean livefeed = true;
    //private InputStream in;
    //private OutputStream out;
    private static SharedPreferences spref_ip;

    Client() {

    }

    public void run() {
        try {

            serverName = RegistrationActivity.serverName;
            while (!LivefeedFragment.sendMsg(LivefeedFragment.volume)){}
            while(!LivefeedFragment.sendMsg(LivefeedFragment.BYTE_START_LIVEFEED)){}
            System.out.println("LIVEFEED TCP HANDSHAKE DONE");
            socket = new Socket(serverName, port);
            socket.setSoTimeout(2000);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            dOut.writeUTF(LoginActivity.clickedProductHashID);
            dOut.flush();
            System.out.println("HASH ID SENT");
            while(true) {
                System.out.println("IN WHILE TRUE");
                int m;
                try {
                    m = socket.getInputStream().read();
                }catch(SocketTimeoutException s){
                    s.printStackTrace();
                    continue;
                }
                if (m != 1) {
                    System.out.println("System mobile not mapped............mob received = " + m);
                    return;
                }else
                    break;

            }

            while (livefeed) {
                byte[] bytes;
                int size;
                try{
                    socket.setSoTimeout(5000);
                    size = dIn.readInt();
                    System.out.println("Buffer size : " + size);
                    bytes = new byte[size];
                    dIn.readFully(bytes,0,size);
                    System.out.println("BYTES READ : " + bytes);
                    //socket.close();
                }catch (SocketTimeoutException e){
                    e.printStackTrace();
                    continue;
                }

                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds= true;*/
                //LivefeedFragment.frame = BitmapFactory.decodeStream(new FlushedInputStream(dIn),null,options);
                LivefeedFragment.frame = BitmapFactory.decodeByteArray(bytes,0,size);
                if(LivefeedFragment.frame == null)
                    System.out.println("FRAME NULL");
                else{
                    LivefeedFragment.frameChanged = true;
                }
                System.out.println("Frame received........");


            }
            livefeed = true;
            System.out.println("CLIENT BANDA JHALA");

        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(socket!=null)
                    socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void end(){
        livefeed = false;
        try {
            if(socket!=null)
                socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("live feed false keli");
    }
}

