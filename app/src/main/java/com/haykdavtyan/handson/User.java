package com.haykdavtyan.handson;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;

public class User {
    private String id;
    private String email;
    private String password;
    private String username;
    private String accType;

    private HashMap<String, String> followers = new HashMap<>();
    private HashMap<String, String> following = new HashMap<>();

    public User() {}

    public User(String email, String id, String username, String accType)
    {
        this.email = email;
        this.id = id;
        this.username = username;
        this.accType = accType;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    public User(String email, String id, String username, String accType, String password)
    {
        this.email = email;
        this.id = id;
        this.username = username;
        this.accType = accType;
        this.password = password;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    public String getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    @PropertyName("Username")
    public String getUsername()
    {
        return username;
    }

    @PropertyName("Type")
    public String getAccType()
    {
        return accType;
    }

    @PropertyName("Followers")
    public HashMap<String, String> getFollowers()
    {
        return followers;
    }

    @PropertyName("Following")
    public HashMap<String, String> getFollowing()
    {
        return following;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @PropertyName("Username")
    public void setUsername(String username)
    {
        this.username = username;
    }

    @PropertyName("Type")
    public void setAccType(String accType)
    {
        this.accType = accType;
    }

    @PropertyName("Followers")
    public void setFollowers(HashMap<String, String> followers)
    {
        this.followers = followers;
    }

    @PropertyName("Following")
    public void setFollowing(HashMap<String, String> following)
    {
        this.following = following;
    }
}
