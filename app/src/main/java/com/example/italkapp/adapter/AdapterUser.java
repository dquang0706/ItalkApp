package com.example.italkapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.ChatActivity;
import com.example.italkapp.R;
import com.example.italkapp.HisProfileActivity;
import com.example.italkapp.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHoler> {
    Context context;
    int layout;
    ArrayList<ModelUser> userList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUser(Context context,int layout, ArrayList<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        this.layout=layout;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new MyHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHoler holder, final int position) {
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();
        String online=userList.get(position).getOnlineStatus();

       if(layout==R.layout.row_user){
           holder.mNameTv.setText(userName);
           holder.mEmailTv.setText(userEmail);
           try {
               if(!userImage.equals("")){
                   Picasso.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(holder.mAvatarIv);
               }else {
                   holder.mAvatarIv.setImageResource(R.drawable.avatar_default);

               }
           } catch (Exception e) {
               holder.mAvatarIv.setImageResource(R.drawable.avatar_default);
           }

           if(online.equals("online")){
               holder.onlineIv.setImageResource(R.drawable.circle_online);
           }else {
               holder.onlineIv.setImageResource(R.drawable.circle_ofline);

           }
           holder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(context, HisProfileActivity.class);
                   intent.putExtra("hisUid", hisUid);
                   context.startActivity(intent);
               }
           });

       }

        if(layout==R.layout.row_avatar_user){
            userName.trim();
            String[] name = userName.split("\\s+");

            holder.mNameTv.setText(name[name.length-1]);
            try {
                if(!userImage.equals("")){
                    Picasso.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(holder.avatarIv);
                }else {
                    holder.avatarIv.setImageResource(R.drawable.avatar_default);

                }
            } catch (Exception e) {
                holder.avatarIv.setImageResource(R.drawable.avatar_default);
            }
            if(online.equals("online")){
                holder.onlineIv.setImageResource(R.drawable.circle_online);
            }else {
                holder.onlineIv.setImageResource(R.drawable.circle_ofline);

            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("hisUid", hisUid);
                    context.startActivity(intent);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHoler extends RecyclerView.ViewHolder {
        ImageView mAvatarIv, blockIv,onlineIv;
        TextView mNameTv, mEmailTv;
        ImageView avatarIv;

        public MyHoler(@NonNull View itemView) {
            super(itemView);
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTvrow);
            blockIv = itemView.findViewById(R.id.blockIv);
            avatarIv = itemView.findViewById(R.id.avatarIv);




        }
    }
}
