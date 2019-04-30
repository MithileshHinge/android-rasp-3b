package com.example.app1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import static android.support.v7.preference.R.id.list;

/**
 * Created by Sibhali on 12/19/2016.
 */
public class NotifyService extends FirebaseMessagingService {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;
    public static final byte BYTE_FACEFOUND_VDOGENERATING = 1,
            BYTE_FACEFOUND_VDOGENERATED = 2,
            BYTE_ALERT1 = 3,
            BYTE_ALERT2 = 4,
            BYTE_ABRUPT_END =5,
            BYTE_LIGHT = 6,
            BYTE_CAMERA_INACTIVE = 7,
            BYTE_MEMORY_ALERT = 8;

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
    public static String recvdHashID, corresProduct, lightTitle;
    public static int serialNo;
    public static volatile boolean notifStatus;
    //List<Integer> notifIDList = new ArrayList<>();
    Set<String> notifIDList;
    StringBuilder str;
    public static SharedPreferences spref_notifs;



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
        final String notifText = "Generating video...";
        final String notifVdoText = "Tap to watch video.";

        //final String memoryText = "Check out on the memory left on your Magic Eye system";

        final NotificationCompat.Builder notifBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif).setContentText(notifText).setAutoCancel(true);
        final NotificationCompat.Builder notifVdoBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_video).setContentText(notifVdoText).setAutoCancel(true);
        final NotificationCompat.Builder lightBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_light).setAutoCancel(true);
        final NotificationCompat.Builder camBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_light).setAutoCancel(true);
        final NotificationCompat.Builder memoryBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_light).setAutoCancel(true);

        notifBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notifVdoBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        lightBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        camBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        memoryBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //activityFrame = new HashMap<>();

        //Toast.makeText(this, "Notification service started", Toast.LENGTH_LONG).show();
        //final Handler handler = new Handler();

        t = new Thread(new Runnable() {
            @Override
            public void run(){

                System.out.println("From: " + remoteMessage.getFrom());
                String _name = null;
                String _date = null;
                Long time = null;

                try {
                    servername = RegistrationActivity.serverName;
                    int p = json.getInt("NotifByte");
                    time = json.getLong("time");
                    recvdHashID = json.getString("HashId");
                    MY_NOTIFICATION_ID = json.getInt("NotifId");
                    System.out.println("HashID" + recvdHashID);

                    //Add notif ID to array list if its not already into array list

                    spref_notifs = getSharedPreferences("notifIDList",MODE_PRIVATE);
                    SharedPreferences.Editor edit = spref_notifs.edit();
                    notifIDList = spref_notifs.getStringSet("notifIDList", new HashSet<String>());
                    System.out.println("notifIDList : "+notifIDList.toString());
                    if(! (p==BYTE_CAMERA_INACTIVE | p==BYTE_MEMORY_ALERT | p==BYTE_LIGHT) ) {
                        str = new StringBuilder();
                        str.append(MY_NOTIFICATION_ID);
                        if (notifIDList.contains(str.toString())) {
                            // Notification ID is received for the second time
                            System.out.println("..........2nd entry in array list "+MY_NOTIFICATION_ID);
                            notifIDList.remove(str.toString());
                            edit.putStringSet("notifIDList",notifIDList);
                            edit.apply();
                            if (p == BYTE_ALERT1 | p == BYTE_FACEFOUND_VDOGENERATING) {
                                return;
                            }
                        } else {
                            // Notification ID is received for the first time
                            System.out.println("..........1st entry in array list "+MY_NOTIFICATION_ID);
                            str = new StringBuilder();
                            str.append(MY_NOTIFICATION_ID);
                            notifIDList.add(str.toString());
                            System.out.println("NOTIF ID LIST" + notifIDList);
                            edit.putStringSet("notifIDList",notifIDList);
                            edit.apply();
                        }
                    }

                    SharedPreferences spref = getSharedPreferences(recvdHashID,MODE_PRIVATE);
                    corresProduct = spref.getString("name",null);
                    System.out.println("Product name: " + corresProduct);
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
                        notifBuilder.setContentTitle(corresProduct + ": Alert level1.");
                    if(p==BYTE_FACEFOUND_VDOGENERATING)
                        notifBuilder.setContentTitle(corresProduct + ": Something's happening at your door!");
                    if(p == BYTE_FACEFOUND_VDOGENERATED)
                    {
                        _name = "Face Found.";
                        notifVdoBuilder.setContentTitle(corresProduct + ": Face Found.");
                    }
                    if(p == BYTE_ALERT2)
                    {
                        _name = "Activity occurred.";
                        notifVdoBuilder.setContentTitle(corresProduct + ": Activity occurred.");
                    }
                    if( p == BYTE_ABRUPT_END){
                        _name = "Abrupt end of activity.";
                        notifVdoBuilder.setContentTitle(corresProduct + ": Abrupt end of activity.");
                    }
                    if(p == BYTE_LIGHT){
                        _name = "Lights have changed.";
                        lightBuilder.setContentTitle(corresProduct + ": Lights have changed.");
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
                        notifBuilder.setStyle(bps);


                        if (NotifActivity.jIV != null) {
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
                        _date = json.getString("date");

                        String frameString = json.getString("Frame");
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(frameString, Base64.DEFAULT));
                        final Bitmap notifFrame = BitmapFactory.decodeStream(inputStream);
                        inputStream.reset();
                        String thumbpath;
                        if(notifFrame == null){
                            System.out.println("NOTIF FRAME NULL...........");
                            thumbpath = null;
                        }else
                            thumbpath = saveImage(context,notifFrame, _date);
                        db.addRow(new DatabaseRow(_name, _date, 0, thumbpath, recvdHashID));
                        System.out.println("NOTIFY SERVICE THUMBPATH : " + thumbpath );

                    }

                    MY_NOTIFICATION_ID = (serialNo*100)+MY_NOTIFICATION_ID;
                    System.out.println("NOTIFICATION ID :" + MY_NOTIFICATION_ID);

                    if((p == BYTE_FACEFOUND_VDOGENERATING || p == BYTE_ALERT1) && notifStatus ) {
                        notifBuilder.setWhen(time);
                        notificationManager.notify(MY_NOTIFICATION_ID, notifBuilder.build());
                    }

                    if ((p == BYTE_FACEFOUND_VDOGENERATED || p == BYTE_ALERT2 || p == BYTE_ABRUPT_END)&& notifStatus) {

                        secondNotifIntent.putExtra("video_notif_id", MY_NOTIFICATION_ID);
                        secondNotifIntent.putExtra("HashID",recvdHashID);

                        int requestID = (int) System.currentTimeMillis();
                        PendingIntent secondPendingIntent = PendingIntent.getActivity(context, requestID, secondNotifIntent, 0);
                        notifVdoBuilder.setContentIntent(secondPendingIntent);
                        notifVdoBuilder.setWhen(time);

                        notificationManager.notify(MY_NOTIFICATION_ID, notifVdoBuilder.build());
                        System.out.println("NOTIF 2nd GIVEN");

                    }else if(!notifStatus)
                        System.out.println("2nd notif not build, NOTIF STATUS : " + notifStatus );


                    if(p == BYTE_LIGHT && notifStatus){
                        notificationManager.notify(MY_NOTIFICATION_ID,lightBuilder.build());

                    }else if(!notifStatus)
                        System.out.println("Light change notif not build, NOTIF STATUS : " + notifStatus);

                    if(p == BYTE_CAMERA_INACTIVE){
                       camBuilder.setContentTitle(corresProduct + ": Camera Inactive alert" );
                        notificationManager.notify(MY_NOTIFICATION_ID,camBuilder.build());

                    }
                    if(p == BYTE_MEMORY_ALERT){
                        int memoryspace = json.getInt("%memory");
                        if(memoryspace == 90) {
                            memoryBuilder.setContentTitle(corresProduct + ": Low storage space alert");
                            memoryBuilder.setContentText("Your storage space is " + memoryspace + "% full. ");
                        }
                        else {
                            memoryBuilder.setContentTitle(corresProduct + ": Memory Alert");
                            memoryBuilder.setContentText("Your storage space was " + memoryspace + "% full. ");
                        }
                        notificationManager.notify(MY_NOTIFICATION_ID,memoryBuilder.build());
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

    /*public String saveImage( Bitmap b, String name){
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisPictures");
        imageStorageDir = new File(imageStorageDir.getPath(),RegistrationActivity.clickedItem);
        name=imageStorageDir.getPath() + "/" + name +".jpg";
        FileOutputStream out;
        try {
            //out = context.openFileOutput(name, Context.MODE_PRIVATE);
            out = new FileOutputStream(name);
            b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (NullPointerException e){
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Returning thumbpath saveimage");
        return name;
    }*/

    //save image internally
    public String saveImage(Context context, Bitmap b, String name){
        name = name + ".jpg";
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Returning thumbpath saveimage");
        return name;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
