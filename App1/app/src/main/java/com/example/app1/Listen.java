package com.example.app1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by mithileshhinge on 07/01/18.
 */
public class Listen extends Thread {
    public byte[] buffer;
    public static DatagramSocket listenSocket;
    private Socket handshake_socket;
    private int Listen_UDP_Port = 7673 , Listen_TCP_Port = 7675;
    private int sampleRate = 16000;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize;
    private String serverName;

    final byte BYTE_START_LISTEN = 5;

    AudioTrack audioTrack;
    public boolean listen_status = true;


    public Listen(){

    }

    public void run(){

        try{
            System.out.println(".......TRY CATCH CHYA AAT.........");
            serverName = RegistrationActivity.serverName;
            while (!LivefeedFragment.sendMsg(BYTE_START_LISTEN)){}

            handshake_socket = new Socket(serverName, Listen_TCP_Port);
            listenSocket = new DatagramSocket();

            DataOutputStream dOut = new DataOutputStream(handshake_socket.getOutputStream());
            dOut.writeInt(listenSocket.getLocalPort());
            dOut.flush();
            int m = handshake_socket.getInputStream().read();
            if( m != 1){
                Log.d("System is offline","");
                System.out.println("............client received = "+ m);
                return;
            }


            minBufSize = 40960;
            buffer = new byte[minBufSize];

            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

            System.out.println("DataGramSocket BANAVLA!!!!!");
            System.out.println("Server: " + serverName);

            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,AudioFormat.CHANNEL_OUT_MONO,audioFormat,minBufSize,AudioTrack.MODE_STREAM);
            audioTrack.play();

            while(true){

                System.out.println(".......blah blah.......");
                //listenSocket = new DatagramSocket(Listen_UDP_Port);

                listenSocket.receive(packet);
                System.out.println("PACKETS RECEIVED DATA:" + String.valueOf(buffer));
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
    }
}
