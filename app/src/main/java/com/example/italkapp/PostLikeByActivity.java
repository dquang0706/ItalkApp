package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.italkapp.adapter.AdapterUser;
import com.example.italkapp.model.ModelUser;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PostLikeByActivity extends AppCompatActivity {
    String postId;
    RecyclerView recyclerView;
    ArrayList<ModelUser> userList;
    AdapterUser adapterUser;
    FirebaseUser firebaseUser;
    ImageView backIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like_by);
        ColorStatusbar.setColorStatusBar(PostLikeByActivity.this);

        recyclerView = findViewById(R.id.recyclerview);
        backIv = findViewById(R.id.backIv);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        userList=new ArrayList<>();
        checkOnlineStatus("online");
        Intent intent = getIntent();
        // lấy id của bài đăng từ post chuyển qua
        postId = intent.getStringExtra("postId");
        try {
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(postId).child("Likes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        String hisUid=""+ds.getRef().getKey();
                        // lấy mã id trong nhứng người đã like có trong bảng Likes
                        userList.clear();
                        getUserLikePost(hisUid);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PostLikeByActivity.this, getString(R.string.Error), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }


        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void getUserLikePost(String hisUid){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);
                    userList.add(modelUser);
                }
                adapterUser=new AdapterUser(PostLikeByActivity.this,R.layout.row_user,userList);
                recyclerView.setAdapter(adapterUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostLikeByActivity.this, ""+databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void checkOnlineStatus(String status) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);


    }
}
