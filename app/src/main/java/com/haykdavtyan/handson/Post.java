package com.haykdavtyan.handson;

import com.google.firebase.Timestamp;

import java.util.HashMap;

public class Post
{
    private String id;
    private String title;
    private String creator;
    private String creatorId;
    private boolean approved;
    private Timestamp creationTime;
    private Timestamp expirationTime;
    private String creatorType;
    private String description;
    private HashMap<String, Object> comments;
    private HashMap<String, Object> likedBy;
    private int commentsCount;

    private boolean isLiked = false;

    private int likes = 0;

    public Post() {}

    public Post(String title, String description)
    {
        this.title = title;
        this.description = description;
        this.comments = new HashMap<>();
    }

    public String getCreator()
    {
        return creator;
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public String getCreatorId()
    {
        return creatorId;
    }

    public void setCreatorId(String creatorId)
    {
        this.creatorId = creatorId;
    }

    public String getCreatorType()
    {
        return creatorType;
    }

    public void setCreatorType(String creatorType)
    {
        this.creatorType = creatorType;
    }

    public HashMap<String, Object> getLikedBy()
    {
        return likedBy;
    }

    public void setLikedBy(HashMap<String, Object> likedBy)
    {
        this.likedBy = likedBy;
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

    public HashMap<String, Object> getComments()
    {
        return comments;
    }

    public void setComments(HashMap<String, Object> comments) {
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

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Timestamp expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void addComment(Object comment)
    {
        if (comments == null)
        {
            comments = new HashMap<>();
        }

        comments.put("", comment);
    }

    public void setCommentsCount (int commentCount)
    {
        this.commentsCount = commentCount;
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
