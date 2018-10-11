package com.example.app1;

/**
 * Created by isha sagote on 24-01-2018.
 */

import android.app.ProgressDialog;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    public static String username;
    public static String password;
    public static String email;
    public static SharedPreferences spref_user,spref_email;
    private static SharedPreferences loggedIn;
    boolean check2 = false;
    public static boolean toBeVerified = false , createFile;    //toBeVerified is set true when user logs out from app
    public static int i;
    private static Socket connServerSocket;
    public static InputStream in;
    public static OutputStream out;

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.input_username) EditText _usernameText;

    //TODO : Remove this portion ; just for testing

    public static boolean validate1Done = false,validate2Done = false;
    public int connServerPort = 7660;
    String regID;
    public static String serverName;

    //TODO : Remove this portion ; just for testing

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ButterKnife.inject(this);
        //loggedIn = PreferenceManager.getDefaultSharedPreferences(this);
        serverName = RegistrationActivity.serverName;
        loggedIn = getApplicationContext().getSharedPreferences("myPref",0);
        check2 = loggedIn.getBoolean("auto_login",false);
        System.out.println("check boolean of login = "+check2);
        check2 = false;     //just for testing purpose. To be removed!!!
        if(check2) {
            _loginButton.setEnabled(true);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
        }

        connServerSocket = SocketHandler.getSocket();
        try {
            in = connServerSocket.getInputStream();
            out = connServerSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException n ){
            n.printStackTrace();
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        validate();
        if(toBeVerified) {
            if (!validate1Done) {
                onLoginFailed();
                return;
            }
        }else {
            while (!validate1Done) {
                System.out.println("..");
            }
        }
        validate1Done = false;

        new Thread(new Runnable() { 
            @Override
            public void run() {
                try {
                    System.out.println("...............New thread within login started..............");
                    if (in.read() == 9) {
                        //Toast.makeText(getBaseContext(), "Connection successfully established !", Toast.LENGTH_LONG).show();
                        System.out.println("Connection successfully established !");
                        while (true) {
                            try {
                                in.read();
                                out.write(1);
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
        //Toast.makeText(getBaseContext(), "Connection successfully established !", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        username = _usernameText.getText().toString();

        spref_user = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit2 = spref_user.edit();
        edit2.putString("username",username);
        edit2.commit();
        spref_email = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit3 = spref_email.edit();
        edit3.putString("email_id",email);
        edit3.commit();
        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                        System.out.println("...............Login success..............");
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
    public void onLoginSuccess() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        _loginButton.setEnabled(true);
        loggedIn = PreferenceManager.getDefaultSharedPreferences(this);
        loggedIn.edit().putBoolean("auto_login",true).apply();
        finish();
        System.out.println("............logged in...........");
    }

    public void onLoginFailed() {
        System.out.println("........in login failed........");
        //Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public void validate() {
        username = _usernameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        while(true) {
            boolean exit = true;
            if (username.isEmpty() || username.length() < 4 || username.length() > 10) {
                _usernameText.setError("between 4 and 10 alphabets");
                exit = false;
            } else {
                _usernameText.setError(null);
            }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _emailText.setError("enter a valid email address");
                exit = false;
            } else {
                _emailText.setError(null);
            }
            if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                _passwordText.setError("between 4 and 10 alphanumeric characters");
                exit = false;
            } else {
                _passwordText.setError(null);
            }
            if(exit) {
                System.out.println(".....formats acceptable.....");
                break;
            }
        }
        if(toBeVerified){
            //TODO - verification process
            System.out.println("Username : " + readusername(getApplicationContext()) + "    Password : " + readpassword(getApplicationContext()));
            System.out.println("Username typed : " + username + "    Password typed: " + password);

            if (username.equals(readusername(getApplicationContext()))) {
                if (password.equals(readpassword(getApplicationContext()))) {
                    validate1Done = true;
                    System.out.println("Validation Done!!!");
                } else {
                    _passwordText.setError("Incorrect Password");
                    System.out.println(".........incorrect password");
                    _passwordText.setText("");
                }
            } else {
                _usernameText.setError("Incorrect Username");
                System.out.println(".........incorrect username");
                _usernameText.setText("");
            }
            _loginButton.setEnabled(true);

        }else {
            if(createFile){
                System.out.println("Writing username password");
                writeusername(username,getApplicationContext());
                writepassword(password,getApplicationContext());
            }
            System.out.println("Sending username password to server");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    i = 0;
                    try {
                        do {
                            DataOutputStream dOut = new DataOutputStream(out);
                            dOut.writeUTF(LoginActivity.username);
                            dOut.flush();
                            dOut.writeUTF(LoginActivity.password);
                            dOut.flush();
                            //To be removed!! hard coded fcm reg token!!
                            MyFirebaseInstanceIDService.refreshedToken = "cOFUjRSeIyc:APA91bGa-bfU4c5B76Q-LgeZo1OLtYYmuLVVGw-eEBVqyP3-1vx_UYIQ3Nj54jPoxFjs95FGV04h-kMdEjdqW6fflU8UMwakKZ48SRqeo49WP8xJNq9RXmOo64QxNtEWU8tUiNqI_o5n";
                            dOut.writeUTF(MyFirebaseInstanceIDService.refreshedToken);
                            dOut.flush();

                            i = in.read();
                            out.write(0);   //handshake after receiving the registration status from server
                            out.flush();

                            validate1Done = true;
                            System.out.println("....login variable = " + i);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (i == 2) {
                                        Toast.makeText(getBaseContext(), "Registered", Toast.LENGTH_LONG).show();
                                    } else if (i == 4) {
                                        Toast.makeText(getBaseContext(), "Invalid Username or Password. Try Again !", Toast.LENGTH_LONG).show();
                                    } else if (i == 3) {
                                        Toast.makeText(getBaseContext(), "Maximum number of failed login attempts reached !", Toast.LENGTH_LONG).show();
                                    } else if (i == 6) {
                                        Toast.makeText(getBaseContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                                    } else if (i == 7) {
                                        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            if (i == 2) {
                                //Toast.makeText(getBaseContext(), "Registered", Toast.LENGTH_LONG).show();
                                break;
                            } else if (i == 4) {
                                //Toast.makeText(getBaseContext(), "Invalid Username or Password. Try Again !", Toast.LENGTH_LONG).show();
                            } else if (i == 3) {
                                //Toast.makeText(getBaseContext(), "Maximum number of failed login attempts reached !", Toast.LENGTH_LONG).show();
                            } else if (i == 6) {
                                //Toast.makeText(getBaseContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                                break;
                            } else if (i == 7) {
                                //Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
                                break;
                            } else {
                                break;
                            }
                            if (in.read() == 8) {
                                //Toast.makeText(getBaseContext(), "System is offline", Toast.LENGTH_LONG).show();
                                System.out.println("..............System is offline..................");
                            }
                        } while (i != 3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException n) {
                        n.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void writeusername(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("username.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private void writepassword(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("password.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readusername(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("username.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    private String readpassword(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("password.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
