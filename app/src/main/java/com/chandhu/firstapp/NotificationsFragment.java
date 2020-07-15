package com.chandhu.firstapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;


public class NotificationsFragment extends Fragment {

    private List<NotificationPost> notification_list;
    private FirebaseFirestore firebaseFirestore;
    private NotificationPostRecyclerAdapter notificationPostRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_notifications, container, false);

        notification_list = new ArrayList<>();
        RecyclerView notificationRecyclerView = view.findViewById(R.id.notification_recycler_view);
        notificationPostRecyclerAdapter = new NotificationPostRecyclerAdapter(notification_list);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        notificationRecyclerView.setAdapter(notificationPostRecyclerAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();
            notificationRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp",Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    assert queryDocumentSnapshots != null;
                    if (isFirstPageFirstLoad) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        notification_list.clear();
                    }
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            final String notification_post_id = doc.getDocument().getId();


                            final String[] name_of_user = {""};
                            firebaseFirestore.collection("Posts").document(notification_post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    assert documentSnapshot != null;
                                    name_of_user[0] = Objects.requireNonNull(documentSnapshot.getString("user_id"));
                                    if (name_of_user[0].equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        firebaseFirestore.collection("Posts").document(notification_post_id).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                assert queryDocumentSnapshots != null;
                                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                        String liker_id = documentChange.getDocument().getId();
                                                        NotificationPost notificationPost = documentChange.getDocument().toObject(NotificationPost.class).withId(liker_id);
                                                        if (isFirstPageFirstLoad){
                                                            notification_list.add(notificationPost);
                                                        }else{
                                                            notification_list.add(0,notificationPost);
                                                        }
                                                        notificationPostRecyclerAdapter.notifyDataSetChanged();
                                                    } else if (documentChange.getType() == DocumentChange.Type.REMOVED){
                                                        String liker_id = documentChange.getDocument().getId();
                                                        NotificationPost notificationPost = documentChange.getDocument().toObject(NotificationPost.class).withId(liker_id);
                                                        notification_list.remove(notificationPost);
                                                        notificationPostRecyclerAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            });
        }

        return view;
        }

    private void loadMorePosts() {
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
                            final String notification_post_id = doc.getDocument().getId();

                            final String[] name_of_user = {""};
                            firebaseFirestore.collection("Posts").document(notification_post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    assert documentSnapshot != null;
                                    name_of_user[0] = Objects.requireNonNull(documentSnapshot.getString("user_id"));
                                    if (name_of_user[0].equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                        firebaseFirestore.collection("Posts").document(notification_post_id).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                assert queryDocumentSnapshots != null;
                                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
//                                                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                        String liker_id = documentChange.getDocument().getId();
                                                        NotificationPost notificationPost = documentChange.getDocument().toObject(NotificationPost.class).withId(liker_id);
                                                        notification_list.add(notificationPost);
                                                        notificationPostRecyclerAdapter.notifyDataSetChanged();
//                                                    }
                                                }
                                            }
                                        });

                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}