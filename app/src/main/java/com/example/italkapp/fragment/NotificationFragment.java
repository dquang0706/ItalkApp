package com.example.italkapp.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.italkapp.R;
import com.example.italkapp.adapter.AdapterNotification;
import com.example.italkapp.model.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {
View view;
ArrayList<ModelNotification> notificationList;
public  static  AdapterNotification adapterNotification;
RecyclerView recyclerView;
LinearLayout noNotificationLayout;
DatabaseReference reference;
String myUid;
FirebaseAuth firebaseAuth;

ImageView backIv;

    public NotificationFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view=inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView=view.findViewById(R.id.recyclerviewNotification);
        noNotificationLayout=view.findViewById(R.id.noNotificationLayout);
        backIv=view.findViewById(R.id.backIv);
        notificationList=new ArrayList<>();
        myUid=FirebaseAuth.getInstance().getUid();

        try {
            reference= FirebaseDatabase.getInstance().getReference("Notifications");
            reference.child(myUid).child(myUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notificationList.clear();
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        ModelNotification modelNotification=ds.getValue(ModelNotification.class);
                        notificationList.add(modelNotification);
                    }
                    if(notificationList.size()==0){
                        noNotificationLayout.setVisibility(View.VISIBLE);
                    }else {
                        noNotificationLayout.setVisibility(View.GONE);
                    }
                    adapterNotification=new AdapterNotification(getContext(),notificationList);
                    recyclerView.setAdapter(adapterNotification);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().onBackPressed();
                    getActivity().finish();
                }
            });
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }




        return view;
    }
}
