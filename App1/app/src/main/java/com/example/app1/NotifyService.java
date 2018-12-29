package com.example.app1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sibhali on 12/19/2016.
 */
public class NotifyService extends FirebaseMessagingService {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;
    public static final byte BYTE_FACEFOUND_VDOGENERATING = 1, BYTE_FACEFOUND_VDOGENERATED = 2, BYTE_ALERT1 = 3, BYTE_ALERT2 = 4 , BYTE_ABRUPT_END =5, BYTE_LIGHT = 6, BYTE_CAMERA_INACTIVE = 7;

    NotifyServiceReceiver notifyServiceReceiver;

    private static int MY_NOTIFICATION_ID;
    public static int NotifByte;
    private static int MY_VIDEO_NOTIFICATION_ID;

    public static String servername ;
    private NotificationManager notificationManager;

    private static Thread t;
    private static SharedPreferences spref_ip;

    public static DatabaseHandler db;

    public Bitmap notifFrame;
    public static int PORT_NOTIF=7667, PORT_NOTIF_FRAME=7669, PORT_NOTIF_VIDEO=7668;

    public JSONObject json;
    public RemoteMessage remoteMessage;
    public static String recvdHashID, corresProduct;
    public static int serialNo;
    public static volatile boolean notifStatus;

    //public NotifyService(){    }

    @Override
    public void onCreate() {
        //notifyServiceReceiver = new NotifyServiceReceiver();
        db = new DatabaseHandler(getApplicationContext());
        super.onCreate();
        System.out.println("...........NOTIF SERVICE ON CREATE...........");
    }

    @Override
    public void onMessageReceived(RemoteMessage rM){
        remoteMessage = rM;
        json = new JSONObject(remoteMessage.getData());
        try {
            System.out.println("....notif received! " + json.getInt("NotifByte"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final Context context = getApplicationContext();
        String notifTitle = "Something's happening at your door! : "+ corresProduct ;
        final String notifText = "Generating video...";

        String notifVdoTitle = "Something's happening at your door! Video generated. : "+corresProduct;
        String notifVdoText = "Tap to watch video.";
        final String lightTitle = "Lights have changed : "+ corresProduct;
        final String camTitle = "Camera Inactive alert : ";

        final NotificationCompat.Builder notifBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif).setContentTitle(notifTitle).setContentText(notifText).setAutoCancel(true);
        final NotificationCompat.Builder notifVdoBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_video).setContentTitle(notifVdoTitle).setContentText(notifVdoText).setAutoCancel(true);
        final NotificationCompat.Builder lightBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_light).setContentTitle(lightTitle).setAutoCancel(true);
        final NotificationCompat.Builder camBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_light).setContentTitle(camTitle).setAutoCancel(true);

        notifBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notifVdoBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        lightBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        camBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Toast.makeText(this, "Notification service started", Toast.LENGTH_LONG).show();
        //final Handler handler = new Handler();

