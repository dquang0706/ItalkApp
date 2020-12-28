package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.adapter.AdapterPost;
import com.example.italkapp.model.ModelPost;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class HisProfileActivity extends AppCompatActivity {
    ImageView avataIv, coverIv, backIv, avatarPostIv;
    TextView nameTv, noPostTv, blockMessageTv, followTv;

    RecyclerView postRecyclerView;
    LinearLayout linearLayoutChat, blockMessageLayout;
    ArrayList<ModelPost> postsList;
    AdapterPost adapterPost;
    String hisUid, myUid;
    boolean checkBlock = false;
    boolean checkFollow = false;
    RelativeLayout realativeVietBai;

    LinearLayout followBtn;
    DatabaseReference followRef;
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his_profile);
        ColorStatusbar.setColorStatusBar(HisProfileActivity.this);


        postRecyclerView = findViewById(R.id.recyclerview_posts);
        avataIv = findViewById(R.id.avataIv);
        nameTv = findViewById(R.id.nameTv);
        blockMessageTv = findViewById(R.id.blockMessageTv);
        noPostTv = findViewById(R.id.noPostTv);
        coverIv = findViewById(R.id.coverIv);
        backIv = findViewById(R.id.backIv);
        linearLayoutChat = findViewById(R.id.linearLayoutChat);
        blockMessageLayout = findViewById(R.id.blockMessageLayout);
        realativeVietBai = findViewById(R.id.realativeVietBai);
        followBtn = findViewById(R.id.followBtn);
        followTv = findViewById(R.id.followTv);
        avatarPostIv = findViewById(R.id.avatarPostIv);
        postsList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        followRef = FirebaseDatabase.getInstance().getReference("Follows");
        Intent intent = getIntent();

        hisUid = intent.getStringExtra("hisUid");
        myUid = FirebaseAuth.getInstance().getUid();
        HomeActivity.getAvatar(HisProfileActivity.this, avatarPostIv);
        try {
            checkOnlineStatus("online");
            loadHisProfile();

            if (checkBlock == true) {
                blockMessageTv.setText(R.string.Unblock_message);
            }
            if (checkBlock == false) {
                blockMessageTv.setText(getString(R.string.block_message));

            }
            realativeVietBai.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(HisProfileActivity.this, AddPostActivity.class));
                }
            });
            linearLayoutChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBlockWhenSendMessage(hisUid);
                }
            });
            blockMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                            HisProfileActivity.this, R.style.BottomSheetDialogTheme
                    );
                    View bottomSheetView = LayoutInflater.from(HisProfileActivity.this).inflate(
                            R.layout.dialog_choose_image,
                            (LinearLayout) bottomSheetDialog.findViewById(R.id.bottomSheetContainer)
                    );


                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(50, 0, 50, 0);
                    bottomSheetView.setLayoutParams(params);

                    bottomSheetDialog.setCancelable(false);
                    bottomSheetView.findViewById(R.id.galleryBtn).setVisibility(View.GONE);
                    final TextView blockTv = bottomSheetView.findViewById(R.id.actionTv);
                    LinearLayout blockBtn = bottomSheetView.findViewById(R.id.cameraBtn);
                    checkIsBlocked(hisUid, blockTv);
                    if (checkBlock == true) {
                        blockTv.setText(R.string.unblock_message_this_people);
                    }
                    if (checkBlock == false) {
                        blockTv.setText(R.string.block_message_this_people);

                    }
                    blockBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkBlock = !checkBlock;
                            if (checkBlock == true) {
                                blockUser(hisUid);
                            }
                            if (checkBlock == false) {
                                unBlockUser(hisUid);
                            }
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();

                }
            });
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkFollow = true;
                    followRef.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (checkFollow) {
                                if (dataSnapshot.hasChild(hisUid)) {
                                    followRef.child(firebaseAuth.getUid()).child(hisUid).removeValue();
                                    checkFollow = false;
                                } else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("hisUid", hisUid);
                                    hashMap.put("status", "follow");
                                    followRef.child(firebaseAuth.getUid()).child(hisUid).setValue(hashMap);
                                    checkFollow = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.getMessage();
        }

        try {
            loadHisProfile();
            loadHisPost();
            SetTextFollow();
            checkUserStatus();

        } catch (Exception e) {
            e.getMessage();

        }

    }


    private void SetTextFollow() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(hisUid)) {
                    followTv.setText(R.string.Unfollow);
                } else {
                    followTv.setText(R.string.Follow);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadHisPost() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postsList.add(modelPost);
                    if (postsList.size() != 0) {
                        noPostTv.setVisibility(View.GONE);
                    } else {
                        noPostTv.setVisibility(View.VISIBLE);
                    }
                    adapterPost = new AdapterPost(HisProfileActivity.this, R.layout.row_post, postsList);
                    postRecyclerView.setAdapter(adapterPost);
                    adapterPost.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HisProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadHisProfile() {
        // đổ lại dữ liệu pfrofile của user được click vào post
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //lấy data đổ vào các view
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    nameTv.setText(name);

                    try {
                        if (image != "") {
                            Picasso.with(HisProfileActivity.this).load(image).placeholder(R.drawable.avatar_default).into(avataIv);
                        } else {
                            avataIv.setImageResource(R.drawable.avatar_default);
                        }
                    } catch (Exception e) {
                        avataIv.setImageResource(R.drawable.avatar_default);
                    }
                    try {
                        if (cover != "") {
                            Picasso.with(HisProfileActivity.this).load(cover).placeholder(R.drawable.ic_gallery_grey).into(coverIv);
                        } else {
                            coverIv.setImageResource(R.drawable.ic_gallery_grey);
                        }
                    } catch (Exception e) {
                        coverIv.setImageResource(R.drawable.ic_gallery_grey);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void blockUser(String hisUid) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedHisUsers").child(hisUid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HisProfileActivity.this, R.string.Block_Succesfuly, Toast.LENGTH_SHORT).show();
                blockMessageTv.setText(R.string.Unblock_message);

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HisProfileActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedHisUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(HisProfileActivity.this, R.string.Unblock_Succesfuly, Toast.LENGTH_SHORT).show();
                                        blockMessageTv.setText(getString(R.string.block_message));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(HisProfileActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();

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

    private void checkBlockWhenSendMessage(final String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedHisUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(HisProfileActivity.this, R.string.You_can_send_message, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent = new Intent(HisProfileActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkIsBlocked(final String hisUid, final TextView textView) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedHisUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                checkBlock = true;
                                textView.setText(R.string.unblock_message_this_people);
                            }
                            if (!ds.exists()) {
                                checkBlock = false;
                                textView.setText(R.string.block_message_this_people);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
        } else {
            startActivity(new Intent(HisProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        String timeStamp = "" + System.currentTimeMillis();
        checkOnlineStatus(timeStamp);
        super.onPause();
    }

    public void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);


    }
}
