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
import android.widget.EditText;
import android.widget.Toast;

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

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.input_username) EditText _usernameText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ButterKnife.inject(this);
        loggedIn = PreferenceManager.getDefaultSharedPreferences(this);
        boolean check = loggedIn.getBoolean("auto_login",false);
        if(check) {
            _loginButton.setEnabled(true);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
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

        if (!validate()) {
            onLoginFailed();
            return;
        }

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
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if(username.isEmpty() || username.length() < 4 || username.length() > 10){
            _usernameText.setError("between 4 and 10 alphabets");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
