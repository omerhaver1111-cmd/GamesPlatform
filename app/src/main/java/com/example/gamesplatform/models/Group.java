package com.example.gamesplatform.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Group implements Serializable {
    public String id;
    public String leaderId;
    public String groupName;
    public Map<String, Boolean> members; // if group leader -> true


    public Group() {
        members = new HashMap<>();
    }

    public Group(String groupName,String leaderId) {
        this.leaderId = leaderId;
        this.groupName = groupName;
        this.id = groupName;
        members = new HashMap<>();
        members.put(leaderId, true);
    }

    public void addMember(String userId) {
        members.put(userId, false);
    }

    public void removeMember(String userId) {
        members.remove(userId);
    }

    public String getGroupId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isMember(String userId) {
        return members.containsKey(userId);
    }

    @Exclude
    public Set<String> getMembers() {
        return this.members.keySet();
    }
}
