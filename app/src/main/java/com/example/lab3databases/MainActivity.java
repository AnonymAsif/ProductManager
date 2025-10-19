package com.example.lab3databases;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView productId;
    EditText productName, productPrice;
    Button addBtn, findBtn, deleteBtn;
    ListView productListView;

    ArrayList<String> productList;
    ArrayAdapter adapter;
    MyDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productList = new ArrayList<>();

        // info layout
        productId = findViewById(R.id.productId);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);

        //buttons
        addBtn = findViewById(R.id.addBtn);
        findBtn = findViewById(R.id.findBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        // listview
        productListView = findViewById(R.id.productListView);

        // db handler
        dbHandler = new MyDBHandler(this);


        // button listeners
        addBtn.setOnClickListener(v -> {
            String name = productName.getText().toString();

            if (name.isBlank()) {
                Toast.makeText(this, "Products must have a name.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!name.chars().allMatch(Character::isLetterOrDigit)) {
                Toast.makeText(this, "Product names must be alphanumeric.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Character.isAlphabetic(name.charAt(0))) {
                Toast.makeText(this, "Product names must begin with a letter.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productPrice.getText().toString().isBlank()) {
                Toast.makeText(this, "Products must have a price.", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(productPrice.getText().toString());
            Product product = new Product(name, price);
            dbHandler.addProduct(product);

            productName.setText("");
            productPrice.setText("");

            viewProducts();
        });

        findBtn.setOnClickListener(v -> {
            String name = productName.getText().toString();
            if (!name.chars().allMatch(Character::isLetterOrDigit)) {
                Toast.makeText(this, "Product names must be alphanumeric.", Toast.LENGTH_SHORT).show();
                return;
            }

            Double price;
            if (productPrice.getText().toString().isBlank()) price = null;
            else price = Double.parseDouble(productPrice.getText().toString());

            viewProducts(name, price);
        });

        deleteBtn.setOnClickListener(v -> {
            String name = productName.getText().toString();

            if (!name.chars().allMatch(Character::isLetterOrDigit)) {
                Toast.makeText(this, "Product names must be alphanumeric.", Toast.LENGTH_SHORT).show();
                return;
            }

            Double price;
            if (productPrice.getText().toString().isBlank()) price = null;
            else price = Double.parseDouble(productPrice.getText().toString());

            // Double check with user if they deleted without filters
            if (price == null && name.isBlank()) {
                AlertDialog confirmDialog = new AlertDialog.Builder(this)
                        .setTitle("Clear Database")
                        .setMessage("You are about to delete every saved product. Would you like to continue?")
                        .setIcon(R.drawable.ic_auto_delete_white_foreground)
                        .setPositiveButton("Yes", (a, b) -> deleteProducts(name, price))
                        .setNegativeButton("Cancel", (a, b) ->
                                Toast.makeText(this, "Operation cancelled.", Toast.LENGTH_SHORT).show())
                        .create();

                confirmDialog.show();
                return;
            }
            deleteProducts(name, price);
        });

        viewProducts();
    }

    private void viewProducts() {
        productList.clear();
        Cursor cursor = dbHandler.getData();
        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "Nothing to show", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                productList.add(cursor.getString(1) + " (" +cursor.getString(2)+")");
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        productListView.setAdapter(adapter);

        cursor.close();
    }

    private void viewProducts(String name, Double price) {
        productList.clear();
        Cursor cursor = dbHandler.getData(name, price);
        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "Nothing to show", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                productList.add(cursor.getString(1) + " (" +cursor.getString(2)+")");
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        productListView.setAdapter(adapter);

        cursor.close();
    }

    private void deleteProducts(String name, Double price) {
        int deletionCount = dbHandler.deleteData(name, price);
        if (deletionCount == 0) {
            Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Successfully deleted " + deletionCount + " items", Toast.LENGTH_SHORT).show();

        productName.setText("");
        productPrice.setText("");
        viewProducts();
    }
}