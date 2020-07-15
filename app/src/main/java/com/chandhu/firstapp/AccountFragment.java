package com.chandhu.firstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountFragment extends Fragment {

    private CircleImageView userImage;
    private TextView userName;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        userImage = view.findViewById(R.id.account_user_image);
        userName = view.findViewById(R.id.account_user_name);

        blog_list = new ArrayList<>();
        final RecyclerView blogListView = view.findViewById(R.id.user_blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blogListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blogListView.setAdapter(blogRecyclerAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    boolean reached_bottom = !recyclerView.canScrollVertically(1);
                    if (reached_bottom){
                        loadMorePosts();
                    }
                }
            });


            final Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp",Query.Direction.DESCENDING);


            firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    assert queryDocumentSnapshots != null;
                    if (isFirstPageFirstLoad) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    }
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            final String blog_post_id = doc.getDocument().getId();
                            firebaseFirestore.collection("Posts").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    String logIt = Objects.requireNonNull(task.getResult()).getString("user_id");
                                    assert logIt != null;
                                    if (logIt.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_post_id);
                                        if (isFirstPageFirstLoad) {
                                            blog_list.add(blogPost);
                                        } else {
                                            blog_list.add(0,blogPost);
                                        }
                                        blogRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            });

        }

        final String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(Objects.requireNonNull(task.getResult()).exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        userName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.defaultprofile);
                        Glide.with(Objects.requireNonNull(getContext())).setDefaultRequestOptions(placeholderRequest).load(image).into(userImage);
                    }

                } else {

                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(getContext(), "(FIRESTORE Retrieve Error : " + error, Toast.LENGTH_LONG).show();

                }
            }
        });


        return view;
    }

    public void loadMorePosts(){
        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(Objects.requireNonNull(getActivity()),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                assert queryDocumentSnapshots != null;
                if (!queryDocumentSnapshots.isEmpty()){
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blog_post_id = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_post_id);
                            blog_list.add(blogPost);
                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}