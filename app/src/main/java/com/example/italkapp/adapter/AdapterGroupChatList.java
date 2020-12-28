package com.example.italkapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.GroupChatActivity;
import com.example.italkapp.R;
import com.example.italkapp.model.ModelGroupChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.ViewHolder> {
    @NonNull
    private Context context;
    private List<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(@NonNull Context context, List<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @Override
    public AdapterGroupChatList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chatlist, parent, false);
        return new AdapterGroupChatList.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGroupChatList.ViewHolder holder, int position) {
        ModelGroupChatList modelGroupChatList = groupChatLists.get(position);
        final String groupId = modelGroupChatList.getGroupId();
        final String groupIcon = modelGroupChatList.getGroupIcon();
        String groupName = modelGroupChatList.getGroupName();

        //set data
        holder.groupNameTv.setText(groupName);
        try {
            Picasso.with(context).load(groupIcon).placeholder(R.drawable.ic_app2).into(holder.groupIv);
        } catch (Exception e) {
            holder.groupIv.setImageResource(R.drawable.ic_app2);
        }
        holder.lastMessageTv.setText("");
        holder.sender.setText("");
        // Load last message and username
        loadLastMessage(modelGroupChatList, holder);

        // intent to ChatGroupActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(ModelGroupChatList modelGroupChatList, final ViewHolder holder) {
        // get last mess from Groups
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(modelGroupChatList.getGroupId()).child("Messages").limitToLast(1) // get last item from that child
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            // get data
                            String message = "" + ds.child("message").getValue();
                            String sender = "" + ds.child("sender").getValue();
                            String type = "" + ds.child("type").getValue();
                            Log.i("type", "onDataChange: " + type);
                            if (type.equals("text")) {
                                holder.lastMessageTv.setText(message);
                            } else {
                                holder.lastMessageTv.setText(context.getString(R.string.Send_a_photo));
                            }
                            // set data

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                String senderName = "" + ds.child("name").getValue();
                                                holder.sender.setText(senderName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupIv;
        TextView groupNameTv, sender, lastMessageTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIv = itemView.findViewById(R.id.groupChatIv);
            groupNameTv = itemView.findViewById(R.id.groupNameTv);
            sender = itemView.findViewById(R.id.senderName);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
