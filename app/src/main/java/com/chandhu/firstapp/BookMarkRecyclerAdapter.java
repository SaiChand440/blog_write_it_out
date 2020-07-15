package com.chandhu.firstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookMarkRecyclerAdapter extends RecyclerView.Adapter<BookMarkRecyclerAdapter.ViewHolder> {

    public List<BookMarkPost> bookmark_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;

    public BookMarkRecyclerAdapter(List<BookMarkPost> bookmark_list) {
        this.bookmark_list = bookmark_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_card_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String bookMarkPostId = bookmark_list.get(position).BookMarkPostId;

        String desc_data = bookmark_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = bookmark_list.get(position).getImage_url();
        String thumbUrl = bookmark_list.get(position).getThumb_url();
        holder.setBlogImage(image_url,thumbUrl);

        String user_id = bookmark_list.get(position).getUser_id();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String userName = Objects.requireNonNull(task.getResult()).getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setUserDescription(userName,userImage);
                }else{
                    Toast.makeText(context, "" + Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        long milliseconds = bookmark_list.get(position).getTimeStamp().getTime();
        Date date = new Date(milliseconds);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
        String dateText = df2.format(date);

        holder.setDate(dateText);

        firebaseFirestore.collection("Posts/" + bookMarkPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                if (!queryDocumentSnapshots.isEmpty()){
                    holder.updateLikesCount(queryDocumentSnapshots.size());
                    Log.i("Snapshot size", String.valueOf(queryDocumentSnapshots.size()));
                }else{
                    holder.updateLikesCount(0);
                    Log.i("Snapshot size","0");
                }

            }
        });

        final String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        firebaseFirestore.collection("Posts/" + bookMarkPostId + "/Likes").document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                if (documentSnapshot.exists()){
                    holder.bookmarkLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_selected_24));
                } else{
                    holder.bookmarkLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_24));
                }
            }
        });

        holder.bookmarkLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + bookMarkPostId + "/Likes").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!Objects.requireNonNull(task.getResult()).exists()){
                            Map<String,Object> likes_map = new HashMap<>();
                            likes_map.put("timeStamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + bookMarkPostId + "/Likes").document(current_user_id).set(likes_map);
                        }else{
                            firebaseFirestore.collection("Posts/" + bookMarkPostId + "/Likes").document(current_user_id).delete();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmark_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView bookmarkLikeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            bookmarkLikeBtn = mView.findViewById(R.id.bookmark_like_button);
        }

        @SuppressLint("CheckResult")
        public void setUserDescription(String name, String image){
            TextView userName = mView.findViewById(R.id.bookmark_user_name);
            userName.setText(name);
            CircleImageView userData = mView.findViewById(R.id.bookmark_user_image);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.profileloader);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(userData);
        }

        public void setDescText(String text) {
            TextView descView = mView.findViewById(R.id.bookmark_desc);
            descView.setText(text);
        }

        public void setBlogImage(String download_uri, String thumb_uri) {
            ImageView blogImageView = mView.findViewById(R.id.bookmark_image);

            Glide.with(context)
                    .load(download_uri)
                    .thumbnail(Glide.with(context).load(thumb_uri))
                    .into(blogImageView);
        }

        public void setDate(String date) {
            TextView blogDate = mView.findViewById(R.id.bookmark_date);
            blogDate.setText(date);
        }

        public void updateLikesCount(int count){
            TextView blogLikeCount = mView.findViewById(R.id.bookmark_like_count);
            blogLikeCount.setText(String.valueOf(count));
        }
    }
}
