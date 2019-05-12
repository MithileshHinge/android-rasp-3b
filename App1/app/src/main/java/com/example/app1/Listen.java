package com.example.app1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Sibhali on 10-05-2019.
 */

public class Listen extends Thread {

    public byte[] buffer;
    public static DatagramSocket listenSocket;
    private Socket handshake_socket;
    private int Listen_UDP_Port = 7673 , Listen_TCP_Port = 7675;
    private int sampleRate = 44100;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize;
    private String serverName;

    final byte BYTE_START_LISTEN = 5;

    AudioTrack audioTrack;
    public volatile boolean listen_status = true, listenPacketRx = false;
    private long time1 = System.currentTimeMillis();


    public Listen(){

    }

    public void run(){
        try{
            listenPacketRx = false;
            System.out.println(".......TRY CATCH CHYA AAT.........");
            serverName = RegistrationActivity.serverName;
            while (!LivefeedFragment.sendMsg(BYTE_START_LISTEN)){}

            handshake_socket = new Socket(serverName, Listen_TCP_Port);
            listenSocket = new DatagramSocket();

            String hashId = LoginActivity.clickedProductHashID;
            DataOutputStream dout = new DataOutputStream(handshake_socket.getOutputStream());
            dout.writeUTF(hashId);
            dout.flush();
            int m = handshake_socket.getInputStream().read();
            if( m != 1){
                Log.d("System is offline","");
                System.out.println("............client received = "+ m);
                return;
            }

            //UDP Hole-punching
            /*byte[] handshakeBuf = new byte[256];
            DatagramPacket handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length, InetAddress.getByName(serverName),Listen_UDP_Port);
            for (int i=0; i<10; i++){
                System.out.println("Sending listen handshake....");
                listenSocket.send(handshakePacket);
            }*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] handshakeBuf = new byte[256];
                    DatagramPacket handshakePacket = null;
                    try {
                        handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length, InetAddress.getByName(RegistrationActivity.serverName), Listen_UDP_Port);
                        while(!listenPacketRx) {
                            System.out.println("Sending listen handshake....");
                            listenSocket.send(handshakePacket);
                        }

                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            minBufSize = 4096;
            buffer = new byte[minBufSize];

            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,AudioFormat.CHANNEL_OUT_MONO,audioFormat,minBufSize,AudioTrack.MODE_STREAM);
            audioTrack.play();

            listenSocket.setSoTimeout(5000);
            while(true){

                //System.out.println(".......blah blah.......");
                //listenSocket = new DatagramSocket(Listen_UDP_Port);
                time1 = System.currentTimeMillis();
                try {
                    listenSocket.receive(packet);
                    listenPacketRx = true;
                    System.out.println("BUFFER" + packet.getLength()/1024 + " " + packet.getData().length);
                    System.out.println("PACKETS RECEIVED DATA:" + String.valueOf(buffer));

                }catch (SocketTimeoutException e){
                    e.printStackTrace();
                }
                System.out.println("time ................. = " + (System.currentTimeMillis() - time1) );
                audioTrack.write(buffer, 0, minBufSize);
                audioTrack.play();

                //listenSocket.close();

                if (!listen_status){
                    if(audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                        if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                            try {
                                audioTrack.stop();
                                System.out.println("AUDIO TRACK STOP!!!");
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }

                        audioTrack.release();
                        System.out.println("!!! AUDIO TRACK RELEASED");
                    }

                    //while (!LivefeedFragment.sendMsg(BYTE_STOP_LISTEN)){}


                    listen_status = true;
                    handshake_socket.close();
                    System.out.println("LISTEN CLIENT BANDA JHALA");
                    return;
                }
            }

        }catch(IOException e){
            e.printStackTrace();
            try {
                if(handshake_socket!=null)
                    handshake_socket.close();
                if(listenSocket!=null)
                    listenSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void end(){
        listen_status = false;
        listenPacketRx = true;
    }

}
