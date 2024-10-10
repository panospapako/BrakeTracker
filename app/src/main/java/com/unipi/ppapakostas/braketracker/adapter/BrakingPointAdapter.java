package com.unipi.ppapakostas.braketracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.ppapakostas.braketracker.R;
import com.unipi.ppapakostas.braketracker.model.BrakingPoint;

import java.util.List;

/**
 * Adapter class for displaying braking points in a RecyclerView.
 */
public class BrakingPointAdapter extends RecyclerView.Adapter<BrakingPointAdapter.BrakingPointViewHolder> {

    private final List<BrakingPoint> brakingPoints;

    public BrakingPointAdapter(List<BrakingPoint> brakingPoints) {
        this.brakingPoints = brakingPoints;
    }


    @NonNull
    @Override
    public BrakingPointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_braking_point, parent, false);
        return new BrakingPointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrakingPointViewHolder holder, int position) {
        BrakingPoint brakingPoint = brakingPoints.get(position);
        holder.tvNumber.setText(String.format("%s.", String.valueOf(position + 1)));
        holder.tvLocation.setText(String.format("Lat: %s, Lon: %s", brakingPoint.getLatitude(), brakingPoint.getLongitude()));
        holder.tvTimestamp.setText(String.format("Time: %s", brakingPoint.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return brakingPoints.size();
    }

    public static class BrakingPointViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvLocation, tvTimestamp;

        public BrakingPointViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
