package com.example.handson1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Post
{
    private String id;
    private String title;
    private String description;
    private List<Map<String, Object>> comments;

    private boolean isLiked = false;

    private int likes = 0;

    public Post() {}

    public Post(String title, String description)
    {
        this.title = title;
        this.description = description;
        this.comments = new ArrayList<>();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<Map<String, Object>> getComments()
    {
        return comments;
    }

    public void setComments(List<Map<String, Object>> comments) {
        this.comments = comments;
    }

    public int getLikes()
    {
        return likes;
    }

    public void setLikes(int likes)
    {
        this.likes = likes;
    }

    public void addComment(Object comment)
    {
        if (comments == null)
        {
            comments = new ArrayList<>();
        }

        comments.add((Map<String, Object>) comment);
    }

    public int getCommentsCount()
    {
        return comments != null ? comments.size() : 0;
    }

    public boolean getIsLiked ()
    {
        return isLiked;
    }

    public void setIsLiked (boolean isLiked)
    {
        this.isLiked = isLiked;
    }
}
