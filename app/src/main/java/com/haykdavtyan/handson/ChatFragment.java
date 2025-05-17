package com.haykdavtyan.handson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class ChatFragment extends Fragment
{
    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;
    FirebaseUser currUser;

    public ChatFragment () {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        currUser = FirebaseAuth.getInstance().getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();
        return view;
    }
    private void setupRecyclerView()
    {
        Query query = FirebaseFirestore.getInstance()
                .collection("chatrooms")
                .whereArrayContains("userIds", currUser.getUid())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class)
                .build();

        adapter = new RecentChatRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.startListening();
    }
}