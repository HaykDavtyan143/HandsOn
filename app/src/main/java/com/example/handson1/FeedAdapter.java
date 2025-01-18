package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private List<Post> posts;

    public FeedAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());
        holder.commentCount.setText(post.getCommentsCount() + " comments");

        holder.commentButton.setOnClickListener(v -> openCommentDialog(post, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void openCommentDialog(Post post, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Add a Comment");

        final android.widget.EditText input = new android.widget.EditText(context);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String comment = input.getText().toString();
            if (!comment.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("posts")
                        .document(post.getId())
                        .update("comments", FieldValue.arrayUnion(comment))
                        .addOnSuccessListener(aVoid -> {
                            post.addComment(comment);
                            notifyItemChanged(position);
                        });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, commentCount;
        Button commentButton;


        @SuppressLint("WrongViewCast")
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            description = itemView.findViewById(R.id.post_description);
            commentCount = itemView.findViewById(R.id.comment_count);
            commentButton = itemView.findViewById(R.id.comment_button);
        }
    }
}

