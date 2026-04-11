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

//public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
//
//    private Context context;
//    private List<GroupWithId> groups;
//    private DatabaseService databaseService;
//    private OnGroupClickListener listener;
//
//    public interface OnGroupClickListener {
//        void onGroupClick(GroupWithId group);
//        void onMoreOptionsClick(GroupWithId group);
//    }
//
//    public static class GroupWithId {
//        public String groupId;
//        public Group group;
//
//        public GroupWithId(String groupId, Group group) {
//            this.groupId = groupId;
//            this.group = group;
//        }
//    }
//
//    public GroupsAdapter(Context context, DatabaseService databaseService, OnGroupClickListener listener) {
//        this.context = context;
//        this.groups = new ArrayList<>();
//        this.databaseService = databaseService;
//        this.listener = listener;
//    }
//
//    public void setGroups(List<GroupWithId> groups) {
//        this.groups = groups;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
//        return new GroupViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
//        GroupWithId groupWithId = groups.get(position);
//        holder.bind(groupWithId);
//    }
//
//    @Override
//    public int getItemCount() {
//        return groups.size();
//    }
//
//    class GroupViewHolder extends RecyclerView.ViewHolder {
//        TextView tvUserInitials;
//        TextView tvGroupName;
//        TextView tvLeaderName;
//        ImageView ivMoreOptions;
//        View cardView;
//
//        public GroupViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvUserInitials = itemView.findViewById(R.id.tv_user_initials);
//            tvGroupName = itemView.findViewById(R.id.tv_item_group_name);
//            tvLeaderName = itemView.findViewById(R.id.tv_item_user_leadername);
//            //ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
//            cardView = itemView;
//        }
//
//        public void bind(GroupWithId groupWithId) {
//            Group group = groupWithId.group;
//
//            // Set group name
//            tvGroupName.setText(group.groupName != null ? group.groupName : "Unnamed Group");
//
//            // Get initials from group name
//            String initials = getInitials(group.groupName);
//            tvUserInitials.setText(initials);
//
//            // Load leader name
//            if (group.leaderId != null) {
//                databaseService.getUser(group.leaderId, new DatabaseService.DatabaseCallback<User>() {
//                    @Override
//                    public void onCompleted(User user) {
//                        String leaderName = user.getUsername() != null ? user.getUsername() : "Unknown Leader";
//                        tvLeaderName.setText(leaderName);
//                    }
//
//                    @Override
//                    public void onFailed(Exception e) {
//                        tvLeaderName.setText("Unknown Leader");
//                    }
//                });
//            } else {
//                tvLeaderName.setText("No Leader");
//            }
//
//            // Set click listeners
//            cardView.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onGroupClick(groupWithId);
//                }
//            });
//
//            ivMoreOptions.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onMoreOptionsClick(groupWithId);
//                }
//            });
//        }
//
//        private String getInitials(String name) {
//            if (name == null || name.trim().isEmpty()) {
//                return "??";
//            }
//
//            String[] words = name.trim().split("\\s+");
//            if (words.length == 1) {
//                return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
//            } else {
//                String first = words[0].substring(0, 1);
//                String second = words[1].substring(0, 1);
//                return (first + second).toUpperCase();
//            }
//        }
//    }
//}

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
        holder.tvLeaderName.setText(getLeaderUser(group).getUsername());

//        holder.tvLeaderName.setText(getLeaderUser(group).getUsername());

        // ראשי תיבות (GN)
        String initials = group.getGroupName().length() >= 2
                ? group.getGroupName().substring(0, 2).toUpperCase()
                : group.getGroupName().toUpperCase();
        holder.tvInitials.setText(initials);

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
