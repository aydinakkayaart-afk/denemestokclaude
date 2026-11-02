package com.stok.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final Locale TR = new Locale("tr", "TR");

    private DbHelper db;
    private final List<DbHelper.Product> items = new ArrayList<>();
    private ProductAdapter adapter;
    private EditText search;
    private TextView summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stok Takip");
        db = new DbHelper(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));

        search = new EditText(this);
        search.setHint("Ürün ara...");
        search.setSingleLine(true);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { refresh(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        root.addView(search, new LinearLayout.LayoutParams(-1, -2));

        summary = new TextView(this);
        summary.setTextSize(13);
        summary.setTextColor(Color.DKGRAY);
        summary.setPadding(dp(4), dp(8), dp(4), dp(8));
        root.addView(summary, new LinearLayout.LayoutParams(-1, -2));

        ListView list = new ListView(this);
        adapter = new ProductAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, pos, id) -> showEditor(items.get(pos)));
        list.setOnItemLongClickListener((parent, view, pos, id) -> {
            confirmDelete(items.get(pos));
            return true;
        });
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button add = new Button(this);
        add.setText("+ Ürün Ekle");
        add.setOnClickListener(v -> showEditor(null));
        root.addView(add, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
        refresh();
    }

    private void refresh() {
        items.clear();
        items.addAll(db.list(search.getText().toString().trim()));
        adapter.notifyDataSetChanged();

        int totalQty = 0;
        double totalValue = 0;
        for (DbHelper.Product p : items) {
            totalQty += p.qty;
            totalValue += p.qty * p.price;
        }
        if (items.isEmpty()) {
            summary.setText("Ürün yok. Aşağıdaki butondan ekleyin.");
        } else {
            summary.setText(String.format(TR, "%d ürün • %d adet • Değer: %,.2f ₺",
                    items.size(), totalQty, totalValue));
        }
    }

    // ---------- Liste ----------

    private class ProductAdapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int i) { return items.get(i); }
        @Override public long getItemId(int i) { return items.get(i).id; }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            final DbHelper.Product p = items.get(i);

            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(10), dp(8), dp(10));

            LinearLayout left = new LinearLayout(MainActivity.this);
            left.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(MainActivity.this);
            name.setText(p.name);
            name.setTextSize(16);
            name.setTypeface(null, Typeface.BOLD);
            left.addView(name);

            TextView info = new TextView(MainActivity.this);
            String text = String.format(TR, "%,.2f ₺", p.price);
            if (p.minQty > 0) text += "  •  Kritik: " + p.minQty;
            info.setText(text);
            info.setTextSize(13);
            info.setTextColor(Color.GRAY);
            left.addView(info);

            row.addView(left, new LinearLayout.LayoutParams(0, -2, 1f));

            Button minus = smallButton("−");
            minus.setOnClickListener(v -> { db.adjustQty(p.id, -1); refresh(); });
            row.addView(minus, new LinearLayout.LayoutParams(dp(48), dp(44)));

            TextView qty = new TextView(MainActivity.this);
            qty.setText(String.valueOf(p.qty));
            qty.setTextSize(18);
            qty.setTypeface(null, Typeface.BOLD);
            qty.setGravity(Gravity.CENTER);
            qty.setMinWidth(dp(44));
            boolean low = p.minQty > 0 && p.qty <= p.minQty;
            qty.setTextColor(low ? Color.RED : Color.BLACK);
            row.addView(qty, new LinearLayout.LayoutParams(-2, -2));

            Button plus = smallButton("+");
            plus.setOnClickListener(v -> { db.adjustQty(p.id, 1); refresh(); });
            row.addView(plus, new LinearLayout.LayoutParams(dp(48), dp(44)));

            return row;
        }
    }

    private Button smallButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setFocusable(false); // satır tıklamasını engellemesin
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    // ---------- Ekle / Düzenle / Sil ----------

    private void showEditor(final DbHelper.Product existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        final EditText name = field("Ürün adı",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final EditText qty = field("Adet", InputType.TYPE_CLASS_NUMBER);
        final EditText price = field("Birim fiyat (₺)",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText minQty = field("Kritik stok seviyesi (opsiyonel)",
                InputType.TYPE_CLASS_NUMBER);
        form.addView(name);
        form.addView(qty);
        form.addView(price);
        form.addView(minQty);

        if (existing != null) {
            name.setText(existing.name);
            qty.setText(String.valueOf(existing.qty));
            price.setText(existing.price == Math.floor(existing.price)
                    ? String.valueOf((long) existing.price)
                    : String.valueOf(existing.price));
            minQty.setText(String.valueOf(existing.minQty));
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Yeni ürün" : "Ürünü düzenle")
                .setView(form)
                .setPositiveButton("Kaydet", null)
                .setNegativeButton("İptal", null);
        if (existing != null) b.setNeutralButton("Sil", null);

        final AlertDialog dialog = b.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        // Geçersiz girişte pencere kapanmasın diye tıklamayı show()'dan sonra bağlıyoruz.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            if (n.isEmpty()) {
                name.setError("Ad gerekli");
                return;
            }
            int q = parseInt(qty.getText().toString());
            double pr = parseDouble(price.getText().toString());
            int mq = parseInt(minQty.getText().toString());
            if (existing == null) {
                db.insert(n, q, pr, mq);
            } else {
                db.update(existing.id, n, q, pr, mq);
            }
            dialog.dismiss();
            refresh();
        });
        if (existing != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                dialog.dismiss();
                confirmDelete(existing);
            });
        }
    }

    private void confirmDelete(final DbHelper.Product p) {
        new AlertDialog.Builder(this)
                .setTitle("Silinsin mi?")
                .setMessage(p.name + " silinecek.")
                .setPositiveButton("Sil", (d, w) -> { db.delete(p.id); refresh(); })
                .setNegativeButton("İptal", null)
                .show();
    }

    // ---------- Yardımcılar ----------

    private EditText field(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setSingleLine(true);
        return e;
    }

    private static int parseInt(String s) {
        try {
            return Math.max(0, Integer.parseInt(s.trim()));
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        try {
            return Math.max(0, Double.parseDouble(s.trim().replace(',', '.')));
        } catch (Exception e) {
            return 0;
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
