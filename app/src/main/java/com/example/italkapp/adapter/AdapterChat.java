package com.example.italkapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.ChatActivity;
import com.example.italkapp.HisProfileActivity;
import com.example.italkapp.R;
import com.example.italkapp.model.ModelChat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHoler>  {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;

    boolean showTime =false;


    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHoler(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHoler(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHoler holder, final int position) {
        String message   = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
        // gán dữ liệu

        if(type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Picasso.with(context).load(message).placeholder(R.drawable.ic_gallery_grey).into(holder.messageIv);
        }
        // gán dữ liệu

        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try {
            if(!imageUrl.equals("")){
                Picasso.with(context).load(imageUrl).placeholder(R.drawable.avatar_default).into(holder.profileIv);
            }else {
                holder.profileIv.setImageResource(R.drawable.avatar_default);

            }
        } catch (Exception e) {
             holder.profileIv.setImageResource(R.drawable.avatar_default);
        }
         //bắt trạng thái xem hoặc chưa xem tin nhắn
        if (chatList.size() - 1==position) {
            if (chatList.get(position).isSeen()) {
                holder.isSeenTv.setText(R.string.Seen);
            } else {
                holder.isSeenTv.setText(R.string.Sent);
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

        holder.timeTv.setVisibility(View.INVISIBLE);
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                   showTime =!showTime;
                   if(getItemViewType(position)==1){
                       if(showTime ==true){
                           holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_right));
                           holder.timeTv.setVisibility(View.INVISIBLE);
                           Animation animation=AnimationUtils.loadAnimation(context,R.anim.popdown);
                           holder.timeTv.setAnimation(animation);
                       }
                       if(showTime ==false){
                           holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_right_bold));
                           holder.timeTv.setVisibility(View.VISIBLE);
                           Animation animation=AnimationUtils.loadAnimation(context,R.anim.popup);
                           holder.timeTv.setAnimation(animation);
                       }
                   }
                if(getItemViewType(position)==0){
                    if(showTime ==true){
                        holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_left));
                        holder.timeTv.setVisibility(View.INVISIBLE);
                        Animation animation=AnimationUtils.loadAnimation(context,R.anim.popdown);
                        holder.timeTv.setAnimation(animation);
                    }
                    if(showTime ==false){
                        holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_left_bold));
                        holder.timeTv.setVisibility(View.VISIBLE);
                        Animation animation=AnimationUtils.loadAnimation(context,R.anim.popup);
                        holder.timeTv.setAnimation(animation);
                    }
                }

            }
        });
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
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
                confirmTv.setText(context.getString(R.string.are_you_delete_this_message));
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                deletelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteMessage(position);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }
        });
        holder.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HisProfileActivity.class);
                intent.putExtra("hisUid", chatList.get(position).getSender());
                context.startActivity(intent);
            }
        });
    }
    private void deleteMessage(int position) {
        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp=chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){

                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, R.string.Deleted, Toast.LENGTH_SHORT).show();
                                ChatActivity.adapterChat.notifyDataSetChanged();
                            }
                        });
                    }else {
                        Toast.makeText(context, R.string.You_just_only_delete_your_messages, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHoler extends RecyclerView.ViewHolder {
        ImageView profileIv,messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout;


        public MyHoler(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeendTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageIv=itemView.findViewById(R.id.messageIv);
        }
    }
}
