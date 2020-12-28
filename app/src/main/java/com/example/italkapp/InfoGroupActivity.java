package com.example.italkapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoGroupActivity extends AppCompatActivity {
    private CircleImageView groupIconIv;
    private TextView groupNameTv, groupDescriptionTv,leaveGroupTv;
    private String groupId;
    private RelativeLayout finishLayout, addMemberLayout,viewMemberLayout,leaveGroupLayout,changeGroupNameLayout;
    private String myGroupRole;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_group);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ColorStatusbar.setColorStatusBar(InfoGroupActivity.this);
        init();

        firebaseAuth = FirebaseAuth.getInstance();
        // get groupId from GroupChatActivity
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        try {
            loadGroupRole();
            loadGroupInfo();
        }catch (Exception e){

        }



        // Finish activity
        finishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Add member
        addMemberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoGroupActivity.this, GroupMemberAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        // View list member
        viewMemberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoGroupActivity.this, ListMemberActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        // Leave group
        leaveGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog=new Dialog(InfoGroupActivity.this);
                dialog.setContentView(R.layout.dialog_confirm);
                Window window = dialog.getWindow();
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (dialog != null && dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                TextView confirmTv=dialog.findViewById(R.id.confirmTv);
                TextView messagCfTv=dialog.findViewById(R.id.messagCfTv);
                TextView cancelTv=dialog.findViewById(R.id.cancelTv);
                TextView leaveTv=dialog.findViewById(R.id.updatetv);
                leaveTv.setTextColor(getResources().getColor(R.color.blackNhat2));
                cancelTv.setTextColor(getResources().getColor(R.color.red));
                LinearLayout cancelBtn=dialog.findViewById(R.id.cancelBtn);
                LinearLayout leaveBtn=dialog.findViewById(R.id.deleteBtn);

                if (myGroupRole.equals("creator")){
                    confirmTv.setText(R.string.Delete_group);
                    messagCfTv.setText(R.string.Are_you_sure_want_to_Delete_group);
                    leaveTv.setText(getString(R.string.delete));
                }
                else{
                    confirmTv.setText(R.string.leave_group);
                    messagCfTv.setText(R.string.Are_you_sure_want_to_Leave_group);
                    leaveTv.setText(getString(R.string.leave_group));

                }
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                leaveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (myGroupRole.equals("creator")){
                            deleteGroup();
                        }else{

                            leaveGroup();
                        }
                    }
                });
                dialog.show();


            }
        });
        // Edit group info
        changeGroupNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoGroupActivity.this,EditGroupActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
//        checkOnlineStatus("online");
    }



    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InfoGroupActivity.this, R.string.Group_left_successfully, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(InfoGroupActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InfoGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InfoGroupActivity.this, R.string.Group_deleted, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(InfoGroupActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InfoGroupActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init() {
        groupIconIv = findViewById(R.id.groupIcon);
        groupNameTv = findViewById(R.id.groupNameTv);
        finishLayout = findViewById(R.id.finishLayout);
        addMemberLayout = findViewById(R.id.addMemberLayout);
        groupDescriptionTv = findViewById(R.id.groupDescriptionTv);
        viewMemberLayout = findViewById(R.id.viewMemberLayout);
        leaveGroupLayout = findViewById(R.id.leaveGroupLayout);
        changeGroupNameLayout = findViewById(R.id.changeGroupNameLayout);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String groupName = "" + ds.child("groupName").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            groupNameTv.setText(groupName);
                            groupDescriptionTv.setText(groupDescription);
                            try {
                                Picasso.with(InfoGroupActivity.this).load(groupIcon).placeholder(R.drawable.ic_app2).into(groupIconIv);
                            } catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.ic_app2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            myGroupRole = ""+dataSnapshot.child("role").getValue();
                            if (myGroupRole.equals("creator")){
                                leaveGroupTv.setText(R.string.Delete_group);
                            }
                            else{
                                leaveGroupTv.setText(R.string.leave_group);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

//    @Override
//    protected void onPause() {
//        String timeStamp =""+System.currentTimeMillis();
//        checkOnlineStatus(timeStamp);
//        super.onPause();
//    }
//
//    public void checkOnlineStatus(String status) {
//
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("onlineStatus", status);
//        //cap nhat trang thai cua user
//        dbRef.updateChildren(hashMap);
//
//
//    }
}