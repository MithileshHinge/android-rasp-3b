package com.example.app1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;


import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by Home on 19-07-2017.
 */
public class LivefeedFragment extends Fragment {

    //public static Button photo_button;
    public static ImageView img;
    public static boolean frameChanged = false;
    public static Bitmap frame = null;
    private static Client t;
    private static Listen listen;

    public byte[] buffer;
    public static int p;
    public static DatagramSocket AudioSocket;
    private int AudioPort = 7671, AudioTcpPort = 7670 ;

    static AudioRecord recorder;
    private int sampleRate = 44100;
    public static int volume;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize;
    private boolean voice_status = false;
    public volatile boolean audioReceived;
    private static String servername;
    private Socket handshake_socket;
    private static SharedPreferences spref_ip;
    private ProgressDialog progressDialg;

    private static int msgPort = 7676;
    public static final byte BYTE_STOP_ALARM = 8, BYTE_START_ALARM = 7, BYTE_START_LIVEFEED=2, BYTE_START_AUDIO=13, BYTE_GET_SYSIP=15;

    public static ToggleButton Alarm_button, Voice_button, Listen_button;

    public static final int REQUEST_MICROPHONE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.livefeed_fragment,container,false);
        getActivity().setTitle("Live Feed");
        img = (ImageView) v.findViewById(R.id.imageView);
        Button photo_button = (Button)  v.findViewById(R.id.push_button);
        Voice_button = (ToggleButton) v.findViewById(R.id.Voice_button);
        Alarm_button = (ToggleButton) v.findViewById(R.id.Alarm_button);
        Listen_button = (ToggleButton) v.findViewById(R.id.Speaker_button);
        Voice_button.setChecked(false);
        Alarm_button.setChecked(false);
        Listen_button.setChecked(false);

        servername = RegistrationActivity.serverName;
        System.out.println("........................servername  " + servername);

        SharedPreferences spref_volume = getContext().getSharedPreferences(LoginActivity.clickedProductHashID, Context.MODE_PRIVATE);
        volume =spref_volume.getInt("volume",5);
        System.out.println("LIVEFEED VOLUME : " + volume);
        volume = volume + 20;

        progressDialg = new ProgressDialog(getContext());
        System.out.println("                 Progress Dialog initiated!!!!!!!!!!!!");
        progressDialg.setIndeterminate(true);
        progressDialg.setMessage("Establishing connection...");
        progressDialg.setCanceledOnTouchOutside(false);
        progressDialg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                System.out.println("ON CANCEL");
                ActivityLogFragment activityLogFragment = new ActivityLogFragment();
                android.support.v4.app.FragmentTransaction activityFragmentTransaction = getFragmentManager().beginTransaction();
                activityFragmentTransaction.replace(R.id.frame, activityLogFragment,"ACTIVITY").commit();
            }
        });
        progressDialg.show();

        t = new Client();
        t.start();
        listen = new Listen();
        final Handler handler = new Handler();


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (frameChanged) {
                        if(progressDialg.isShowing()){
                            System.out.println("                     Progress dialog running");
                            progressDialg.dismiss();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageBitmap(frame);
                            }
                        });
                        frameChanged = false;
                    }
                }
            }
        });
        t2.start();


        photo_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "photo clicked", Toast.LENGTH_SHORT).show();
                final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisPictures");
                final File specificImgStorageDir = new File(imageStorageDir.getPath(),RegistrationActivity.clickedItem);
                if (!specificImgStorageDir.exists()) {
                    if (!specificImgStorageDir.mkdirs()) {
                        Log.d("App", "failed to create video directory");
                    }
                }

                SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss");
                Date date = new Date();
                final String finalImageFileName = specificImgStorageDir.getPath() + "/" + ft.format(date) + ".jpg";
                if(frame != null) {
                    try {
                        FileOutputStream fos = new FileOutputStream(finalImageFileName);
                        frame.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found");
                    } catch (IOException e) {
                        System.out.println("Error accessing file");
                    }
                }
            }
        });



        Voice_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
                        return;
                    }
                    voice_status = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(".............voice button clicked...!!!");
                                while (!LivefeedFragment.sendMsg(BYTE_START_AUDIO)) {
                                }
                                handshake_socket = new Socket(servername, AudioTcpPort);
                                System.out.println(".............audio tcp port connected...!!!");
                                /*OutputStream out = handshake_socket.getOutputStream();
                                out.write(1);
                                out.flush();*/
                                String hashId = LoginActivity.clickedProductHashID;
                                DataOutputStream dout = new DataOutputStream(handshake_socket.getOutputStream());
                                dout.writeUTF(hashId);
                                dout.flush();
                                System.out.println("HASH ID PATHAVLI");
                                InputStream in = handshake_socket.getInputStream();
                                p = in.read();
                                System.out.println("Tyani p pathavla P =" + p);
                                //handshake_socket.close();
                                System.out.println(".........HANDSHAKE SOCKET BANDA.....");

                                if (p == 2) {
                                    p = 0;
                                    startStreaming();
                                    System.out.println("......STREAMING START JHALI.....");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                } else {
                    System.out.println("STOP BUTTON");
                    Toast.makeText(v.getContext(), "Recording stopped !", Toast.LENGTH_SHORT).show();
                    voice_status = false;
                    if (recorder != null) {
                        recorder.release();
                        AudioSocket.close();
                        try {
                            handshake_socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        Listen_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    listen = new Listen();
                    listen.start();
                    System.out.println("Listen Started");
                }else{
                    listen.end();
                    System.out.println("Listen Stopped");
                }
            }
        });

        Alarm_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {

                if (isChecked) {
                    System.out.println(".................ALARM BUTTON PRESSED............");
                    blowAlarmDialogBox();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //while (!LivefeedFragment.sendMsg(BYTE_STOP_ALARM)) {}
                            if(LivefeedFragment.sendMsg(BYTE_STOP_ALARM)){
                                //process completed properly
                                System.out.println("...alarm stopped properly");
                            }else{
                                //give error
                                ActivityLogFragment activityLogFragment = new ActivityLogFragment();
                                android.support.v4.app.FragmentTransaction activityFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                activityFragmentTransaction.replace(R.id.frame, activityLogFragment,"ACTIVITY").commit();
                            }
                            System.out.println("....alarm off");
                        }
                    }).start();

                }
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_MICROPHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    voice_status=true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                System.out.println(".............voice button clicked...!!!");
                                while (!LivefeedFragment.sendMsg(BYTE_START_AUDIO)){}
                                handshake_socket = new Socket(servername, AudioTcpPort);
                                System.out.println(".............audio tcp port connected...!!!");
                                /*OutputStream out = handshake_socket.getOutputStream();
                                out.write(1);
                                out.flush();
                                System.out.println("P=1 PATHAVLA");*/
                                String hashId = LoginActivity.clickedProductHashID;
                                DataOutputStream dout = new DataOutputStream(handshake_socket.getOutputStream());
                                dout.writeUTF(hashId);
                                dout.flush();
                                System.out.println("HASH ID PATHAVLI");
                                InputStream in = handshake_socket.getInputStream();
                                p = in.read();
                                System.out.println("Tyani p pathavla P =" + p);
                                //handshake_socket.close();
                                System.out.println(".........HANDSHAKE SOCKET BANDA.....");

                                if (p == 2){
                                    p=0;
                                    startStreaming();
                                    System.out.println("......STREAMING START JHALI.....");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
        }
    }

    public void blowAlarmDialogBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alarm Alert !");
        builder.setMessage("Are you sure you want to blow the Alarm ?");
        builder.create();

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getContext(), "Alarm blown", Toast.LENGTH_LONG).show();
                System.out.println("........yes clicked.....");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!LivefeedFragment.sendMsg(BYTE_START_ALARM)){}
                        System.out.println("....alarm on");
                    }
                }).start();
                LivefeedFragment.Alarm_button.setChecked(true);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getContext(), "Task cancelled", Toast.LENGTH_LONG).show();
                System.out.println("........no clicked.....");
                LivefeedFragment.Alarm_button.setChecked(false);
            }
        });
        builder.show();
    }

    public void startStreaming()
    {
        Thread streamThread = new Thread(new Runnable(){
            @Override
            public void run()
            {
                try{
                    AudioSocket = new DatagramSocket();
                    Log.d("VS", "Socket Created");
                    System.out.println("DataGramSocket BANAVLA!!!!!");
                    //UDP HOLE PUNCHING

                    byte[] handshakeBuf = LoginActivity.clickedProductHashID.getBytes();
                    DatagramPacket handshakePacket = null;
                    try {
                        handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length, InetAddress.getByName(RegistrationActivity.serverName), AudioPort);
                        for(int i=0;i<10;i++) {
                            System.out.println("Sending audio handshake....");
                            AudioSocket.send(handshakePacket);
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                    minBufSize = 4096;
                    buffer = new byte[minBufSize];

                    // Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(servername);
                    Log.d("VS", "Address retrieved");

                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
                    //recorder = findAudioRecord();
                    Log.d("VS", "Recorder initialized");

                    recorder.startRecording();
                    System.out.println("######RECORDING START JHALI");

                    while (voice_status)
                    {
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer,0,buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket(buffer,buffer.length,destination,AudioPort);

                        AudioSocket.send(packet);
                        System.out.println("SENDING DATA");
                    }

                } catch(UnknownHostException e) {
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    Log.e("VS", "IOException");
                    e.printStackTrace();
                }

            }

        });
        streamThread.start();

    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        System.out.println("Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        System.out.println( rate + "Exception, keep trying.");
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }



    @Override
    public void onPause() {
        System.out.println("LIVEFEED ON PAUSE");
        t.end();
        //TODO Listen stopped
        if(listen.isAlive())
            listen.end();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LivefeedFragment.sendMsg(BYTE_STOP_ALARM)) {}
                System.out.println("....alarm off");
            }
        }).start();
        if(voice_status) {
            voice_status = false;
            if (recorder != null) {
                recorder.release();
                AudioSocket.close();
                try {
                    handshake_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Voice_button.setChecked(false);
        super.onPause();
    }

    public static boolean sendMsg(int p){
        Socket msgSocket;
        try {
            System.out.println(".........into send msg................. with servername = "+ RegistrationActivity.serverName);
            msgSocket = new Socket(RegistrationActivity.serverName, msgPort);
            OutputStream out =  msgSocket.getOutputStream();
            InputStream in = msgSocket.getInputStream();
            out.write(p);
            out.flush();
            int r = in.read();
            if(r==0){
                LoginActivity.livefeedDrawer = false;
                sendMsg(p);
                System.out.println("MESSAGE THREAD 0");
              //TODO activity and livefeedoff

            }else if(r==-1){
                //TODO Connection Broken!
                System.out.println("MESSAGE THREAD -1");

            }
            System.out.println(".............byte sent : " + p +"   reply = " + r);
            try{
                msgSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            if(r == -1)
                return false;
            else
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
