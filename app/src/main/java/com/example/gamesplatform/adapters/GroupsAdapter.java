package com.example.gamesplatform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private final List<Group> groups = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final OnGroupListener groupListener;

    public GroupsAdapter(@NonNull OnGroupListener groupListener) {
        this.groupListener = groupListener;
    }

    public void setGroups(List<Group> groupList) {
        groups.clear();
        groups.addAll(groupList);
        notifyDataSetChanged();
    }

    public void setUsers(List<User> userList) {
        users.clear();
        users.addAll(userList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        if (group == null) return;

        if (users.isEmpty()) return;

        holder.tvGroupName.setText(group.getGroupName());
        User leader = getLeaderUser(group);
        if (leader != null) {
            holder.tvLeaderName.setText(leader.getUsername());
        } else {
            holder.tvLeaderName.setText("Unknown");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupListener.onClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Nullable
    private User getLeaderUser(final Group group) {
        for (User user : users) {
            if (Objects.equals(user.getId(), group.leaderId))
                return user;
        }
        return null;
    }

    public interface OnGroupListener {
        public void onClick(Group group);
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView tvGroupName, tvLeaderName, tvInitials;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_item_group_name);
            tvLeaderName = itemView.findViewById(R.id.tv_item_user_leadername);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
        }
    }
}
