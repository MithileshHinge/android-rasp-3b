package com.example.app1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    public static Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public static TextView mode,username;
    public static Context context;
    public static SharedPreferences spref_mode,spref_user;
    public static BookmarkedDatabaseHandler bookmarkedDatabaseHandler;
    private static String  TAG = "Permsission : ";
    private static boolean write_permission;
    public static String serverName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        serverName = RegistrationActivity.serverName;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityLogFragment activityLogFragment = new ActivityLogFragment();
        android.support.v4.app.FragmentTransaction activityFragmentTransaction = getSupportFragmentManager().beginTransaction();
        activityFragmentTransaction.replace(R.id.frame, activityLogFragment);
        activityFragmentTransaction.commit();

        startService(new Intent(this,NotifyService.class));

        spref_mode = PreferenceManager.getDefaultSharedPreferences(this);
        spref_user = PreferenceManager.getDefaultSharedPreferences(this);

        context = getApplicationContext();

        bookmarkedDatabaseHandler = new BookmarkedDatabaseHandler(context);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#F7E7CE"));

        write_permission  = isStoragePermissionGranted() ;
        System.out.println("write permission status is: " + write_permission);
        if(write_permission) {
            //Creating Magic Eye Directory in Internal Storage
            final File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Arvis");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create media directory");
                }
            }
            final File videoStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisVideos");
            if (!videoStorageDir.exists()) {
                if (!videoStorageDir.mkdirs()) {
                    Log.d("App", "failed to create video directory");
                }
            }
            final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisPictures");
            if (!imageStorageDir.exists()) {
                if (!imageStorageDir.mkdirs()) {
                    Log.d("App", "failed to create picture directory");
                }
            }
        }


        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.livefeed);
        item.setEnabled(LoginActivity.livefeedDrawer);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {


                    //Replacing the main content with LivefeedFragment Which is our Inbox View;
                    case R.id.livefeed:
                        //Toast.makeText(getApplicationContext(), "Starting Live Feed", Toast.LENGTH_SHORT).show();
                        LivefeedFragment livefeedFragment = new LivefeedFragment();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, livefeedFragment);
                        fragmentTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click

                    case R.id.recordings:
                        Toast.makeText(getApplicationContext(), "Recordings", Toast.LENGTH_SHORT).show();
                        RecordingFragment recording = new RecordingFragment();
                        android.support.v4.app.FragmentTransaction recordingTransaction = getSupportFragmentManager().beginTransaction();
                        recordingTransaction.replace(R.id.frame, recording);
                        recordingTransaction.commit();
                        return true;
                    case R.id.images:
                        Toast.makeText(getApplicationContext(), "Images", Toast.LENGTH_SHORT).show();
                        ImageFragment images = new ImageFragment();
                        android.support.v4.app.FragmentTransaction imgTransaction = getSupportFragmentManager().beginTransaction();
                        imgTransaction.replace(R.id.frame, images);
                        imgTransaction.commit();
                        return true;
                    case R.id.bookmarks:
                        Toast.makeText(getApplicationContext(), "Bookmarks", Toast.LENGTH_SHORT).show();
                        BookmarkFragment bookmarkFragment = new BookmarkFragment();
                        android.support.v4.app.FragmentTransaction bookmarkFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        bookmarkFragmentTransaction.replace(R.id.frame, bookmarkFragment);
                        bookmarkFragmentTransaction.commit();
                        return true;
                    case R.id.activity:
                        Toast.makeText(getApplicationContext(), "Activity Timeline", Toast.LENGTH_SHORT).show();
                        ActivityLogFragment activityLogFragment = new ActivityLogFragment();
                        android.support.v4.app.FragmentTransaction activityFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        activityFragmentTransaction.replace(R.id.frame, activityLogFragment,"ACTIVITY");
                        activityFragmentTransaction.commit();
                        return true;
                    case R.id.settings:
                        //Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
                        SettingsFragment settingsFragment = new SettingsFragment();
                        android.support.v4.app.FragmentTransaction settingsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        settingsFragmentTransaction.replace(R.id.frame, settingsFragment);
                        settingsFragmentTransaction.commit();

                        return true;
                    /*case R.id.extras:
                        Toast.makeText(getApplicationContext(), "extras", Toast.LENGTH_SHORT).show();
                        return true;*/
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }

            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer opens
                mode = (TextView) findViewById(R.id.mode);
                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.clickedProductHashID,MODE_PRIVATE);
                String s = sharedPreferences.getString("modeType","");
                mode.setText(s);
                username = (TextView) findViewById(R.id.username);
                String s1 = sharedPreferences.getString("name","");
                username.setText(s1);

                super.onDrawerOpened(drawerView);

            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.app1/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.app1/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment current = fragmentManager.findFragmentByTag("ACTIVITY");
        if(current == null){
            ActivityLogFragment activityLogFragment = new ActivityLogFragment();
            android.support.v4.app.FragmentTransaction activityFragmentTransaction = getSupportFragmentManager().beginTransaction();
            activityFragmentTransaction.replace(R.id.frame, activityLogFragment,"ACTIVITY");
            activityFragmentTransaction.commit();
        }else{
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            System.out.println("waiting to create folders");
            //Creating Magic Eye Directory in Internal Storage
            final File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Arvis");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create media directory");
                }
            }
            final File videoStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisVideos");
            if (!videoStorageDir.exists()) {
                if (!videoStorageDir.mkdirs()) {
                    Log.d("App", "failed to create video directory");
                }
            }
            final File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("Arvis"), "ArvisPictures");
            if (!imageStorageDir.exists()) {
                if (!imageStorageDir.mkdirs()) {
                    Log.d("App", "failed to create picture directory");
                }
            }
        }
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;

            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;

        }
    }
    public void logoutClicked(View view) {
        SharedPreferences loggedIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loggedIn.edit().putBoolean("auto_login",false).apply();
        Toast.makeText(view.getContext(), "Logged out", Toast.LENGTH_SHORT).show();
        try {
            LoginActivity.connServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
        finish();
    }
}

