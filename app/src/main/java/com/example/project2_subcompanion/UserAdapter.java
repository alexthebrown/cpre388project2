package com.example.project2_subcompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<UserModel> userList;
    private Context context;
    private RecyclerViewInterface recyclerViewInterface;

    public UserAdapter(List<UserModel> userList, Context context, RecyclerViewInterface recyclerViewInterface){
        this.userList = userList;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, email;
        public Button alterUser;

        public ViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            alterUser = itemView.findViewById(R.id.alterUser);

            alterUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
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
                .inflate(R.layout.userlist_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        if(user.getUserClass().equals("public")){
            holder.alterUser.setText("Make Exec");
        } else {
            holder.alterUser.setText("Make Public");
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
