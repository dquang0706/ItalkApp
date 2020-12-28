package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.adapter.AdapterComments;
import com.example.italkapp.adapter.AdapterPost;
import com.example.italkapp.fragment.HomeFragment;
import com.example.italkapp.model.ModelComment;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PostDetailActivity extends AppCompatActivity {
    ImageView backIv;

    String hisUid, myUid, myEmail, myName,pDescr, pImage, myAvatar, postId, plikes, hisName, hisDp;

    ImageView uPictureIv, pImageIv, likeIv;
    TextView uNameTv, pTimeTiv, pDescriptionTv, pLikesTv, pCommentsTv, likeTv;
    ImageButton moreBtn;
    LinearLayout likeBtn, seeLikeBtn, hideCommentLayout;
    RelativeLayout profileLayout;

    EditText commentEt;
    ImageView sendBtn;
    ImageView cAvatarIv;
    RecyclerView recyclerView;
    public static AdapterComments adapterComments;
    ArrayList<ModelComment> commentList;


    FirebaseAuth firebaseAuth;

    private DatabaseReference postsRef;

    SweetAlertDialog sd;

    int count;
    boolean checkLike=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ColorStatusbar.setColorStatusBar(PostDetailActivity.this);

        backIv = findViewById(R.id.backIv);
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIvPost);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        likeIv = findViewById(R.id.likeIv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikeTv);
        likeTv = findViewById(R.id.likeTv);
        pCommentsTv = findViewById(R.id.pCommentTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        seeLikeBtn = findViewById(R.id.seeLikeBtn);
        profileLayout = findViewById(R.id.profileLayout);
        hideCommentLayout = findViewById(R.id.hideCommentLayout);
        recyclerView = findViewById(R.id.recyclerView);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.senBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        count = intent.getIntExtra("count", 0);

        commentList = new ArrayList<>();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        firebaseAuth = FirebaseAuth.getInstance();
        checkOnlineStatus("online");
        sd = new SweetAlertDialog(PostDetailActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(R.string.Loading);
        sd.setCancelable(true);
        try {
            loadPostInfo();
            checkUserStatus();
            loadUserInfo();
            setLikes(postId);
            setTextCoutLike();
            // gửi comment button click
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postComment();
                }
            });
            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkLiked();
                }
            });
            moreBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View view) {
                    showMoreOption();
                }
            });
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            seeLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PostDetailActivity.this, PostLikeByActivity.class);
                    intent.putExtra("postId", postId);
                    startActivity(intent);
                }
            });

            loadComment();
        }catch (Exception e){
            e.getMessage();
        }

    }

    public void checkLiked() {
        checkLike=true;
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (checkLike) {
                    if (dataSnapshot.child(postId).child("Likes").hasChild(myUid)) {
                        postsRef.child(postId).child("Likes").child(myUid).removeValue();
                        checkLike = false;
                    } else {
                        postsRef.child(postId).child("Likes").child(myUid).setValue(true);
                        count++;
                        addToNotifications(firebaseAuth.getUid(), postId, "like");
                        checkLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes( final String postKey) {
        postsRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).child("Likes").hasChild(myUid)) {
                   likeIv.setImageResource(R.drawable.ic_liked);
                } else {
                 likeIv.setImageResource(R.drawable.ic_like);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setTextCoutLike() {
        postsRef.child(postId).child("Likes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String cout = dataSnapshot.getChildrenCount() + "";
                        pLikesTv.setText(cout + " ");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOption() {
        PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, moreBtn, Gravity.END);
        // chỉ trạng thái của mình mới show lên
        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.Delete);
            popupMenu.getMenu().add(Menu.NONE, 1, 0, R.string.Edit);
        }
        if (!pDescr.equals("")) {
            popupMenu.getMenu().add(Menu.NONE, 2, 0, R.string.Copy_content);
        }
        if(!hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 3, 0, R.string.View_profile);

        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    deletePost();
                    Toast.makeText(PostDetailActivity.this, R.string.Delete_successful, Toast.LENGTH_SHORT).show();
                    HomeFragment.adapterPost.notifyDataSetChanged();
                    finish();
                }
                if (id == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                if(id==2){
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("String", pDescr);
                    clipboardManager.setPrimaryClip(clip);
                    clip.getDescription();
                    Toast.makeText(PostDetailActivity.this, R.string.Copied, Toast.LENGTH_SHORT).show();
                }
                if(id==3){
                    Intent intent = new Intent(PostDetailActivity.this, HisProfileActivity.class);
                    intent.putExtra("hisUid", hisUid);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void postComment() {
        sd.show();
        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, R.string.Please_Enter_comment, Toast.LENGTH_SHORT).show();
            sd.dismiss();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uAvatar", myAvatar);
        hashMap.put("uName", myName);
        // post dữu liệu lên firebase
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sd.dismiss();
                addToNotifications(hisUid, postId, "comment");
                commentEt.setText("");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
                Toast.makeText(PostDetailActivity.this, getString(R.string.Comment_failed) + " " + getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                commentEt.setText("");
            }
        });

    }

    private void loadComment() {
        //set layoutmanager for recyclerview
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear list before add comment
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    adapterComments = new AdapterComments(PostDetailActivity.this, commentList, myUid, postId);
                    recyclerView.setAdapter(adapterComments);
                }
                if (commentList.size() == 0) {
                    hideCommentLayout.setVisibility(View.VISIBLE);
                } else {
                    hideCommentLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addToNotifications(String hisUid, String pId, String typeNotification) {
        count++;
        if (!FirebaseAuth.getInstance().getUid().equals(hisUid)) {
            String timestamp = "" + System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pId", pId);
            hashMap.put("timestamp", timestamp);
            hashMap.put("hisId", hisUid);
            hashMap.put("typeNotification", typeNotification);
            hashMap.put("myId", myUid);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
            ref.child(hisUid).child(hisUid).child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

            ref.child(hisUid).child("NotificationCount").setValue(count + "");
            ref.child(hisUid).child("NotificationCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    count = Integer.parseInt(dataSnapshot.getValue() + "");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myAvatar = "" + ds.child("image").getValue();
                    try {
                        if (!myAvatar.equals("")) {
                            Picasso.with(PostDetailActivity.this).load(myAvatar).placeholder(R.drawable.avatar_default).into(cAvatarIv);
                        } else {
                            cAvatarIv.setImageResource(R.drawable.avatar_default);
                        }
                    } catch (Exception e) {
                        cAvatarIv.setImageResource(R.drawable.avatar_default);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadPostInfo() {
        // lấy bài đăng sử dụng id click vào post của mình
        Query query = postsRef.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    pDescr = "" + ds.child("pDescr").getValue();
                    plikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uAvatar").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    hisName = "" + ds.child("uName").getValue();


                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();

                    pDescriptionTv.setText(pDescr);
                    pTimeTiv.setText(pTime);
                    uNameTv.setText(hisName);
                    setCommentCount(postId, pCommentsTv);

                    if (pImage.equals("noImage")) {
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.with(PostDetailActivity.this).load(pImage).placeholder(R.drawable.ic_gallery_grey).into(pImageIv);
                        } catch (Exception e) {
                            pImageIv.setImageResource(R.drawable.ic_gallery_grey);
                        }
                    }
                    try {
                        if(hisDp.equals("")){
                            Picasso.with(PostDetailActivity.this).load(R.drawable.avatar_default).into(uPictureIv);

                        }else {
                            Picasso.with(PostDetailActivity.this).load(hisDp).placeholder(R.drawable.avatar_default).into(uPictureIv);

                        }
                    } catch (Exception e) {
                          e.getMessage();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setCommentCount(String postId, final TextView commentCountTv) {
        postsRef.child(postId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentCountTv.setText(dataSnapshot.getChildrenCount() + " " + getResources().getString(R.string.comment));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void deletePost() {
        if (pImage.equals("noImage")) {
            // status không hình ảnh
            deleteWithOutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.Loading));
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, R.string.Delete_successful, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        pd.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithOutImage() {
        sd.show();
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, R.string.Delete_successful, Toast.LENGTH_SHORT).show();
                sd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sd.dismiss();
            }
        });

    }

    public void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myEmail = user.getEmail();
            myUid = user.getUid();
        } else {
            startActivity(new Intent(PostDetailActivity.this, MainActivity.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    protected void onPause() {
        String timeStamp =""+System.currentTimeMillis();
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
