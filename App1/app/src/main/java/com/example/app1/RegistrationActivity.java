package com.example.app1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationActivity extends AppCompatActivity {

    public static String serverName = "13.235.19.8";
    public static RecyclerView productView;
    public static String clickedItem;
    /*public static List<Product> allProducts = new ArrayList<>();
    public static Set<String> hashIDList = new HashSet<String>();
    public static Set<String> productNameList = new HashSet<String>();*/
    public static List<Product> allProducts = new ArrayList<>();
    public static String hashIDList;
    public static String productNameList;
    // static Product allProducts;
    public static SharedPreferences spref_list;
    public static int productSerialNo;
    private static boolean productExist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_screen);

        productExist = false;
        //FrameLayout fLayout = (FrameLayout) findViewById(R.id.activity_to_do);
        productView = (RecyclerView)findViewById(R.id.product_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        productView.setLayoutManager(linearLayoutManager);
        productView.setHasFixedSize(true);
        System.out.println("........Reg activity started........");
        SharedPreferences spref = getSharedPreferences("fcmToken" , MODE_PRIVATE);
        System.out.println("........FCM Token = " + spref.getString("fcmToken", ""));

        //TODO: reproduce the stored allProducts

        spref_list = getSharedPreferences("Lists", MODE_PRIVATE);
        productNameList = spref_list.getString("productNameList",null);
        hashIDList = spref_list.getString("hashIDList",null);
        /*productNameList = spref_list.getStringSet("productNameList",new HashSet<String>());
        hashIDList = spref_list.getStringSet("hashIDList",new HashSet<String>());*/

        System.out.println("RA onCreate : hashIDList = "+hashIDList + " ProductName = " + productNameList);

        //REFRESH HashIDList and ProductNameList !!!!!!!!!!!!
        /*hashIDList = new HashSet<>();
        productNameList = new HashSet<>();

        SharedPreferences.Editor edit = spref_list.edit();
        edit.putStringSet("hashIDList",hashIDList);
        edit.putStringSet("productNameList",productNameList);
        edit.apply();*/

        allProducts = new ArrayList<>();
        if(hashIDList!= null){
            SharedPreferences preferences = getSharedPreferences(hashIDList,MODE_APPEND);
            String n = preferences.getString("name",null);
            String h = preferences.getString("hashID",null);
            String u = preferences.getString("username",null);
            String pa = preferences.getString("password",null);
            Boolean li = preferences.getBoolean("loggedIn",false);
            System.out.println("name = "+n+" hashID = "+h+" username = "+u+" password = "+pa+" logged in = "+li);
            Product p = new Product(n,h);
            p.setName(n);
            p.setHashID(h);
            p.setUsername(u);
            p.setPassword(pa);
            p.setIfLoggedIn(li);
            allProducts.add(p);
        }


        System.out.println("allProducts = "+allProducts);

        /*Product product = new Product("home","aa");
        product.setUsername("qwer");
        product.setPassword("qwer");
        product.setIfLoggedIn(true);
        allProducts.add(product);*/
        // TODO: 22-06-2019 if logout button is pressed set product adapter

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addProduct);
        if(allProducts.size()>0){
            System.out.println(".......Products are there........." );
            fab.setVisibility(View.INVISIBLE);
            if(!allProducts.get(0).getIfLoggedIn()){
                System.out.println("PRODUCT NOT LOGGED IN");
            }else {
                System.out.println("DIRECT LOGIN");
                proceed();
                //TODO Direct login
            }
            /*int size = allProducts.size();
            System.out.println("SIZE : " + size + " " + allProducts.get(0).getHashID() + " " + allProducts.get(1).getHashID());
            */
            productView.setVisibility(View.VISIBLE);
            ProductAdapter mAdapter = new ProductAdapter(this, allProducts);
            productView.setAdapter(mAdapter);




        }else {
            System.out.println("........No products........");
            productView.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "There is no product in the database. Start adding now", Toast.LENGTH_LONG).show();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskDialog();
            }
        });
    }

    public void proceed(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, 0);

        finish();
    }

    private void addTaskDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.add_product_layout, null);

        final EditText nameField = (EditText)subView.findViewById(R.id.enter_name);
        final EditText hashIDField = (EditText)subView.findViewById(R.id.enter_hashID);
        final EditText emailIDField = (EditText)subView.findViewById(R.id.enter_email);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new product");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("ADD PRODUCT", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("ADD BUTTON ON CLICK");
                final String name = nameField.getText().toString();
                final String hashID = hashIDField.getText().toString();
                final String email = emailIDField.getText().toString();
                System.out.println("Entered Product : " + name + " " + hashID + " " + email);
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(RegistrationActivity.this, "Something went wrong. Check your reference name", Toast.LENGTH_LONG).show();
                    nameField.setError("Enter a reference name");
                } else if (hashID.length() <= 0) {
                    Toast.makeText(RegistrationActivity.this, "Something went wrong. Check your hashID", Toast.LENGTH_LONG).show();
                    hashIDField.setError("Enter a valid hashID");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegistrationActivity.this, "Something went wrong. Check your email address", Toast.LENGTH_LONG).show();
                    emailIDField.setError("Enter a valid email address");
                /*} else if (hashIDList!=null){
                    if(hashIDList.equals(hashID)) {
                        Toast.makeText(RegistrationActivity.this, "hashID already exists. Check again !", Toast.LENGTH_LONG).show();
                        hashIDField.setError("hash ID already exists");
                    }
                } else if (productNameList != null){
                       if(productNameList.equals(name)) {
                           Toast.makeText(RegistrationActivity.this, "Name already exists. Please choose another name !", Toast.LENGTH_LONG).show();
                           nameField.setError("Reference name already exists");
                       }*/
                } else {
                    /*Product newProduct = new Product(name, hashID);
                    newProduct.setIfLoggedIn(false);
                    allProducts.add(newProduct);*/

                    productNameList = name;
                    hashIDList = hashID;

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    //SharedPreferences.Editor edit = sharedPreferences.edit();
                    //productSerialNo = sharedPreferences.getInt("serialNo",1);

                    //TODO: store allProducts
                    SharedPreferences.Editor editor = getSharedPreferences(hashID, MODE_PRIVATE).edit();
                    editor.clear();
                    editor.putString("name", name);
                    editor.putString("hashID", hashID);
                    editor.putString("email", email);
                    editor.apply();

                    /*editor.putInt("serialNo",productSerialNo);
                    editor.apply();

                    System.out.println("............serial number for new product = "+productSerialNo);
                    productSerialNo++;

                    edit.putInt("serialNo",productSerialNo);
                    edit.apply();*/

                    SharedPreferences.Editor editor2 = spref_list.edit();
                    editor2.clear();
                    editor2.putString("hashIDList", hashIDList);
                    editor2.putString("productNameList", productNameList);
                    editor2.apply();

                    System.out.println(" RA  adding : hashIDList = " + hashIDList + " ProductName = " + productNameList);

                    Intent t = new Intent(RegistrationActivity.this, RegistrationActivity.class);
                    startActivity(t);
                    finish();
                    System.out.println("........product added........");
                    //finish();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(RegistrationActivity.this, "Task cancelled", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if(mDatabase != null){
            mDatabase.close();
        }*/
    }
}
