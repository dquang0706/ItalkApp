package com.example.italkapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
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
import com.example.italkapp.PostDetailActivity;
import com.example.italkapp.R;
import com.example.italkapp.fragment.NotificationFragment;
import com.example.italkapp.model.ModelNotification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.ViewHolder> {
    Context context;
    ArrayList<ModelNotification> notificationList;
    DatabaseReference reference;
    SweetAlertDialog sd;
    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        reference = FirebaseDatabase.getInstance().getReference("Users");
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        sd = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(context.getResources().getString(R.string.LOADING));
        sd.setCancelable(false);

        final ModelNotification modelNotification = notificationList.get(position);
        final String hisId = modelNotification.getMyId();
        final String timestamp = modelNotification.getTimestamp();
        final String pId = modelNotification.getpId();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        final String pTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();

        loadNameAndImageHisUserNotification(hisId,holder.nameTv,holder.avatarIv);

        if(modelNotification.getTypeNotification().equals("like")){
            holder.notificationTv.setText(context.getResources().getString(R.string.Like_your_post));
        }
        if(modelNotification.getTypeNotification().equals("comment")){
            holder.notificationTv.setText(context.getResources().getString(R.string.Comment_your_post));
        }
        holder.timeTv.setText(pTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);  // truyền bài đăng có id này qua detailactivity
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final Dialog dialog = new android.app.Dialog(context);
                dialog.setContentView(R.layout.dialog_confirm);
                Window window = dialog.getWindow();
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (dialog != null && dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
                LinearLayout deletelBtn = dialog.findViewById(R.id.deleteBtn);
                TextView confirmTv = dialog.findViewById(R.id.messagCfTv);
                confirmTv.setText(R.string.Do_you_want_delete_this_notification);
                deletelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sd.show();
                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
                        ref.child(FirebaseAuth.getInstance().getUid()).child(FirebaseAuth.getInstance().getUid()).child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, context.getResources().getString(R.string.Delete_successful), Toast.LENGTH_SHORT).show();
                                NotificationFragment.adapterNotification.notifyDataSetChanged();
                                dialog.dismiss();
                                if (notificationList.size() == 0) {
                                    ref.child(FirebaseAuth.getInstance().getUid()).removeValue();
                                }
                                sd.dismiss();

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        sd.dismiss();
                                    }
                                });
                        dialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        sd.dismiss();
                    }
                });
                dialog.show();

                return false;
            }
        });
    }

    private void loadNameAndImageHisUserNotification(String hisId, final TextView nameTv, final ImageView avatarIv) {
        reference.orderByChild("uid").equalTo(hisId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    nameTv.setText(name);
                    try {
                        if (image.equals("")) {
                         avatarIv.setImageResource(R.drawable.avatar_default);
                        } else {
                            Picasso.with(context).load(image).placeholder(R.drawable.avatar_default).into(avatarIv);
                        }
                    } catch (Exception e) {
                        avatarIv.setImageResource(R.drawable.avatar_default);
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
        return notificationList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
