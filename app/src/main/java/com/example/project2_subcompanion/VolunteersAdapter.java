package com.example.project2_subcompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VolunteersAdapter extends RecyclerView.Adapter<VolunteersAdapter.ViewHolder> {
    private List<UserModel> volunteers;
    private Context context;
    private RecyclerViewInterface recyclerViewInterface;
    private String currentUserId;

    public VolunteersAdapter(List<UserModel> volunteers, Context context, RecyclerViewInterface recyclerViewInterface, String currentUserId) {
        this.volunteers = volunteers;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
        this.currentUserId = currentUserId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Button remove;

        public ViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            remove = itemView.findViewById(R.id.remove);

            remove.setOnClickListener(new View.OnClickListener() {
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

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.volunteer_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel volunteer = volunteers.get(position);
        holder.name.setText(volunteer.getName());
        if(volunteer.getId().equals(currentUserId)){
            holder.remove.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }



}
