package com.haykdavtyan.handson;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>
{
    Context context;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context)
    {
        super(options);
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model)
    {
        if (model.getSenderId().equals(currentUser.getUid()))
        {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());
        }
        else
        {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(model.getMessage());
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;

        public ChatModelViewHolder(@NonNull View itemView)
        {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.leftChat);
            rightChatLayout = itemView.findViewById(R.id.rightChat);
            leftChatTextview = itemView.findViewById(R.id.leftChatTV);
            rightChatTextview = itemView.findViewById(R.id.rightChatTV);
        }
    }
}
