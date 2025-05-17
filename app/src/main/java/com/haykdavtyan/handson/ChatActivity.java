package com.haykdavtyan.handson;

import android.content.Intent;
import android.os.Bundle;
import android.os.LimitExceededException;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity
{
    ChatRecyclerAdapter adapter;
    FirebaseUser currentUser;
    ChatroomModel chatroomModel;
    String chatroomId;
    ImageButton send;
    Button newMessages;
    EditText etMessage;
    RecyclerView recyclerView;
    TextView type;
    Button btnUsername;
    String username;
    String userID;
    String currUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        send = findViewById(R.id.send);
        newMessages = findViewById(R.id.newMessages);
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.message);
        btnUsername = findViewById(R.id.username);
        type = findViewById(R.id.type);

        username = getIntent().getStringExtra("username");
        userID = getIntent().getStringExtra("userID");
        currUserId = currentUser.getUid();

        chatroomId = getChatromId(userID, currUserId);

        btnUsername.setText(username);
        type.setText(getIntent().getStringExtra("type"));

        send.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) return;

            sendMessageToUser(message);
            recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0), 200);
        });

        newMessages.setOnClickListener(v -> {
            recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0), 200);
            newMessages.setVisibility(View.GONE);
        });

        btnUsername.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, OtherUsersProfileActivity.class);

            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("type", getIntent().getStringExtra("type"));

            startActivity(intent);
            finish();
        });

        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    void getOrCreateChatroomModel()
    {
        DocumentReference ref = FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null)
                {
                    chatroomModel = new ChatroomModel(chatroomId,
                            Arrays.asList(currUserId, userID),
                            Timestamp.now(), "");
                    ref.set(chatroomModel);
                }
            }
        });

    }

    String getChatromId (String userId1, String userId2)
    {
        if(userId1.hashCode() < userId2.hashCode())
        {
            return userId1 + "_" + userId2;
        }
        else
        {
            return userId2 + "_" + userId1;
        }
    }

    void sendMessageToUser (String message)
    {
        DocumentReference refCR = FirebaseFirestore.getInstance()
                .collection("chatrooms").document(chatroomId);

        CollectionReference refCM = refCR.collection("chats");

        chatroomModel.setLastMessageSenderId(currUserId);
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessage(message);
        refCR.set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, currUserId, Timestamp.now());
        refCM.add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task)
            {
                if (task.isSuccessful())
                {
                    etMessage.setText("");
                }
            }
        });
    }

    void setupChatRecyclerView ()
    {
        if (adapter != null) {
            recyclerView.setAdapter(null);
            adapter = null;
        }

        Query query = FirebaseFirestore.getInstance()
                .collection("chatrooms")
                .document(chatroomId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class)
                .build();

        adapter = new ChatRecyclerAdapter(options, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                // Check if user is at bottom (position 0 in reverse layout)
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                    // If user is near the bottom (e.g., top 2 messages), auto-scroll
                    if (firstVisiblePosition <= 2) {
                        recyclerView.scrollToPosition(0);
                    }
                    else
                    {
                        newMessages.setVisibility(View.VISIBLE);

                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                if (layoutManager != null) {
                                    int firstVisible = layoutManager.findFirstVisibleItemPosition();

                                    if (firstVisible <= 0)
                                    {
                                        newMessages.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });


        adapter.startListening();
    }
}