        t = new Thread(new Runnable() {
            @Override
            public void run(){

                System.out.println("From: " + remoteMessage.getFrom());
                notifFrame = null;

                String imageName = null;
                String _name = null;

                try {
                    servername = RegistrationActivity.serverName;
                    /*Socket client = new Socket(servername, 6667);
                    System.out.println("CONNECTED "+"to " + servername);
                    InputStream in = client.getInputStream();
                    OutputStream out = client.getOutputStream();
                    int p = in.read();*/
                    //JSONObject json = new JSONObject(remoteMessage.getData());
                    int p = json.getInt("NotifByte");
                    recvdHashID = json.getString("HashId");
                    System.out.println("HashID" + recvdHashID);

                    SharedPreferences spref = getSharedPreferences(recvdHashID,MODE_PRIVATE);
                    corresProduct = spref.getString("name",null);
                    notifStatus = spref.getBoolean("mobNotif",false);
                    serialNo = spref.getInt("serialNo",0);

                    System.out.println("...........Notif Status = "+notifStatus);
                    RegistrationActivity.clickedItem = corresProduct;
                    LoginActivity.clickedProductHashID = recvdHashID;

                    /*for (Product product : RegistrationActivity.allProducts) {
                        if((product.getHashID()).equals(recvdHashID)) {
                            corresProduct = product.getName();
                        }
                    }*/
                    /*out.write(1);
                    out.flush();*/
                    System.out.println("NOTIF  RECIEVED: "+ String.valueOf(p));

                    Intent firstNotifIntent = new Intent(context, NotifActivity.class);
                    Intent secondNotifIntent = new Intent(context, NotifActivity.class);
                    firstNotifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    secondNotifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    if(p==BYTE_ALERT1)
                    {
                        notifBuilder.setContentTitle("Alert level1 : "+corresProduct);
                    }
                    if(p == BYTE_FACEFOUND_VDOGENERATED)
                    {
                        notifVdoBuilder.setContentTitle("Face Found.Video generated : "+corresProduct);
                        _name = "Face Found.Video generated";
                        //String datenow = DatabaseRow.dateFormat.format(DatabaseRow.date);
                        //String datenow = getCurrentTimeStamp();
                        //ActivityLogFragment.db .addContact(new DatabaseRow(DatabaseRow._name,datenow));
                    }
                    if(p == BYTE_ALERT2)
                    {
                        _name = "Suspicious activity. Alert level 2";
                        notifVdoBuilder.setContentTitle("Suspicious activity.Video generated : "+corresProduct);
                    }
                    if( p == BYTE_ABRUPT_END){
                        _name = "Abrupt end of activity.";
                    }
                    if(p==BYTE_FACEFOUND_VDOGENERATING || p== BYTE_ALERT1) {
                        /*Socket socketFrame = new Socket(servername, PORT_NOTIF_FRAME);
                        System.out.println("............FRAME SOCKET........");
                        InputStream inFrame = socketFrame.getInputStream();
                        final Bitmap notifFrame = BitmapFactory.decodeStream(new FlushedInputStream(inFrame));
                        socketFrame.close();
                        System.out.println("...........frame bhetli.....");

                        imageName = getCurrentTimeStamp();
                        saveImage(notifFrame, imageName);

                        firstNotifIntent.putExtra("image_name", imageName);*/

                        int requestID = (int) System.currentTimeMillis();
                        PendingIntent firstPendingIntent = PendingIntent.getActivity(context, requestID, firstNotifIntent, 0);
                        notifBuilder.setContentIntent(firstPendingIntent);

                        /*NotificationCompat.BigPictureStyle bps = new NotificationCompat.BigPictureStyle().bigPicture(notifFrame);
                        notifBuilder.setStyle(bps);*/


                        /*if (NotifActivity.jIV != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    NotifActivity.jIV.setImageBitmap(notifFrame);
                                }
                            });
                        }*/
                    }
                    if(p== BYTE_FACEFOUND_VDOGENERATED || p ==BYTE_ALERT2 || p == BYTE_ABRUPT_END || p == BYTE_LIGHT){
                        /*DataInputStream dataInputStream = new DataInputStream(in);
                        String _date = dataInputStream.readUTF();*/
                        String _date = json.getString("date");
                        if(p == BYTE_LIGHT){
                            _name = lightTitle;
                            db.addRow(new DatabaseRow(_name,_date, 0 , null, recvdHashID));
                        }else {
                            db.addRow(new DatabaseRow(_name, _date, 0, imageName, recvdHashID));
                        }
                    }
                    MY_NOTIFICATION_ID = json.getInt("NotifId");
                    MY_NOTIFICATION_ID = (serialNo*100)+MY_NOTIFICATION_ID;

                    System.out.println("serial no :" + serialNo);
                    System.out.println("NOTIFICATION ID :" + MY_NOTIFICATION_ID);

                    /*out.write(9);
                    out.flush();
                    client.close();*/

                    if((p == BYTE_FACEFOUND_VDOGENERATING || p == BYTE_ALERT1) && notifStatus )
                    {
                        notificationManager.notify(MY_NOTIFICATION_ID, notifBuilder.build());

                    }else if(!notifStatus)
                        System.out.println("First Notif not build, NOTIF STATUS : " + notifStatus);

                    if ((p == BYTE_FACEFOUND_VDOGENERATED || p == BYTE_ALERT2 || p == BYTE_ABRUPT_END)&& notifStatus) {

                        secondNotifIntent.putExtra("video_notif_id", MY_NOTIFICATION_ID);
                        secondNotifIntent.putExtra("HashID",recvdHashID);

                        int requestID = (int) System.currentTimeMillis();
                        PendingIntent secondPendingIntent = PendingIntent.getActivity(context, requestID, secondNotifIntent, 0);
                        notifVdoBuilder.setContentIntent(secondPendingIntent);

                        notificationManager.notify(MY_NOTIFICATION_ID, notifVdoBuilder.build());
                        System.out.println("NOTIF 2nd GIVEN");

                    }else if(!notifStatus)
                        System.out.println("2nd notif not build, NOTIF STATUS : " + notifStatus );


                    if(p == BYTE_LIGHT && notifStatus){
                        notificationManager.notify(MY_NOTIFICATION_ID,lightBuilder.build());
                        //_name = lightTitle;
                        //String date = DatabaseRow.dateFormat.format(new Date());
                        //db.addRow(new DatabaseRow(_name,date, 0 , null));
                    }else if(!notifStatus)
                        System.out.println("Light change notif not build, NOTIF STATUS : " + notifStatus);

                    if(p == BYTE_CAMERA_INACTIVE){
                       camBuilder.setContentTitle(camTitle + corresProduct);
                        notificationManager.notify(MY_NOTIFICATION_ID,camBuilder.build());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /////////FCM wala code

                if (remoteMessage == null)
                    return;

            }
        });
        t.start();
    }


    /*@Override
    public void onDestroy() {
        Log.d("DESTROYEDDD!", "HAHAHA!");
        Toast.makeText(NotifyService.this, "Notification service stopped", Toast.LENGTH_SHORT).show();
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }*/
    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    private class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int rqs = intent.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
            }
        }
    }

    public void saveImage( Bitmap b, String name){
        final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyePictures");
        name=imageStorageDir.getPath() + "/" + name +".jpg";
        FileOutputStream out;
        try {
            //out = context.openFileOutput(name, Context.MODE_PRIVATE);
            out = new FileOutputStream(name);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
