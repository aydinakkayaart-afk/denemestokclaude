package com.stok.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite veritabanı: tek tablo (products). */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "stok.db";
    private static final int DB_VERSION = 1;

    public static class Product {
        public long id;
        public String name;
        public int qty;
        public double price;
        public int minQty;
    }

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "qty INTEGER NOT NULL DEFAULT 0,"
                + "price REAL NOT NULL DEFAULT 0,"
                + "min_qty INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Şimdilik şema değişikliği yok.
    }

    public List<Product> list(String query) {
        List<Product> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String base = "SELECT id, name, qty, price, min_qty FROM products ";
        Cursor c;
        if (query == null || query.isEmpty()) {
            c = db.rawQuery(base + "ORDER BY name COLLATE NOCASE", null);
        } else {
            c = db.rawQuery(base + "WHERE name LIKE ? ORDER BY name COLLATE NOCASE",
                    new String[]{"%" + query + "%"});
        }
        try {
            while (c.moveToNext()) {
                Product p = new Product();
                p.id = c.getLong(0);
                p.name = c.getString(1);
                p.qty = c.getInt(2);
                p.price = c.getDouble(3);
                p.minQty = c.getInt(4);
                out.add(p);
            }
        } finally {
            c.close();
        }
        return out;
    }

    public void insert(String name, int qty, double price, int minQty) {
        getWritableDatabase().insert("products", null, values(name, qty, price, minQty));
    }

    public void update(long id, String name, int qty, double price, int minQty) {
        getWritableDatabase().update("products", values(name, qty, price, minQty),
                "id = ?", new String[]{String.valueOf(id)});
    }

    /** Stoğu delta kadar değiştirir; 0'ın altına düşmez. */
    public void adjustQty(long id, int delta) {
        getWritableDatabase().execSQL(
                "UPDATE products SET qty = MAX(0, qty + ?) WHERE id = ?",
                new Object[]{delta, id});
    }

    public void delete(long id) {
        getWritableDatabase().delete("products", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues values(String name, int qty, double price, int minQty) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("qty", qty);
        v.put("price", price);
        v.put("min_qty", minQty);
        return v;
    }
}
