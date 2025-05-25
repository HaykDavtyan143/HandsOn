package com.haykdavtyan.handson;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;


public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {
    Context context;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    DocumentReference ref;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        setHasStableIds(true);

    }

    @Override
    public long getItemId(int position) {
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model)
    {
        if (model.getUserIds() == null || model.getUserIds().size() < 2) return;

        if (model.getUserIds().get(0).equals(currentUser.getUid()))
        {
            ref = FirebaseFirestore.getInstance().collection("users").document(model.getUserIds().get(1));
        }
        else
        {
            ref = FirebaseFirestore.getInstance().collection("users").document(model.getUserIds().get(0));
        }

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                String time = new SimpleDateFormat("HH:MM")
                        .format(model.getLastMessageTimestamp().toDate());

                User otherUser = task.getResult().toObject(User.class);
                holder.usernameText.setText(otherUser.getUsername());
                holder.typeText.setText(otherUser.getAccType());

                if (model.getLastMessage() != null)
                {
                    if (model.getLastMessageSenderId().equals(currentUser.getUid()))
                    {
                        holder.lastMessageText.setText("You: " + model.getLastMessage());
                    }
                    else
                    {
                        holder.lastMessageText.setText(otherUser.getUsername() + ":" + model.getLastMessage());
                    }
                }

                holder.lastMessageTime.setText(time);

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("username", otherUser.getUsername());
                    intent.putExtra("type", otherUser.getAccType());
                    intent.putExtra("userID", otherUser.getId());

                    context.startActivity(intent);

                });
            }
        });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView typeText;
        TextView lastMessageText;
        TextView lastMessageTime;
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user);
            typeText = itemView.findViewById(R.id.userType);
            lastMessageText = itemView.findViewById(R.id.lastMessage);
            lastMessageTime = itemView.findViewById(R.id.time);
        }
    }

}
