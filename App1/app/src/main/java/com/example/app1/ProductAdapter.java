package com.example.app1;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_layout, parent, false);

        textView = (TextView) view.findViewById(R.id.product_name);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView t = ((TextView)view);
                String str = t.getText().toString();
                System.out.println("item clicked = "+str);
                RegistrationActivity.clickedItem = str;
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

                deleteTaskDialogBox(position,singleProduct);
                //delete row from database
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

                    //TODO: store allProducts
                    System.out.println("name b4 change = "+product.getName());
                    RegistrationActivity.productNameList.remove(product.getName());
                    String corresHashId = product.getHashID();

                    product.setName(name);
                    System.out.println("name after change = "+product.getName());
                    for(Product p : listProducts){
                        if(p.getHashID() == corresHashId)
                            p.setName(name);
                    }

                    RegistrationActivity.productNameList.add(product.getName());
                    SharedPreferences.Editor editor2 = RegistrationActivity.spref_list.edit();
                    editor2.clear();
                    editor2.putStringSet("hashIDList",RegistrationActivity.hashIDList);
                    editor2.putStringSet("productNameList",RegistrationActivity.productNameList);
                    editor2.apply();

                    //TODO - store the new name in shared pref!!
                    SharedPreferences spref = context.getSharedPreferences(corresHashId,Context.MODE_PRIVATE);
                    /*String usr = spref.getString("username",null);
                    String pas = spref.getString("password",null);
                    String email = spref.getString("email",null);
                    Boolean logdIn = spref.getBoolean("loggedIn",false);
                    Boolean fcmTokenSent = spref.getBoolean("fcmTokenSent",true);
                    System.out.println("to store: name = "+name+" hashID = "+corresHashId+" username = "+usr+" password = "+pas+" logged In state = "+logdIn);
                    */
                    SharedPreferences.Editor editor = spref.edit();
                    //editor.clear();
                    editor.putString("name",name);
                    /*editor.putString("hashID",corresHashId);
                    editor.putString("username",usr);
                    editor.putString("password",pas);
                    editor.putString("email",email);
                    editor.putBoolean("loggedIn",logdIn);
                    editor.putBoolean("fcmTokenSent",fcmTokenSent);*/
                    editor.apply();

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

    private void deleteTaskDialogBox(final int position, final Product singleProduct){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete product");
        builder.setMessage("Are you sure you want to delete product "+singleProduct.getName()+" ?");
        builder.create();

        builder.setPositiveButton("DELETE PRODUCT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                listProducts.remove(position);
                System.out.println(" deleted name = "+singleProduct.getName());
                System.out.println(" deleted hash id = "+singleProduct.getHashID());
                RegistrationActivity.hashIDList.remove(singleProduct.getHashID());
                RegistrationActivity.productNameList.remove(singleProduct.getName());

                SharedPreferences.Editor editor2 = RegistrationActivity.spref_list.edit();
                editor2.clear();
                editor2.putStringSet("hashIDList",RegistrationActivity.hashIDList);
                editor2.putStringSet("productNameList",RegistrationActivity.productNameList);
                editor2.apply();

                SharedPreferences.Editor editor = context.getSharedPreferences(singleProduct.getHashID(),Context.MODE_PRIVATE).edit();
                editor.remove("hashID");
                editor.remove("name");
                editor.remove("username");
                editor.remove("password");
                editor.remove("fcmTokenSent");
                editor.remove("loggedIn");
                editor.remove("email");
                editor.clear();
                editor.apply();

                Toast.makeText(context, "Product deleted successfully !", Toast.LENGTH_SHORT).show();

                //refresh the activity page.
                ((Activity)context).finish();
                context.startActivity(((Activity) context).getIntent());

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Task cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public void proceed(){
        Intent intent = new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }
}
