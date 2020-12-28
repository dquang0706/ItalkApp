package com.example.italkapp.adapter;

import android.app.Dialog;
import android.content.Context;
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

import com.example.italkapp.GroupChatActivity;
import com.example.italkapp.R;
import com.example.italkapp.model.ModelGroupChat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.ViewHolder> {
    @NonNull
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<ModelGroupChat> modelGroupChatList;
    private FirebaseAuth firebaseAuth;
    private String userImage;
    boolean showTime =false;
    public AdapterGroupChat(Context context, List<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new ViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterGroupChat.ViewHolder holder, final int position) {
        holder.isSeenTv.setVisibility(View.GONE);
        // get data
        ModelGroupChat model = modelGroupChatList.get(position);
        String message = model.getMessage();
        String senderUid = model.getSender();
        String type = model.getType();
        String timeStamp = model.getTimeStamp();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
        // Check input type
        if (type.equals("text")) {
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        } else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Picasso.with(context).load(message).placeholder(R.drawable.ic_gallery_grey).into(holder.messageIv);
        }
        // set data
        holder.timeTv.setText(dateTime);
        holder.messageTv.setText(message);
        setUserAvatar(model, holder);

        // Click on message show time
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
                        Animation animation= AnimationUtils.loadAnimation(context, R.anim.popdown);
                        holder.timeTv.setAnimation(animation);
                    }
                    if(showTime ==false){
                        holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_right_bold));
                        holder.timeTv.setVisibility(View.VISIBLE);
                        Animation animation=AnimationUtils.loadAnimation(context, R.anim.popup);
                        holder.timeTv.setAnimation(animation);
                    }
                }
                if(getItemViewType(position)==0){
                    if(showTime ==true){
                        holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_left));
                        holder.timeTv.setVisibility(View.INVISIBLE);
                        Animation animation=AnimationUtils.loadAnimation(context, R.anim.popdown);
                        holder.timeTv.setAnimation(animation);
                    }
                    if(showTime ==false){
                        holder.messageTv.setBackgroundDrawable(context.getDrawable(R.drawable.background_row_chat_left_bold));
                        holder.timeTv.setVisibility(View.VISIBLE);
                        Animation animation=AnimationUtils.loadAnimation(context, R.anim.popup);
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
                LinearLayout cancelBtn=dialog.findViewById(R.id.cancelBtn);
                LinearLayout deletelBtn=dialog.findViewById(R.id.deleteBtn);
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
    }
    private void deleteMessage(int position) {
        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp=modelGroupChatList.get(position).getTimeStamp();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        Query query=ref.child(GroupChatActivity.groupId).child("Messages").orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){

                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, R.string.Deleted, Toast.LENGTH_SHORT).show();
                                GroupChatActivity.adapterGroupChat.notifyDataSetChanged();
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
    private void setUserAvatar(ModelGroupChat model, final ViewHolder holder) {
        // Get sender info from uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            userImage = "" + ds.child("image").getValue();
                        }
                        try {
                            Picasso.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(holder.profileIv);
                        } catch (Exception e) {
                            holder.profileIv.setImageResource(R.drawable.avatar_default);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, context.getString(R.string.Error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv,isSeenTv;
        LinearLayout messageLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeendTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }
}
