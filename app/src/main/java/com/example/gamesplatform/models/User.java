package com.example.gamesplatform.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    public String id;
    public String username;
    public String nickname;
    public String email;
    public String password;

    public boolean inGroup;
    public boolean isAdmin;

    public int money;
    public int exp;
    public int carGameRecord;
    public int snakeRecord;
    public int pianoRecord;

    public User() {
    }

    public User(String id, String username, String nickname, String email, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.inGroup = false;
        this.isAdmin = isAdmin;
        this.money = 100;
        this.exp = 0;
        this.carGameRecord = 0;
        this.snakeRecord = 0;
        this.pianoRecord = 0;
    }

    public static int levelCalculate(long xp) {
        /// Level = 0.2 * sqrt(XP)
        int level = (int) Math.floor(0.2 * Math.sqrt(xp));
        return Math.max(1, level);
    }

    public static int getRemainingExp(int xp) {
        int currentLevel = levelCalculate(xp);
        /// חישוב כמות הexp עבור הרמה
        int xpAtLevel = (int) Math.pow(currentLevel / 0.2, 2);
        return xp - xpAtLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getInGroup() {
        return this.inGroup;
    }

    @Exclude
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getCarGameRecord() {
        return carGameRecord;
    }

    public void setCarGameRecord(int carGameRecord) {
        this.carGameRecord = carGameRecord;
    }

    public int getSnakeRecord() {
        return snakeRecord;
    }

    public void setSnakeRecord(int setSnakeRecord) {
        this.snakeRecord = setSnakeRecord;
    }

    public int getPianoRecord() {
        return pianoRecord;
    }

    public void setPianoRecord(int setPianoRecord) {
        this.pianoRecord = setPianoRecord;
    }

    @Exclude
    public boolean isInGroup() {
        return inGroup;
    }

    public void setInGroup(boolean ing) {
        this.inGroup = ing;
    }

    public Group getMyGroup(List<Group> groups) {
        if (!inGroup || groups == null || id == null) {
            return null;
        }
        for (Group group : groups) {
            if (group.getUserIds() != null && group.getUserIds().contains(id)) {
                return group;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isAdmin=" + isAdmin +
                ", money=" + money +
                ", exp=" + exp +
                '}';
    }


}
