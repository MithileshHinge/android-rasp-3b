package com.example.app1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static int i = 1;
    public boolean switched;
    private static int msgPort = 7676;
    private String servername = RegistrationActivity.serverName;
    public SharedPreferences spref_ip;
    byte BYTE_SURV_MODE_ON = 1, BYTE_SURV_MODE_OFF = 3, BYTE_EMAIL_NOTIF_ON = 9, BYTE_EMAIL_NOTIF_OFF = 10;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getActivity().setTitle("Settings");

        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.settings_preferences);

        Preference p2 = getPreferenceScreen().findPreference("key4");
        SharedPreferences spref = getContext().getSharedPreferences(LoginActivity.clickedProductHashID,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = spref.edit();
        if(p2 instanceof ListPreference){
            ListPreference listPref = (ListPreference) p2;
            p2.setEnabled(LoginActivity.livefeedDrawer);
            String mode = spref.getString("modeType",null);
            System.out.println("...........mode = "+mode);
            i=0;
            int index = 10;
            for (CharSequence str : listPref.getEntries()){
                if(str.equals(mode)){
                    index = i;
                }
                i++;
            }
            System.out.println("...........index = "+index);

            if(mode == null){
                ((ListPreference) p2).setValueIndex(0);
            }else{
                ((ListPreference) p2).setValueIndex(index);
            }
            p2.setSummary(listPref.getEntry());
            edit.putString("modeType",listPref.getEntry().toString());
            edit.apply();

        }

        Preference p3 = getPreferenceScreen().findPreference("keyy1");
        if (p3 instanceof EditTextPreference) {
            p3.setSummary(spref.getString("username",""));
        }

        Preference p4 = getPreferenceScreen().findPreference("keyy2");
        if (p4 instanceof EditTextPreference) {
            p4.setSummary(spref.getString("name",""));
        }

        Preference p5 = getPreferenceScreen().findPreference("keyy3");
        if (p5 instanceof EditTextPreference) {
            p5.setSummary(spref.getString("email",""));
        }

        Preference p8 = getPreferenceScreen().findPreference("keyy4");
        if (p8 instanceof EditTextPreference) {
            p8.setSummary(spref.getString("sysLocalIP",""));
        }

        Preference p6 = getPreferenceScreen().findPreference("key1");
        boolean mobNotif = spref.getBoolean("mobNotif",false);
        if(p6 instanceof SwitchPreferenceCompat){
            ((SwitchPreferenceCompat) p6).setChecked(mobNotif);
        }

        Preference p7 = getPreferenceScreen().findPreference("key5");
        p7.setEnabled(LoginActivity.livefeedDrawer);
        boolean emailNotif = spref.getBoolean("emailNotif",false);
        if(p7 instanceof SwitchPreferenceCompat){
            ((SwitchPreferenceCompat) p7).setChecked(emailNotif);
        }

        Preference p1 = getPreferenceScreen().findPreference("key3");
        int volume = spref.getInt("volume",5);

    }
    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            SharedPreferences spref_mode = getContext().getSharedPreferences(LoginActivity.clickedProductHashID,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = spref_mode.edit();
            final ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
            edit.putString("modeType",listPref.getEntry().toString());
            edit.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Servername in settings fragment  : "+ servername);
                        Socket socket = new Socket(servername, msgPort);
                        OutputStream out = socket.getOutputStream();
                        InputStream in = socket.getInputStream();
                        if(listPref.getEntry().toString().equals("Surveillance Mode")) {
                        out.write(BYTE_SURV_MODE_ON);
                        } else out.write(BYTE_SURV_MODE_OFF);
                        in.read();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        /*
        // To DO: send email change request to device and on mail
        if(key.equals("keyy3")) {
            Preference p5 = getPreferenceScreen().findPreference("keyy3");

            if (p5 instanceof EditTextPreference) {
                SharedPreferences spref_email = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor edit3 = spref_email.edit();
                EditTextPreference editTextPref = (EditTextPreference) p5;
                if (p5.getTitle().toString().toLowerCase().contains("Email Id")) {
                    p5.setSummary("******");
                } else {
                    p5.setSummary(editTextPref.getText());
                    edit3.putString("email_id",editTextPref.getText());
                    edit3.commit();
                }
            }

        }*/

        if(key.equals("key1")) {
            Preference p6 = getPreferenceScreen().findPreference("key1");

            if (p6 instanceof SwitchPreferenceCompat) {
                SharedPreferences spref_mobNotif = getContext().getSharedPreferences(LoginActivity.clickedProductHashID,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spref_mobNotif.edit();
                SwitchPreferenceCompat switchPreferenceCompat = (SwitchPreferenceCompat)p6;
                switched = (switchPreferenceCompat).isChecked();
                editor.putBoolean("mobNotif",switched);
                editor.apply();
                if(switched){
                    Toast.makeText(getContext(), "Mobile Notification started", Toast.LENGTH_SHORT).show();
                    //NotifyService.notifStatus = true;
                    System.out.println("NOTIF STATUS : " + NotifyService.notifStatus);
                    System.out.println(".........Notify service started.................");
                }else{
                    //NotifyService.notifStatus = false;
                    System.out.println("NOTIF STATUS : " + NotifyService.notifStatus);
                    Toast.makeText(getContext(), "Mobile Notification stopped", Toast.LENGTH_SHORT).show();
                    /*Intent intent = new Intent();
                    intent.setAction(NotifyService.ACTION);
                    intent.putExtra("RQS", NotifyService.RQS_STOP_SERVICE);
                    getActivity().sendBroadcast(intent);*/
                    System.out.println(".........Notify service stopped.................");
                }

            }

        }

        if(key.equals("key5")) {
            Preference p7 = getPreferenceScreen().findPreference("key5");

            if (p7 instanceof SwitchPreferenceCompat) {
                SharedPreferences spref_emailNotif = getContext().getSharedPreferences(LoginActivity.clickedProductHashID,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spref_emailNotif.edit();

                SwitchPreferenceCompat switchPreferenceCompat = (SwitchPreferenceCompat)p7;
                switched = (switchPreferenceCompat).isChecked();

                editor.putBoolean("emailNotif",switched);
                editor.apply();

                if(switched){
                    Toast.makeText(getContext(), "Email Notification started", Toast.LENGTH_SHORT).show();
                    //getActivity().startService(new Intent(getActivity(),NotifyService.class));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LivefeedFragment.sendMsg(BYTE_EMAIL_NOTIF_ON);
                        }
                    }).start();

                }else{
                    Toast.makeText(getContext(), "Email Notification stopped", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LivefeedFragment.sendMsg(BYTE_EMAIL_NOTIF_OFF);
                        }
                    }).start();
                }

            }

        }

        if(key.equals("key3")){
            Preference p1 = getPreferenceScreen().findPreference("key3");
            if(p1 instanceof SeekBarPreference){

                SharedPreferences spref_volume = getContext().getSharedPreferences(LoginActivity.clickedProductHashID,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spref_volume.edit();

                SeekBarPreference seekBarPreference = (SeekBarPreference)p1;
                int volume = seekBarPreference.getValue();
                System.out.println("VOLUME :" + volume);
                editor.putInt("volume",volume);
                editor.apply();
            }
        }
    }
}