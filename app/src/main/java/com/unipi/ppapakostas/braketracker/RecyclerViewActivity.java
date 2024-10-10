package com.unipi.ppapakostas.braketracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.ppapakostas.braketracker.adapter.BrakingPointAdapter;
import com.unipi.ppapakostas.braketracker.model.BrakingPoint;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BrakingPointAdapter adapter;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = Database.getInstance(this);

        List<BrakingPoint> brakingPoints = getBrakingPointsFromDatabase();
        adapter = new BrakingPointAdapter(brakingPoints);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.recyclerToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Home");

    }

    private List<BrakingPoint> getBrakingPointsFromDatabase() {
        List<BrakingPoint> brakingPoints = new ArrayList<>();
        Cursor cursor = database.getAll();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow("lon"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                float acceleration = cursor.getFloat(cursor.getColumnIndexOrThrow("acceleration"));

                brakingPoints.add(new BrakingPoint(lat, lon, timestamp, acceleration));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return brakingPoints;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}