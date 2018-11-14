package com.example.app1;

/**
 * Created by isha sagote on 24-01-2018.
 */

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    public static String username;
    public static String password;
    public static SharedPreferences spref_user,spref_email;
    private static SharedPreferences loggedIn;
    boolean check2 = false;
    //public static boolean toBeVerified = false , createFile;    //toBeVerified is set true when user logs out from app
    public static boolean createFile;
    public static int i;
    public static Socket connServerSocket;
    public static InputStream in;
    public static OutputStream out;

    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.loggedIn) CheckBox _loggedIn;
    @InjectView(R.id.input_username) EditText _usernameText;

    //TODO : Remove this portion ; just for testing

    //public static boolean validate1Done = false,validate2Done = false;
    public static int validate1Done = 3;    //if registered properly = 1; if wrong registration = 2; else default 3
    public static int connServerPort = 7660;
    String hashID;
    public static String serverName;
    public static long timeDisableLoginStart, timeDisableLoginEnd;
    private Product product;

    //TODO : Remove this portion ; just for testing

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ButterKnife.inject(this);

        //loggedIn = PreferenceManager.getDefaultSharedPreferences(this);
        for (Product product : RegistrationActivity.allProducts) {
            if(product.getName() == RegistrationActivity.clickedItem) {
                hashID = product.getHashID();
                this.product = product;
            }
        }
        System.out.println("reg id = "+ hashID);
        serverName = RegistrationActivity.serverName;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("...connecting server socket...");
                    connServerSocket = new Socket(serverName,connServerPort);
                    // Sending Reg ID
                    DataOutputStream dOut = new DataOutputStream(connServerSocket.getOutputStream());
                    dOut.writeUTF(hashID);
                    int i = connServerSocket.getInputStream().read();
                    System.out.println("....registration variable = "+i);
                    if(i==5) {
                        System.out.println(".......1........");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "System not registered.", Toast.LENGTH_LONG).show();
                                System.out.println(".......2........");
                            }
                        });
                    } else if(i == 1) {
                        System.out.println(".......3........");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "System already registered.", Toast.LENGTH_LONG).show();
                                System.out.println("............4............");
                            }
                        });

                    }
                    System.out.println("...thread sending reg id ends...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if(product.getIfLoggedIn()){
            _usernameText.setText(product.getUsername());
            _passwordText.setText(product.getPassword());
            _loggedIn.setChecked(true);
        }

        /*loggedIn = getApplicationContext().getSharedPreferences("myPref",0);
        check2 = loggedIn.getBoolean("auto_login",false);
        System.out.println("check boolean of login = "+check2);
        check2 = false;     //just for testing purpose. To be removed!!!
        if(check2) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
        }*/

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if(!checkCredentials()) {
            onLoginFailed();
            return;
        }

        product.setUsername(username);
        product.setPassword(password);

        if(_loggedIn.isChecked())
            product.setIfLoggedIn(true);
        else
            product.setIfLoggedIn(false);


        System.out.println(".....credentials done");
        validate();
        System.out.println(".....validation done");
        while (validate1Done == 3) {
        }
        if (validate1Done == 2)
            return;
        validate1Done = 3;

        System.out.println("...value of validate1Done = "+validate1Done);

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
                    } else
                        System.out.println("...system offline...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
        //Toast.makeText(getBaseContext(), "Connection successfully established !", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(false);

        System.out.println("...authenticating process started...");
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        password = _passwordText.getText().toString();
        username = _usernameText.getText().toString();

        spref_user = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit2 = spref_user.edit();
        edit2.putString("username",username);
        edit2.commit();

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
        password = _passwordText.getText().toString();

        System.out.println("...........Sending username password to server");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("...starting thread to send username and password...");
                //i = 0;
                try {
                    // Sending Username and Password
                    in = connServerSocket.getInputStream();
                    out = connServerSocket.getOutputStream();
                    DataOutputStream dOut = new DataOutputStream(out);
                    dOut.writeUTF(username);
                    dOut.flush();
                    dOut.writeUTF(password);
                    dOut.flush();
                    System.out.println("username and password flushed   "+username+"   "+password);
                    //To be removed!! hard coded fcm reg token!!
                    /*MyFirebaseInstanceIDService.refreshedToken = "cOFUjRSeIyc:APA91bGa-bfU4c5B76Q-LgeZo1OLtYYmuLVVGw-eEBVqyP3-1vx_UYIQ3Nj54jPoxFjs95FGV04h-kMdEjdqW6fflU8UMwakKZ48SRqeo49WP8xJNq9RXmOo64QxNtEWU8tUiNqI_o5n";
                    dOut.writeUTF(MyFirebaseInstanceIDService.refreshedToken);
                    dOut.flush();*/

                    i = in.read();
                    System.out.println("................");
                    /*out.write(0);   //handshake after receiving the registration status from server
                    out.flush();*/

                    //validate1Done = true;
                    System.out.println("....login variable = " + i);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (i == 2) {
                                Toast.makeText(getBaseContext(), "Registered", Toast.LENGTH_LONG).show();
                            } else if (i == 4) {
                                Toast.makeText(getBaseContext(), "Invalid Username or Password. Try Again !", Toast.LENGTH_LONG).show();
                            } else if (i == 3) {
                                disableLogin();
                                Toast.makeText(getBaseContext(), "Maximum number of failed login attempts reached !", Toast.LENGTH_LONG).show();
                            } else if (i == 6) {
                                Toast.makeText(getBaseContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                            } else if (i == 7) {
                                Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    if(i == 2 || i==6)
                        validate1Done = 1;
                    else {
                        onLoginFailed();
                        validate1Done = 2;
                        return;
                    }
                    System.out.println("....thread on its last stage...");
                    /*if (in.read() == 8) {
                        //Toast.makeText(getBaseContext(), "System is offline", Toast.LENGTH_LONG).show();
                        System.out.println("..............System is offline..................");
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }
            }
        }).start();
    }

    public boolean checkCredentials(){
        boolean checked = true;
        username = _usernameText.getText().toString();
        password = _passwordText.getText().toString();

        if (username.isEmpty() || username.length() < 4 || username.length() > 10) {
            _usernameText.setError("between 4 and 10 alphabets");
            System.out.println("...username invalid");
            checked = false;
        } else
            _usernameText.setError(null);

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            System.out.println("...password invalid");
            checked = false;
        } else
            _passwordText.setError(null);

        //System.out.println(".........credentials validated");

        return checked;
    }

    public void disableLogin(){
        timeDisableLoginStart = System.currentTimeMillis();
        timeDisableLoginEnd = timeDisableLoginStart+30000;

        Toast.makeText(getBaseContext(), "Login disabled for 5 mins", Toast.LENGTH_LONG).show();

        _loginButton.setText("Login disabled for 5 mins");
        _loginButton.setEnabled(false);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        System.out.println("timer ended");
                        _loginButton.setText("Login");
                        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                        startActivityForResult(intent, REQUEST_SIGNUP);
                    }
                },30000);

    }

}
