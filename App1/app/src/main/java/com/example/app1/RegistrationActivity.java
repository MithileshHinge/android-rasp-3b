package com.example.app1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by isha sagote on 23-09-2018.
 */

public class RegistrationActivity extends AppCompatActivity {
     private static final String TAG = "RegistrationActivity";
    private static final int REQUEST_SIGNUP = 0;

    public static String regID;

    private static SharedPreferences spref_regID;
    private static SharedPreferences registeredIn;

    private static Socket connServerSocket;
    public int connServerPort = 7660;
    public static SharedPreferences spref_ip;
    public static String serverName = "192.168.1.102";
    public static boolean valid;
    public static boolean validateDone = false;

    @InjectView(R.id.btn_register) Button _registerButton;
    @InjectView(R.id.input_regID) EditText _regID;
    @InjectView(R.id.input_serverName) EditText _serverName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_screen);
        ButterKnife.inject(this);
        registeredIn = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("registration activity started");
        boolean check = registeredIn.getBoolean("auto_login",false);
        System.out.println("check boolean of registration = "+check);
        check = false;      //just for testing purpose. To be removed!!!
        if(check) {
            _registerButton.setEnabled(true);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
        }

        _registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println("clicked on register button");
                register();
            }
        });

    }

    public void register() {
        Log.d(TAG, "Register");

        validate();
        while(!validateDone)   {}
        validateDone = false;

        System.out.println("......registration validated......");
        LoginActivity.createFile = true;
        _registerButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registering...");
        progressDialog.show();
        //regID = _regID.getText().toString();
        spref_regID = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = spref_regID.edit();
        edit.putString("regID",regID);
        edit.commit();

        regID = "2eab13847fe70c2e59dc588f299224aa";

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onRegistrationSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed(){
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
    public void onRegistrationSuccess() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        _registerButton.setEnabled(true);
        registeredIn = PreferenceManager.getDefaultSharedPreferences(this);
        registeredIn.edit().putBoolean("auto_login",true).apply();
        finish();
        System.out.println(".......registration success.....");
    }

    public void onRegistrationFailed() {
        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
        _registerButton.setEnabled(true);
    }

    public void validate() {

        regID = "2eab13847fe70c2e59dc588f299224aa";     // to be removed!! hard coded registration hash id

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connServerSocket = new Socket(serverName, connServerPort);
                    SocketHandler.setSocket(connServerSocket);
                    DataOutputStream dOut = new DataOutputStream(connServerSocket.getOutputStream());
                    dOut.writeUTF(RegistrationActivity.regID);
                    int i = connServerSocket.getInputStream().read();

                    System.out.println("....registration variable = "+i);
                    if(i==5) {
                        System.out.println(".......1........");
                        validateDone = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "System not registered.", Toast.LENGTH_LONG).show();
                                System.out.println(".......2........");
                            }
                        });
                    } else if(i == 1) {
                        System.out.println(".......3........");
                        validateDone = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "System already registered.", Toast.LENGTH_LONG).show();
                                System.out.println("............4............");
                            }
                        });

                    } else
                        _regID.setError(null);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    onRegistrationFailed();
                } catch (IOException e) {
                    e.printStackTrace();
                    onRegistrationFailed();
                }
            }
        }).start();
    }

}
