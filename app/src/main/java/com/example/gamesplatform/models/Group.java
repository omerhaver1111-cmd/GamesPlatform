package com.example.gamesplatform.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Group implements Serializable {
    public String id;
    public String leaderId;
    public String groupName;    //שם  הקבוצה
    public Map<String, Boolean> members; // if group leader -> true
    public String bannerImageBase64;


    public Group() {
        members = new HashMap<>();
    }

    public Group(String groupName, String leaderId) {
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

    public String getLeaderId() {
        return leaderId;
    }

    public boolean isMember(String userId) {
        return members != null && members.containsKey(userId);
    }

    @Exclude
    public Set<String> getMembers() {
        return this.members.keySet();
    }

    public void promoteMember(String userId) {
        if (members.containsKey(userId)) {
            members.put(userId, true);
        }
    }

    public void demoteMember(String userId) {
        // חשוב לוודא שלא עושים demote ל-leader הראשי בטעות
        if (members.containsKey(userId) && !userId.equals(leaderId)) {
            members.put(userId, false);
        }
    }

    public boolean isAdmin(String userId) {
        return members != null && members.containsKey(userId) && Boolean.TRUE.equals(members.get(userId));
    }

    @Exclude
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * מחזירה רשימה של כל ה-IDs של המשתמשים בקבוצה
     */
    @Exclude //  לא יישמר ב-Firebase
    public Set<String> getUserIds() {
        if (this.members == null) {
            return new HashMap<String, Boolean>().keySet();
        }
        return this.members.keySet();
    }

    public String getBannerImageBase64() {
        return bannerImageBase64;
    }

    public void setBannerImageBase64(String bannerImageBase64) {
        this.bannerImageBase64 = bannerImageBase64;
    }

}
