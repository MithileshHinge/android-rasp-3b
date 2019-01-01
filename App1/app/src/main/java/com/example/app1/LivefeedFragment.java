package com.example.app1;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public byte[] buffer;
    public static int p;
    public static DatagramSocket AudioSocket;
    private int AudioPort = 7671, AudioTcpPort = 7670;

    static AudioRecord recorder;
    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize;
    private boolean status = false;
    private static String servername;
    private Socket handshake_socket;
    private static SharedPreferences spref_ip;

    private static int msgPort = 7676;
    public static final byte BYTE_STOP_ALARM = 8, BYTE_START_ALARM = 7, BYTE_START_LIVEFEED=2, BYTE_START_AUDIO=13;

    public static ToggleButton Alarm_button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.livefeed_fragment,container,false);
        getActivity().setTitle("Live Feed");
        img = (ImageView) v.findViewById(R.id.imageView);
        Button photo_button = (Button)  v.findViewById(R.id.push_button);
        ToggleButton Voice_button = (ToggleButton) v.findViewById(R.id.Voice_button);
        Alarm_button = (ToggleButton) v.findViewById(R.id.Alarm_button);
        Voice_button.setChecked(false);
        Alarm_button.setChecked(false);

        servername = RegistrationActivity.serverName;
        System.out.println("........................servername  " + servername);

        t = new Client();
        t.start();
        final Handler handler = new Handler();


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (frameChanged) {
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
                final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyePictures");

                final File specificImgStorageDir = new File(imageStorageDir.getPath(),RegistrationActivity.clickedItem);
                if (!specificImgStorageDir.exists()) {
                    if (!specificImgStorageDir.mkdirs()) {
                        Log.d("App", "failed to create video directory");
                    }
                }

                SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss");
                Date date = new Date();
                final String finalImageFileName = specificImgStorageDir.getPath() + "/" + date + ".jpg";
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
                if(isChecked){
                    status=true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                System.out.println(".............voice button clicked...!!!");
                                while (!LivefeedFragment.sendMsg(BYTE_START_AUDIO)){}
                                handshake_socket = new Socket(servername, AudioTcpPort);
                                System.out.println(".............audio tcp port connected...!!!");
                                OutputStream out = handshake_socket.getOutputStream();
                                out.write(1);
                                out.flush();
                                System.out.println("P=1 PATHAVLA");
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


                }else{
                    System.out.println("STOP BUTTON");
                    Toast.makeText(v.getContext(), "Recording stopped !", Toast.LENGTH_SHORT).show();
                    status = false;
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

        Alarm_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {

                if(isChecked){
                    System.out.println(".................ALARM BUTTON PRESSED............");
                    blowAlarmDialogBox();
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!LivefeedFragment.sendMsg(BYTE_STOP_ALARM)){}
                            System.out.println("....alarm off");
                        }
                    });

                }
            }
        });

        return v;
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
                });
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

                    while (status)
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
        t.end();
        if(status) {
            status = false;
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
        super.onPause();
    }

    public static boolean sendMsg(int p){
        Socket msgSocket;
        try {
            //spref_ip = PreferenceManager.getDefaultSharedPreferences(this);
            //servername = spref_ip.getString("ip_address","");
            System.out.println(".........into send msg................. with servername = "+ RegistrationActivity.serverName);
            msgSocket = new Socket(RegistrationActivity.serverName, msgPort);
            OutputStream out =  msgSocket.getOutputStream();
            InputStream in = msgSocket.getInputStream();
            out.write(p);
            out.flush();
            in.read();
            System.out.println(".............byte sent : " + p);
            try{
                msgSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
