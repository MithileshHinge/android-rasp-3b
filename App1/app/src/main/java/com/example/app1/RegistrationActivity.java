package com.example.app1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {

    public static String serverName = "192.168.1.106";
    public static RecyclerView productView;
    public static List<Product> allProducts = new ArrayList<>();
    public static String clickedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_screen);

        //FrameLayout fLayout = (FrameLayout) findViewById(R.id.activity_to_do);

        productView = (RecyclerView)findViewById(R.id.product_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        productView.setLayoutManager(linearLayoutManager);
        productView.setHasFixedSize(true);
        System.out.println("........Reg activity started........");
        for (Product fileEntry : allProducts) {
            System.out.println(fileEntry.getName() +"........" + fileEntry.getHashID());
        }

        //TODO: reproduce the stored allProducts

        if(allProducts.size() > 0){
            System.out.println(".......Products are there.........");
            productView.setVisibility(View.VISIBLE);
            ProductAdapter mAdapter = new ProductAdapter(this, allProducts);
            productView.setAdapter(mAdapter);

        }else {
            System.out.println("........No products........");
            productView.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "There is no product in the database. Start adding now", Toast.LENGTH_LONG).show();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addProduct);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add new quick task
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
        final EditText quantityField = (EditText)subView.findViewById(R.id.enter_quantity);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new product");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("ADD PRODUCT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String name = nameField.getText().toString();
                final String quantity = quantityField.getText().toString();

                if(TextUtils.isEmpty(name) || quantity.length() <= 0){
                    Toast.makeText(RegistrationActivity.this, "Something went wrong. Check your input values", Toast.LENGTH_LONG).show();
                }
                else{
                    Product newProduct = new Product(name, quantity);
                    allProducts.add(newProduct);
                    newProduct.setIfLoggedIn(false);

                    //TODO: store allProducts

                    Intent t= new Intent(RegistrationActivity.this,RegistrationActivity.class);
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
