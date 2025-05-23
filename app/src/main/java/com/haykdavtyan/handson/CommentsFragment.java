package com.haykdavtyan.handson;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommentsFragment extends Fragment {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageButton postCommentButton;
    private CommentsAdapter commentsAdapter;

    // Map commentId → commentDataMap
    private final Map<String, Map<String, Object>> commentsMap = new LinkedHashMap<>();
    private final List<String> commentKeys = new ArrayList<>();

    private String postId;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUsername;
    private String currentType;

    private static final String ARG_POST_ID = "post_id";

    public static CommentsFragment newInstance(String postId)
    {
        CommentsFragment f = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput        = view.findViewById(R.id.comment_input);
        postCommentButton   = view.findViewById(R.id.post_comment_button);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
        if (postId == null) {
            Log.e("CommentsFragment", "Missing postId");
            return view;
        }

        // Fetch current username
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(d -> currentUsername = d.exists() ? d.getString("Username") : "Anon")
                .addOnFailureListener(e -> {
                    currentUsername = "Anon";
                    Log.e("CommentsFragment", "username fetch failed", e);
                });

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(d -> currentType = d.exists() ? d.getString("Type") : "none")
                .addOnFailureListener(e -> {
                    currentType = "none";
                    Log.e("CommentsFragment", "type fetch failed", e);
                });

        // Adapter setup
        commentsAdapter = new CommentsAdapter(requireContext(), commentsMap, commentKeys, postId, currentUserId);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentsAdapter);

        loadComments();

        postCommentButton.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            String key = String.valueOf(System.currentTimeMillis());
            Map<String,Object> commentData = new HashMap<>();
            commentData.put("text",    text);
            commentData.put("creator", currentUsername);
            commentData.put("creatorId", currentUserId);
            commentData.put("creatorType", currentType);
            commentData.put("likes",   0L);
            commentData.put("likedBy", new HashMap<String, Boolean>());

            DocumentReference postRef = db.collection("posts").document(postId);
            // **Use update()** to write nested map
            postRef.update("comments." + key, commentData)
                    .addOnSuccessListener(a -> {
                        commentsMap.put(key, commentData);
                        commentKeys.clear();
                        commentKeys.addAll(commentsMap.keySet());
                        commentsAdapter.notifyDataSetChanged();
                        commentsRecyclerView.scrollToPosition(commentKeys.size() - 1);
                        commentInput.setText("");
                    })
                    .addOnFailureListener(e -> Log.e("CommentsFragment", "add comment failed", e));
        });

        return view;
    }

    private void loadComments() {
        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    Object raw = doc.get("comments");
                    commentsMap.clear();
                    commentKeys.clear();

                    if (raw instanceof Map) {
                        //noinspection unchecked
                        Map<String, Object> outer = (Map<String, Object>) raw;
                        for (Map.Entry<String, Object> e : outer.entrySet()) {
                            if (e.getValue() instanceof Map) {
                                //noinspection unchecked
                                commentsMap.put(e.getKey(), (Map<String, Object>) e.getValue());
                            }
                        }
                    }

                    commentKeys.addAll(commentsMap.keySet());
                    commentsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("CommentsFragment", "load comments failed", e));
    }
}
