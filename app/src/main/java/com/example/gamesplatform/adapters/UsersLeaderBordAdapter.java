package com.example.gamesplatform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersLeaderBordAdapter extends RecyclerView.Adapter<UsersLeaderBordAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private Map<String, Group> groupsMap = new HashMap<>();
    private String gameType;
    private OnUserClickListener listener;

    public UsersLeaderBordAdapter() {
    }

    public UsersLeaderBordAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public UsersLeaderBordAdapter(String gameType) {
        this.gameType = gameType;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void setGroupsMap(Map<String, Group> groupsMap) {
        this.groupsMap = groupsMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = users.get(position);

        int rank = position + 1;
        holder.tvRank.setText("#" + rank);

        String fullName = user.getUsername();
        holder.tvFullName.setText(fullName);

        holder.tvNickname.setText("(" + user.getNickname() + ")");

        holder.tvGroupName.setText(getUserGroupName(user));


        int score;
        switch (gameType) {
            case "car":
                score = user.getCarGameRecord();
                break;

            case "snake":
                score = user.getSnakeRecord();
                break;

            case "piano":
                score = user.getPianoRecord();
                break;

            default:
                score = 0;
        }

        holder.tvExperience.setText(String.valueOf(score));
        holder.tvInitials.setText(getInitials(fullName));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private String getUserGroupName(User user) {

        if (!user.getInGroup() || groupsMap == null) {
            return "No Group";
        }

        if (user.getId() == null) {
            return "No Group";
        }

        for (Group group : groupsMap.values()) {
            if (group.getUserIds() != null && group.getUserIds().contains(user.getId())) {
                return group.getGroupName();
            }
        }

        return "No Group";
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";

        String[] parts = name.trim().split(" ");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRank, tvInitials, tvFullName, tvNickname, tvGroupName, tvExperience;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRank = itemView.findViewById(R.id.tv_rank);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            tvFullName = itemView.findViewById(R.id.tv_user_full_name);
            tvNickname = itemView.findViewById(R.id.tv_user_nickname);
            tvGroupName = itemView.findViewById(R.id.tv_user_group_name);
            tvExperience = itemView.findViewById(R.id.tv_user_experience);
        }
    }
}