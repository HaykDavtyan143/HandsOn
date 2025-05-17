package com.haykdavtyan.handson;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
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

        highlightCurrentActivityButton();

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), FeedActivity.class));
            btnHome.setColorFilter(ContextCompat.getColor(requireContext(), R.color.handson), PorterDuff.Mode.SRC_IN);
            btnSearch.clearColorFilter();
            btnAddPost.clearColorFilter();
            btnMessages.clearColorFilter();
            btnProfile.clearColorFilter();
        });

        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            btnSearch.setColorFilter(ContextCompat.getColor(requireContext(), R.color.handson), PorterDuff.Mode.SRC_IN);
            btnHome.clearColorFilter();
            btnAddPost.clearColorFilter();
            btnMessages.clearColorFilter();
            btnProfile.clearColorFilter();
        });

        btnAddPost.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
            btnAddPost.setColorFilter(ContextCompat.getColor(requireContext(), R.color.handson), PorterDuff.Mode.SRC_IN);
            btnSearch.clearColorFilter();
            btnHome.clearColorFilter();
            btnMessages.clearColorFilter();
            btnProfile.clearColorFilter();
        });

        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MessagesActivity.class));
            btnMessages.setColorFilter(ContextCompat.getColor(requireContext(), R.color.handson), PorterDuff.Mode.SRC_IN);
            btnSearch.clearColorFilter();
            btnAddPost.clearColorFilter();
            btnHome.clearColorFilter();
            btnProfile.clearColorFilter();
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
            btnProfile.setColorFilter(ContextCompat.getColor(requireContext(), R.color.handson), PorterDuff.Mode.SRC_IN);
            btnSearch.clearColorFilter();
            btnAddPost.clearColorFilter();
            btnMessages.clearColorFilter();
            btnHome.clearColorFilter();
        });

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        highlightCurrentActivityButton();
    }

    private void highlightCurrentActivityButton()
    {
        Activity currentActivity = getActivity();
        if (currentActivity == null) return;

        int highlightColor = ContextCompat.getColor(requireContext(), R.color.handson);

        btnHome.clearColorFilter();
        btnSearch.clearColorFilter();
        btnAddPost.clearColorFilter();
        btnMessages.clearColorFilter();
        btnProfile.clearColorFilter();

        if (currentActivity instanceof FeedActivity)
        {
            btnHome.setColorFilter(highlightColor, PorterDuff.Mode.SRC_IN);
        }
        else if (currentActivity instanceof SearchActivity)
        {
            btnSearch.setColorFilter(highlightColor, PorterDuff.Mode.SRC_IN);
        }
        else if (currentActivity instanceof AddPostActivity)
        {
            btnAddPost.setColorFilter(highlightColor, PorterDuff.Mode.SRC_IN);
        }
        else if (currentActivity instanceof MessagesActivity)
        {
            btnMessages.setColorFilter(highlightColor, PorterDuff.Mode.SRC_IN);
        }
        else if (currentActivity instanceof ProfileActivity)
        {
            btnProfile.setColorFilter(highlightColor, PorterDuff.Mode.SRC_IN);
        }
    }
}
