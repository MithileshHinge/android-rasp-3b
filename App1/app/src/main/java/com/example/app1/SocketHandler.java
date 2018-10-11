package com.example.app1;

import java.net.Socket;

/**
 * Created by isha sagote on 30-09-2018.
 */
public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return SocketHandler.socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }
}
