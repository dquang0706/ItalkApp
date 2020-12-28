package com.example.italkapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.ChatActivity;
import com.example.italkapp.R;
import com.example.italkapp.fragment.ChatOneUserFragment;
import com.example.italkapp.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterOneChatlist extends RecyclerView.Adapter<AdapterOneChatlist.MyHolder> {
    Context context;
    List<ModelUser> userList;
    private HashMap<String, String> lastMessageMap;
    FirebaseUser user;


    public AdapterOneChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        this.lastMessageMap = new HashMap<>();
        user= FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String hisUid = userList.get(position).getUid();
//        Log.d("ggggggggggggg","Adapter: "+ userList.get(position).getUid());
        final String userImage =userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(holder.profileIv);
        } catch (Exception e) {
            holder.profileIv.setImageResource(R.drawable.avatar_default);

        }
        //set online status of other user in chat list
        if (userList.get(position).getOnlineStatus().equals("online")) {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        } else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_ofline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                        final Dialog dialog=new Dialog(context);
                        dialog.setContentView(R.layout.dialog_confirm);
                        Window window = dialog.getWindow();
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (dialog != null && dialog.getWindow() != null) {
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        }
                        TextView confirmTv=dialog.findViewById(R.id.messagCfTv);
                        LinearLayout cancelBtn=dialog.findViewById(R.id.cancelBtn);
                        LinearLayout deletelBtn=dialog.findViewById(R.id.deleteBtn);
                        confirmTv.setText(context.getString(R.string.are_you_delete_this_conversation));
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        deletelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("ChatList");
                                reference.child(user.getUid()).child(hisUid).removeValue();
                                Toast.makeText(context, context.getString(R.string.Delete_successful), Toast.LENGTH_SHORT).show();
                                ChatOneUserFragment.adapterChatlist.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                return false;
            }
        });

    }
  public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);
  }
    @Override
    public int getItemCount() {
        return userList.size();
    }
    class MyHolder extends RecyclerView.ViewHolder {
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);

        }
    }
}
