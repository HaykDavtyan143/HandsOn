package com.example.handson1;

import java.util.HashMap;

public class User
{
    private String id;

    private String username;
    private String accType;
    private int followers;
    private int following;
    private int posts;
    private String description;
    private HashMap<String, Object> recommendedBy;

    public User () {}

    public User (String id, String username, String accType)
    {
        this.id = id;
        this.username = username;
        this.accType = accType;
    }

    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getAccType ()
    {
        return accType;
    }
    public int getFollowers()
    {
        return followers;
    }

    public int getFollowing()
    {
        return following;
    }

    public int getPosts()
    {
        return posts;
    }

    public String getDescription()
    {
        return description;
    }

    public HashMap<String, Object> getRecommendedBy()
    {
        return recommendedBy;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setAccType(String accType)
    {
        this.accType = accType;
    }

    public void setFollowers(int followers)
    {
        this.followers = followers;
    }

    public void setFollowing(int following)
    {
        this.following = following;
    }

    public void setPosts(int posts)
    {
        this.posts = posts;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setRecommendedBy(HashMap<String, Object> recommendedBy)
    {
        this.recommendedBy = recommendedBy;
    }
}
