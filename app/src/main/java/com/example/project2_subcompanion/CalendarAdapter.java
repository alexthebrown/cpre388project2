package com.example.project2_subcompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarModel> calendarList;

    public CalendarAdapter(List<CalendarModel> itemList) {
        this.calendarList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date, location, price;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.event_name);
            date = itemView.findViewById(R.id.event_date);
            location = itemView.findViewById(R.id.location);
            price = itemView.findViewById(R.id.price);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarModel item = calendarList.get(position);
        holder.name.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.location.setText(item.getLocation());
        holder.price.setText(item.getPrice());
    }

@Override
public int getItemCount() {
    return calendarList.size();
}

}
