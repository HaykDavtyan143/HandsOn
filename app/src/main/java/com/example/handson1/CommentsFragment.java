package com.example.handson1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentsFragment extends Fragment
{

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button postCommentButton;
    private CommentsAdapter commentsAdapter;
    private List<Map<String, Object>> comments = new ArrayList<>();
    private String postId;

    private static final String ARG_POST_ID = "post_id";

    public static CommentsFragment newInstance(String postId) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentsFragment()
    {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput = view.findViewById(R.id.comment_input);
        postCommentButton = view.findViewById(R.id.post_comment_button);

        commentsAdapter = new CommentsAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Get postId from arguments
        if (getArguments() != null)
        {
            postId = getArguments().getString(ARG_POST_ID);
        }

        if (postId == null)
        {
            Log.e("CommentsFragment", "Post ID is null!");
            return view;
        }

        // Fetch post data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null && post.getComments() != null) {
                            comments.clear();
                            comments.addAll(post.getComments());
                            commentsAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("CommentsFragment", "Error fetching post", e));

        // Add new comment
        postCommentButton.setOnClickListener(v -> {
            Object newComment = commentInput.getText().toString().trim();
            if (!TextUtils.isEmpty((String)newComment)) {
                db.collection("posts")
                        .document(postId)
                        .update("comments", FieldValue.arrayUnion(newComment))
                        .addOnSuccessListener(aVoid -> {
                            comments.add((Map<String, Object>) newComment);
                            commentsAdapter.notifyItemInserted(comments.size() - 1);
                            commentsRecyclerView.scrollToPosition(comments.size() - 1);
                            commentInput.setText("");
                        })
                        .addOnFailureListener(e -> Log.e("CommentsFragment", "Error adding comment", e));
            }
        });

        return view;
    }
}
