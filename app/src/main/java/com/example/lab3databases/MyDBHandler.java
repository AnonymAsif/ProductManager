package com.example.lab3databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHandler extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PRODUCT_NAME = "name";
    private static final String COLUMN_PRODUCT_PRICE = "price";
    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table_cmd = "CREATE TABLE " + TABLE_NAME +
                "(" + COLUMN_ID + "INTEGER PRIMARY KEY, " +
                COLUMN_PRODUCT_NAME + " TEXT, " +
                COLUMN_PRODUCT_PRICE + " DOUBLE " + ")";

        db.execSQL(create_table_cmd);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null); // returns "cursor" all products from the table
    }

    public Cursor getData(String name, Double price) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Accounts for possible null calls (parameter should be blank, not null)
        if (name.isBlank()) name = null;

        // returns "cursor" all products from the table
        if (name == null && price == null)
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // Searches by name
        if (name != null && price == null)
            return db.rawQuery("SELECT * FROM %s WHERE %s LIKE ?".formatted(TABLE_NAME, COLUMN_PRODUCT_NAME),
                    new String[]{"%" + name + "%"});

        double priceMin = price.longValue() == price ? price - 0.05 : price;
        double priceMax = price.longValue() == price ? price + 1 : price;

        // Searches by price
        if (name == null)
            return db.rawQuery("SELECT * FROM %s WHERE %s BETWEEN ? AND ?".formatted(TABLE_NAME, COLUMN_PRODUCT_PRICE),
                    new String[]{String.valueOf(priceMin), String.valueOf(priceMax)});

        // Search by both name and price
        return db.rawQuery("SELECT * FROM %s WHERE %s LIKE ? AND %s BETWEEN ? AND ?".formatted(TABLE_NAME, COLUMN_PRODUCT_NAME, COLUMN_PRODUCT_PRICE),
                new String[] {"%" + name + "%", String.valueOf(priceMin), String.valueOf(priceMax)});
    }

    public int deleteData(String name, Double price) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Accounts for possible null calls (parameter should be blank, not null)
        if (name.isBlank()) name = null;

        // returns "cursor" all products from the table
        if (name == null && price == null)
            return db.delete(TABLE_NAME, null,null);

        // Searches by name
        else if (name != null && price == null)
            return db.delete(TABLE_NAME, COLUMN_PRODUCT_NAME + " = ?", new String[]{name});
            //db.rawQuery("DELETE FROM %s WHERE %s = ?".formatted(TABLE_NAME, COLUMN_PRODUCT_NAME),
              //      new String[]{name}).close();

        // Searches by price
        else if (name == null)
            return db.delete(TABLE_NAME, COLUMN_PRODUCT_PRICE + " = ?", new String[]{String.valueOf(price)});
            //db.rawQuery("DELETE FROM %s WHERE %s = ?".formatted(TABLE_NAME, COLUMN_PRODUCT_PRICE),
              //      new String[]{String.valueOf(price)}).close();

        // Search by both name and price
        else return db.delete(TABLE_NAME, "%s = ? AND %s = ?".formatted(COLUMN_PRODUCT_NAME, COLUMN_PRODUCT_PRICE),
                    new String[]{name, String.valueOf(price)});
            //db.rawQuery("DELETE FROM %s WHERE %s = ? AND %s = ?".formatted(TABLE_NAME, COLUMN_PRODUCT_NAME, COLUMN_PRODUCT_PRICE),
              //  new String[] {name, String.valueOf(price)}).close();
    }

    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_PRODUCT_NAME, product.getProductName());
        values.put(COLUMN_PRODUCT_PRICE, product.getProductPrice());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }
}
