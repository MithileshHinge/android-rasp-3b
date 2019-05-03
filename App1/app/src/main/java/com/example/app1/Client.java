package com.example.app1;

/**
 * Created by Home on 10-07-2017.
 */
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client extends Thread {
    private String serverName;
    private DatagramSocket udpSocket;
    private Socket socket;
    private int udpPort = 7663;
    private int port = 7666;
    private volatile boolean livefeed = true, frameReceived = false;
    // private InputStream in;
    //private OutputStream out;
    private static SharedPreferences spref_ip;
    Client() {

    }

    public void run() {
        try {

            //serverName = MainActivity.jIP.getText().toString();
            //serverName="192.168.7.2";
            /*spref_ip = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
            serverName = spref_ip.getString("ip_address","");
*/
            serverName = RegistrationActivity.serverName;
            while (!LivefeedFragment.sendMsg(LivefeedFragment.volume)){}
            while(!LivefeedFragment.sendMsg(LivefeedFragment.BYTE_START_LIVEFEED)){}
            System.out.println("LIVEFEED TCP HANDSHAKE DONE");
            socket = new Socket(serverName, port);
            socket.setSoTimeout(2000);
            udpSocket = new DatagramSocket();

            //UDP Hole-punching
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     byte[] handshakeBuf = LoginActivity.clickedProductHashID.getBytes();
                     DatagramPacket handshakePacket = null;
                     try {
                         handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length, InetAddress.getByName(RegistrationActivity.serverName), udpPort);
                         while(!frameReceived) {
                             System.out.println("Sending handshake....");
                             udpSocket.send(handshakePacket);
                         }

                     }catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
             }).start();


            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            /*dOut.writeInt(udpSocket.getLocalPort());
            dOut.flush();*/
            dOut.writeUTF(LoginActivity.clickedProductHashID);
            dOut.flush();

            while(true) {
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
            socket.setSoTimeout(0);

            while (livefeed) {

                byte[] buf = new byte[64000];
                DatagramPacket imgPacket = new DatagramPacket(buf, buf.length);

                try {
                    udpSocket.setSoTimeout(5000);
                    udpSocket.receive(imgPacket);
                    //System.out.println(imgPacket.getAddress());
                }catch(SocketTimeoutException e){
                    e.printStackTrace();
                    continue;
                }
                byte[] imgBuf = imgPacket.getData();
                frameReceived = true;
                LivefeedFragment.frame = BitmapFactory.decodeByteArray(imgBuf, 0, imgBuf.length);
                LivefeedFragment.frameChanged = true;

                System.out.println("Frame received........");

            }
            livefeed = true;
            socket.close();
            System.out.println("CLIENT BANDA JHALA");

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
        frameReceived = false;
        System.out.println("live feed false keli");
    }
}

