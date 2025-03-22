package com.example.handson1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class NavigationBarFragment extends Fragment
{

    private ImageButton btnHome, btnSearch, btnAddPost, btnMessages, btnProfile;

    public NavigationBarFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_navigation_bar, container, false);

        btnHome = rootView.findViewById(R.id.ib_home);
        btnSearch = rootView.findViewById(R.id.ib_search);
        btnAddPost = rootView.findViewById(R.id.ib_add_post);
        btnMessages = rootView.findViewById(R.id.ib_messages);
        btnProfile = rootView.findViewById(R.id.ib_profile);

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), FeedActivity.class));
        });

        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });

        btnAddPost.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        });

        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MessagesActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        return rootView;
    }
}
