package com.example.handson1;

import android.graphics.Color;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CommentsFragment extends Fragment {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private FeedAdapter feedAdapter;
    private FeedActivity feedActivity;
    private ImageButton postCommentButton;
    private CommentsAdapter commentsAdapter;
    private HashMap<String, Object> comments = new HashMap<>();
    private String postId;

    private static final String ARG_POST_ID = "post_id";

    public static CommentsFragment newInstance(String postId) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput = view.findViewById(R.id.comment_input);
        commentInput.setTextColor(Color.BLACK);
        postCommentButton = view.findViewById(R.id.post_comment_button);

        commentsAdapter = new CommentsAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentsAdapter);

        feedActivity = new FeedActivity();
        feedAdapter = new FeedAdapter(getContext(), feedActivity.posts);

        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }

        if (postId == null) {
            Log.e("CommentsFragment", "Post ID is null!");
            return view;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object commentsObject = documentSnapshot.get("comments");

                        if (commentsObject instanceof Map) {
                            comments.clear();
                            for (Map.Entry<?, ?> entry : ((Map<?, ?>) commentsObject).entrySet()) {
                                String key = entry.getKey().toString();
                                comments.put(key, entry.getValue());
                            }
                            commentsAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("CommentsFragment", "Unexpected comments format: " + commentsObject);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("CommentsFragment", "Error fetching post", e));

        postCommentButton.setOnClickListener(v -> {
            String newComment = commentInput.getText().toString().trim();

            if (!TextUtils.isEmpty(newComment)) {

                String commentKey = String.valueOf(newComment.hashCode());

                db.collection("posts")
                        .document(postId)
                        .update("comments." + commentKey, newComment)
                        .addOnSuccessListener(aVoid -> {
                            comments.put(commentKey, newComment);
                            commentsAdapter.notifyDataSetChanged();
                            commentsRecyclerView.scrollToPosition(comments.size() - 1);
                            commentInput.setText("");
                        })
                        .addOnFailureListener(e -> Log.e("CommentsFragment", "Error adding comment", e));

                feedAdapter.getCommentCount(postId, new CommentCountCallback()
                {
                    @Override
                    public void onCommentCountFetched(int count) {
                        feedAdapter.updateCommentCountForPost(postId, count);
                    }
                });
            }
        });

        return view;
    }
}
