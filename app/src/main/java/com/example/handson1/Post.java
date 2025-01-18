package com.example.handson1;

import java.util.ArrayList;
import java.util.List;

public class Post
{
    private String id;
    private String title;
    private String description;
    private List<String> comments;

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

    public List<String> getComments()
    {
        return comments;
    }

    public void setComments(List<String> comments)
    {
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

    public void addComment(String comment)
    {
        if (comments == null)
        {
            comments = new ArrayList<>();
        }

        comments.add(comment);
    }

    public int getCommentsCount()
    {
        return comments != null ? comments.size() : 0;
    }
}
