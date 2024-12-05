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
    private Context context;
    private RecyclerViewInterface recyclerViewInterface;

    public CalendarAdapter(List<CalendarModel> itemList, Context context, RecyclerViewInterface recyclerViewInterface) {
        this.calendarList = itemList;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date, location, price;

        public ViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            name = itemView.findViewById(R.id.event_name);
            date = itemView.findViewById(R.id.event_date);
            location = itemView.findViewById(R.id.location);
            price = itemView.findViewById(R.id.price);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if(recyclerViewInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
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
