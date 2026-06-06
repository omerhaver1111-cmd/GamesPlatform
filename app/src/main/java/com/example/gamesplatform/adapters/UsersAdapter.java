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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnUserClickListener listener;
    private String groupName;

    public UsersAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    /// פונקציה לעדכון המשתמשים
    public void setUsers(List<User> userList) {
        this.users = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvFullName.setText(user.getUsername());
        holder.tvNickname.setText("(@" + user.getNickname() + ")");
        holder.tvExperience.setText(user.getExp() + " XP");
        holder.tvGroupName.setText(groupName);

        holder.groupLayout.setVisibility(View.VISIBLE);
        String name = user.getUsername();
        if (name != null && !name.isEmpty()) {
            if (name.length() >= 2) {
                holder.tvInitials.setText(name.substring(0, 2).toUpperCase());
            } else {
                holder.tvInitials.setText(name.toUpperCase());
            }
        } else {
            holder.tvInitials.setText("***");
        }

        /// הגדרת מאזין ללחיצה
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongUserClick(user, position);
            }
            return false;
        });
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setUserList(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public void setUserAtPosition(int position, User user) {
        users.set(position, user);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnUserClickListener {
        void onUserClick(User user, int position);
        void onLongUserClick(User user, int position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvNickname, tvGroupName, tvExperience, tvInitials;
        View groupLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_user_full_name);
            tvNickname = itemView.findViewById(R.id.tv_user_nickname);
            tvGroupName = itemView.findViewById(R.id.tv_user_group_name);
            tvExperience = itemView.findViewById(R.id.tv_user_experience);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            groupLayout = itemView.findViewById(R.id.layout_group_info);
        }
    }
}