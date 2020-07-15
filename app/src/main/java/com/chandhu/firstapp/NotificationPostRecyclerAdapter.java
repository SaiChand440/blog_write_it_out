package com.chandhu.firstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class NotificationPostRecyclerAdapter extends RecyclerView.Adapter<NotificationPostRecyclerAdapter.ViewHolder> {

    public List<NotificationPost> notification_post;
    public Context context;
    private FirebaseFirestore firebaseFirestore;

    public NotificationPostRecyclerAdapter(List<NotificationPost> notification_post) {
        this.notification_post = notification_post;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_user_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationPostRecyclerAdapter.ViewHolder holder, int position) {

        if(notification_post.get(position).getTimeStamp() != null) {

            long milliseconds = notification_post.get(position).getTimeStamp().getTime();
            Date date = new Date(milliseconds);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
            String dateText = df2.format(date);
            holder.setDescText("Your post was saved at " + dateText);

        }
    }

    @Override
    public int getItemCount() {
        return notification_post.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String text) {
            TextView descView = mView.findViewById(R.id.notification_text);
            descView.setText(text);
        }
    }
}
