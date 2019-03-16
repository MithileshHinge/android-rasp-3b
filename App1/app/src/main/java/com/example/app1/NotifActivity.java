package com.example.app1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Sibhali on 7/12/2017.
 */

public class NotifActivity extends AppCompatActivity {
    //public static ImageView jIVFrame;
    public static ImageView jIV;
    public static VideoView jVV;
    private static int videoNotifID;
    private static String hashID;
    public static Context context;
    public static String servername = RegistrationActivity.serverName;
    public static int connServerPort = 7660;
    private Toolbar toolbar;
    private static SharedPreferences spref_ip;

    public static String filename;
    public int PortVdo = 7668;

    public static final byte BYTE_START_VIDEO_DOWNLOAD = 14;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        context = getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#F7E7CE"));

        System.out.println("........................servername  " + RegistrationActivity.serverName);

        Intent intent = getIntent();
        String imageName = intent.getStringExtra("image_name");
        jIV = (ImageView) findViewById(R.id.xIV);
        jVV = (VideoView) findViewById(R.id.xVV);
        //jIVFrame = (ImageView) findViewById(R.id.xIVFrame);

        jVV.setMediaController(new MediaController(this));

        videoNotifID = intent.getIntExtra("video_notif_id", -1);
        videoNotifID = videoNotifID%100;
        hashID = intent.getStringExtra("HashID");

        if (videoNotifID != -1) {
            jIV.setImageResource(R.drawable.ic_file_download_24dp);
        }else if (imageName != null) {
            Bitmap frame = getImageBitmap(imageName);
            System.out.println("NOTIF FRAME DISPLAYED :" + imageName);
            jIV.setImageBitmap(frame);

        }

        if(videoNotifID != -1) {
            jIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(LoginActivity.connect == null){
                                    System.out.println(".........................connect thread is null");
                                    // Start login process
                                    SharedPreferences spref = getSharedPreferences(hashID, MODE_PRIVATE);
                                    String user = spref.getString("username", new String());
                                    String pass = spref.getString("password", new String());

                                    Socket socket = new Socket(servername, connServerPort);
                                    InputStream in = socket.getInputStream();
                                    OutputStream out = socket.getOutputStream();
                                    DataInputStream dIn = new DataInputStream(in);
                                    DataOutputStream dOut = new DataOutputStream(out);
                                    dOut.writeUTF(hashID);
                                    dOut.flush();
                                    final int i = in.read();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (i == 5) {
                                                Toast.makeText(context, "System not Registered", Toast.LENGTH_LONG).show();
                                                System.out.println("............System not registered.............");
                                            } else if (i == 1) {
                                                Toast.makeText(context, "System already Registered", Toast.LENGTH_SHORT).show();
                                                System.out.println("............System already registered.............");
                                            }
                                        }
                                    });

                                    dOut.writeUTF(user);
                                    dOut.flush();
                                    dOut.writeUTF(pass);
                                    dOut.flush();
                                    in.read();
                                    in.read();
                                }else {
                                    if (!LoginActivity.connect.isAlive()) {
                                        System.out.println(".........................connect thread isn't alive");
                                        // Start login process
                                        SharedPreferences spref = getSharedPreferences(hashID, MODE_PRIVATE);
                                        String user = spref.getString("username", new String());
                                        String pass = spref.getString("password", new String());

                                        Socket socket = new Socket(servername, connServerPort);
                                        InputStream in = socket.getInputStream();
                                        OutputStream out = socket.getOutputStream();
                                        DataInputStream dIn = new DataInputStream(in);
                                        DataOutputStream dOut = new DataOutputStream(out);
                                        dOut.writeUTF(hashID);
                                        dOut.flush();
                                        final int i = in.read();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (i == 5) {
                                                    Toast.makeText(context, "System not Registered", Toast.LENGTH_LONG).show();
                                                    System.out.println("............System not registered.............");
                                                } else if (i == 1) {
                                                    Toast.makeText(context, "System already Registered", Toast.LENGTH_SHORT).show();
                                                    System.out.println("............System already registered.............");
                                                }
                                            }
                                        });

                                        dOut.writeUTF(user);
                                        dOut.flush();
                                        dOut.writeUTF(pass);
                                        dOut.flush();
                                        in.read();
                                        in.read();
                                    }
                                }

                                // Start Video downloading process

                                while(!LivefeedFragment.sendMsg(BYTE_START_VIDEO_DOWNLOAD)){}
                                /*SocketAddress address = new InetSocketAddress(servername,PortVdo);
                                SocketChannel clientChannel = SocketChannel.open(address);
                                Socket socketVdo = clientChannel.socket();*/
                                System.out.println("Starting Video download");
                                Socket socketVdo = new Socket(servername, PortVdo);
                                System.out.println("...vdo socket connected...");
                                OutputStream outVdo = socketVdo.getOutputStream();
                                DataOutputStream doutVdo = new DataOutputStream(outVdo);
                                doutVdo.writeUTF(hashID);
                                doutVdo.flush();
                                doutVdo.writeInt(videoNotifID);
                                doutVdo.flush();
                                System.out.println("Hash ID : " + hashID + " Video Notif ID : " + videoNotifID);


                                InputStream inVdo = socketVdo.getInputStream();
                                DataInputStream dInVdo = new DataInputStream(inVdo);
                                int filenameSize = dInVdo.readInt();
                                outVdo.write(1);
                                outVdo.flush();
                                System.out.println("File Size : " + filenameSize);
                                byte[] filenameInBytes = new byte[filenameSize];
                                inVdo.read(filenameInBytes);
                                filename = new String(filenameInBytes);
                                System.out.println("FILENAME RECIEVED :"  + filename);
                                outVdo.write(1);
                                outVdo.flush();

                                final File vdoDirectory = new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyeVideos");
                                SharedPreferences spref_folder = getSharedPreferences(hashID,MODE_PRIVATE);
                                String folderName = spref_folder.getString("name","Default");
                                System.out.println(".....product folder name = " + folderName);
                                final File specificVdoDir = new File(vdoDirectory.getPath(),folderName);
                                if (!specificVdoDir.exists()) {
                                    if (!specificVdoDir.mkdirs()) {
                                        Log.d("App", "failed to create video directory");
                                    }
                                }

                                final String filepath = specificVdoDir.getPath() + "/" + filename;
                                FileOutputStream fileOut = new FileOutputStream(filepath);
                                //FileOutputStream fileOut = openFileOutput(filename, MODE_PRIVATE);
                                byte[] buffer = new byte[16 * 1024];
                                int count;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        jIV.setImageResource(R.drawable.ic_schedule_24dp);
                                    }
                                });
                                while ((count = inVdo.read(buffer)) > 0) {
                                    fileOut.write(buffer, 0, count);
                                }
                                fileOut.close();
                                socketVdo.close();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Download successful.", Toast.LENGTH_LONG).show();
                                        jIV.setVisibility(View.GONE);
                                        //String filepath = filename;

                                        jVV.setVideoPath(filepath);
                                        jVV.setVisibility(View.VISIBLE);
                                        jVV.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                jVV.start();
                                            }
                                        });
                                    }
                                });
                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Download failed.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }

       /* if (imageName != null){
            Bitmap frame = getImageBitmap(getApplicationContext(), imageName);
            //jIVFrame.setImageBitmap(frame);
        }*/
    }

    public Bitmap getImageBitmap(String name){
        final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyePictures");
        name=imageStorageDir.getPath() + "/" + name +".jpg";
        try{
            //FileInputStream fis = context.openFileInput(name);
            FileInputStream fis = new FileInputStream(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }
}
