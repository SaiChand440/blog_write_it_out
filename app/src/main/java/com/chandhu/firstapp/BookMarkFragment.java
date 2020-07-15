package com.chandhu.firstapp;

import android.os.Bundle;
import android.util.Log;
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

public class BookMarkFragment extends Fragment {

    private List<BookMarkPost> bookmark_list;
    private FirebaseFirestore firebaseFirestore;
    private BookMarkRecyclerAdapter bookMarkRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;

    public BookMarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_mark, container, false);
        bookmark_list = new ArrayList<>();

        final RecyclerView bookMarkListView = view.findViewById(R.id.bookmark_list_view);
        bookMarkRecyclerAdapter = new BookMarkRecyclerAdapter(bookmark_list);
        bookMarkListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        bookMarkListView.setAdapter(bookMarkRecyclerAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            bookMarkListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

            final Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp", Query.Direction.DESCENDING);
            final String[] x = {null};

            firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    assert queryDocumentSnapshots != null;
                    if (isFirstPageFirstLoad) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        bookmark_list.clear();
                    }
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        firebaseFirestore.collection("Posts").document(doc.getDocument().getId()).collection("Likes").addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                assert queryDocumentSnapshots != null;
                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    x[0] = documentChange.getDocument().getId();
                                    if (x[0].equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            String blog_post_id = documentChange.getDocument().getId();
                                            BookMarkPost bookMarkPost = doc.getDocument().toObject(BookMarkPost.class).withId(blog_post_id);
                                            if (isFirstPageFirstLoad) {
                                                bookmark_list.add(bookMarkPost);
                                            } else {
                                                bookmark_list.add(0,bookMarkPost);
                                            }
                                            bookMarkRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }


        return view;
    }

    private void loadMorePosts() {
        final Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timeStamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        final String[] x = {null};

        nextQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                assert queryDocumentSnapshots != null;

                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        firebaseFirestore.collection("Posts").document(doc.getDocument().getId()).collection("Likes").addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                assert queryDocumentSnapshots != null;
                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    x[0] = documentChange.getDocument().getId();
                                    if (x[0].equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            String blog_post_id = documentChange.getDocument().getId();
                                            BookMarkPost bookMarkPost = doc.getDocument().toObject(BookMarkPost.class).withId(blog_post_id);
                                            if (isFirstPageFirstLoad) {
                                                bookmark_list.add(bookMarkPost);
                                            } else {
                                                bookmark_list.add(0, bookMarkPost);
                                            }
                                            bookMarkRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

    }


}