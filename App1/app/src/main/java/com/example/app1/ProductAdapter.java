package com.example.app1;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{

    private Context context;
    private List<Product> listProducts;
    private TextView textView;

    public ProductAdapter(Context context, List<Product> listProducts) {
        this.context = context;
        this.listProducts = listProducts;
        //mDatabase = new SqliteDatabase(context);
    }

    /*public ProductAdapter(){

    }*/



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_layout, parent, false);

        textView = (TextView) view.findViewById(R.id.product_name);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrationActivity.clickedItem = textView.getText().toString();
                System.out.println("........clicked product = "+RegistrationActivity.clickedItem);
                proceed();
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Product singleProduct = listProducts.get(position);

        holder.name.setText(singleProduct.getName());

        holder.editProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskDialog(singleProduct);
            }
        });

        holder.deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete row from database

                listProducts.remove(position);

                //TODO: store listProducts i.e. allProducts
                //refresh the activity page.
                ((Activity)context).finish();
                context.startActivity(((Activity) context).getIntent());
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageView deleteProduct;
        public  ImageView editProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.product_name);
            deleteProduct = (ImageView)itemView.findViewById(R.id.delete_product);
            editProduct = (ImageView)itemView.findViewById(R.id.edit_product);
        }
    }

    @Override
    public int getItemCount() {
        return listProducts.size();
    }


    private void editTaskDialog(final Product product){
        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.edit_product_layout, null);

        final EditText nameField = (EditText)subView.findViewById(R.id.enter_name);
        //final EditText quantityField = (EditText)subView.findViewById(R.id.enter_quantity);

        if(product != null){
            nameField.setText(product.getName());
            //quantityField.setText(String.valueOf(product.getQuantity()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit product");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("EDIT PRODUCT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String name = nameField.getText().toString();
                //final String quantity = quantityField.getText().toString();

                if(TextUtils.isEmpty(name)){
                    Toast.makeText(context, "Something went wrong. Check your input values", Toast.LENGTH_LONG).show();
                }
                else{
                    //mDatabase.updateProduct(new Product(product.getId(), name, quantity));
                    //refresh the activity
                    ((Activity)context).finish();
                    context.startActivity(((Activity)context).getIntent());
                    product.setName(name);

                    //TODO: store allProducts
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Task cancelled", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    public void proceed(){
        Intent intent = new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }
}
