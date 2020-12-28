package com.example.italkapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.HisProfileActivity;
import com.example.italkapp.ListBlockActivity;
import com.example.italkapp.R;
import com.example.italkapp.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterBlock extends RecyclerView.Adapter<AdapterBlock.MyHoler>{
    Context context;
    ArrayList<ModelUser> userList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterBlock(Context context, ArrayList<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }


    @NonNull
    @Override
    public AdapterBlock.MyHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);
        return new AdapterBlock.MyHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHoler holder, final int position) {
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();
        String online=userList.get(position).getOnlineStatus();

        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(holder.mAvatarIv);
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
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(context).inflate(
                        R.layout.dialog_choose_image,
                        (LinearLayout) bottomSheetDialog.findViewById(R.id.bottomSheetContainer)
                );
                FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(50,0,50,0);
                bottomSheetDialog.setCancelable(false);

                bottomSheetView.setLayoutParams(params);
                bottomSheetView.findViewById(R.id.galleryBtn).setVisibility(View.GONE);
                LinearLayout unBlockBtn=bottomSheetView.findViewById(R.id.cameraBtn);
                TextView unblockTv=bottomSheetView.findViewById(R.id.actionTv);
                unblockTv.setText(R.string.unblock_message_this_people);
                LinearLayout cancelBtn=bottomSheetView.findViewById(R.id.cancelBtn);
                unBlockBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            unBlockUser(hisUid);
                            bottomSheetDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       bottomSheetDialog.dismiss();
                    }
                });


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    private void unBlockUser(String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedHisUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ListBlockActivity.adapterBlock.notifyDataSetChanged();

                                        Toast.makeText(context, R.string.unblock_succesfuly, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, R.string.Error, Toast.LENGTH_SHORT).show();

                                    }
                                });
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
        return userList.size();
    }

    class MyHoler extends RecyclerView.ViewHolder {
        ImageView mAvatarIv, blockIv,onlineIv;
        TextView mNameTv, mEmailTv;

        public MyHoler(@NonNull View itemView) {
            super(itemView);
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTvrow);
            blockIv = itemView.findViewById(R.id.blockIv);

        }
    }
}
