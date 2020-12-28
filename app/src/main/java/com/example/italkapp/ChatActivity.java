package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.italkapp.adapter.AdapterChat;
import com.example.italkapp.model.ModelChat;
import com.example.italkapp.notification.Data;
import com.example.italkapp.notification.Sender;
import com.example.italkapp.notification.Token;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChatActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView profileIv,attachBtn;
    ImageButton  microphoneBtn;
    TextView nameTv, userStatusTv,ChatConnectionTV;
    ImageView sendBtn;
    EditText messageEt;


    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    public static AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    SweetAlertDialog sd;
    private static final int REQUEST_CODE_SPEECH = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    private String[] cameraPermission;
    private String[] storagePermission;
    Uri image_uri = null;


    RequestQueue requestQueue;
    SharedPreferences sp;
    private static final String TOPIC_POST_NOTIFICATION = "POST";
    private ConnectivityReceiver connectivityReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_chat);
        ColorStatusbar.setColorStatusBar(ChatActivity.this);
//        sp=getSharedPreferences("Notification_SP",MODE_PRIVATE);
//        notification=sp.getBoolean(""+TOPIC_POST_NOTIFICATION,false);

        recyclerView = findViewById(R.id.chat_Recyclerview);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        sendBtn = findViewById(R.id.senBtn);
        messageEt = findViewById(R.id.messageEt);
        attachBtn = findViewById(R.id.attachBtn);
        microphoneBtn = findViewById(R.id.microphoneBtn);
        ChatConnectionTV = findViewById(R.id.ChatConnectionTV);
        recyclerView.setHasFixedSize(true);


        requestQueue = Volley.newRequestQueue(getApplicationContext());

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        sd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(getString(R.string.Loading));
        sd.setCancelable(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");
        myUid = firebaseAuth.getUid();

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        try {
            checkOnlineStatus("online");


            Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
            userQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = "" + ds.child("name").getValue();
                        hisImage = "" + ds.child("image").getValue();
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        String typingStatus = "" + ds.child("typingTo").getValue();

                        nameTv.setText(name);
                        if (typingStatus.equals(myUid)) {
                            userStatusTv.setText(R.string.Typing);
                            userStatusTv.setTextColor(getResources().getColor(R.color.main));
                            userStatusTv.setTypeface(null, Typeface.BOLD);
                        } else {
                            if (onlineStatus.equals("online")) {
                                userStatusTv.setText(R.string.online);
                                userStatusTv.setTextColor(getResources().getColor(R.color.grey));
                                userStatusTv.setTypeface(null, Typeface.NORMAL);

                            } else {
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(onlineStatus));
                                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                                userStatusTv.setText(getString(R.string.Offline_at) + dateTime);
                            }
                        }


                        try {
                            Picasso.with(ChatActivity.this).load(hisImage).placeholder(R.drawable.avatar_default).into(profileIv);
                        } catch (Exception e) {
                            profileIv.setImageResource(R.drawable.avatar_default);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            profileIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ChatActivity.this, HisProfileActivity.class);
                    intent.putExtra("hisUid", hisUid);
                    startActivity(intent);
                }
            });
            microphoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenVoice();
                }
            });
            attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImagePickDialog();

                }
            });
            sendBtn.setVisibility(View.INVISIBLE);
            messageEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0) {
                        sendBtn.setEnabled(true);
                        sendBtn.setVisibility(View.VISIBLE);
                        checkTyping(hisUid);
                    } else {
                        sendBtn.setEnabled(false);
                        sendBtn.setVisibility(View.INVISIBLE);
                        checkTyping("noOne");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = messageEt.getText().toString().trim();
                    if (TextUtils.isEmpty(message)) {
                        Toast.makeText(ChatActivity.this, R.string.Please_Enter_your_message, Toast.LENGTH_SHORT).show();
                    } else {
                        sendMessage(message);
                    }
                    messageEt.setText("");
                }
            });
            readMessage();
            seenMessage();
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }



    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        }
    }
    private void listenVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.Say_something));
        try {
            startActivityForResult(i, REQUEST_CODE_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(ChatActivity.this, R.string.noSuport, Toast.LENGTH_SHORT).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }
    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHasmap = new HashMap<>();
                        hasSeenHasmap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHasmap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);
    }
    public void checkTyping(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }
    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    recyclerView.setAdapter(adapterChat);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            recyclerView.scrollToPosition(chatList.size()-1);
                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void sendMessage(final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);

        checkNotificationWhenSentText(message);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid)
                .child(hisUid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisUid)
                .child(myUid);
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void sendImageMessage(Uri image_uri) throws IOException {
        sd.show();
        final String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;
        // get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray(); // convert image to byte
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image upload thành công
                sd.dismiss();
                // get uri of upload image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    // add image anc other info database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver", hisUid);
                    hashMap.put("message", downloadUri);
                    hashMap.put("timestamp", timeStamp);
                    hashMap.put("type", "image");
                    hashMap.put("isSeen", false);

                    databaseReference.child("Chats").push().setValue(hashMap);

                   checkNotificationWhenSentImage();


                    final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                            .child(hisUid)
                            .child(myUid);
                    chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
            }
        });

    }
    public void checkNotificationWhenSentText(final String message){
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String chatNotificationStatus = "" + ds.child("chatNotification").getValue();
                    if(chatNotificationStatus.equals("enable")){
                        addNotiticationWhenSend(message);
                    }
                    if(chatNotificationStatus.equals("disable")){
                      break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void checkNotificationWhenSentImage(){
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String chatNotificationStatus = "" + ds.child("chatNotification").getValue();
                    if(chatNotificationStatus.equals("enable")){
                        addNotiticationWhenSend(getString(R.string.Send_a_photo));
                    }
                    if(chatNotificationStatus.equals("disable")){
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void addNotiticationWhenSend(final String message) {
        Query query = userDbRef.orderByChild("uid").equalTo(myUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    final String name = ds.child("name").getValue() + "";
                    senNotification(hisUid, name, message);
                }
            }
            @Override

            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void senNotification(final String hisUidchat, final String name, final String message) {

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisUid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data("" + myUid,
                            name + ": "
                                    + message,
                            getString(R.string.New_message),
                            "" + hisUidchat,
                            "ChatNotification",
                            R.drawable.ic_notification_active);

                    Sender sender = new Sender(data, token.getToken());
                    //fcm jsonObject request
                    try {
                        JSONObject senderJsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // response of the request
                                        Log.d("JSON_RESPONSE", "onresponse" + response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onresponse" + error.toString());

                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content_Type", "application/json");
                                headers.put("Authorization", "key=AAAA86Lm6hE:APA91bHhVV4R8w8ItzEmK_l7-iEoiGcE02xD2tWMDQ6o73oy9P6iil0H6VtWxPIioGnGKAiorV0pGSUPIX1RNnuQlydSYlDQnKJsJBgonExXiRYm-qVBII4SAxiQ0SsVOv37ZLafIqVo");
                                return headers;
                            }
                        };
                            requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void showImagePickDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.dialog_choose_image,
                (LinearLayout) bottomSheetDialog.findViewById(R.id.bottomSheetContainer)
        );
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        bottomSheetView.setLayoutParams(params);

        bottomSheetDialog.setCancelable(false);
        bottomSheetView.findViewById(R.id.cameraBtn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.galleryBtn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestStoragetPermission();
                } else {
                    pickFromGallery();
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
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = ChatActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragetPermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
    }
    @Override
    protected void onPause() {
        userRefForSeen.removeEventListener(seenListener);
        checkTyping("noOne");
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccept = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccept && storageAccept) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(ChatActivity.this, R.string.Please_enable_camera_permissions, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccpted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccpted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(ChatActivity.this, R.string.Please_enable_gallery_permissions, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (requestCode == REQUEST_CODE_SPEECH) {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String inSpeech = res.get(0);
                    messageEt.setText(inSpeech);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }
    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        Locale  myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config,  getResources().getDisplayMetrics());

    }

    @Override
    protected void onResume() {
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(connectivityReceiver);
        super.onStop();
    }

    public class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            ChatConnectionTV.setVisibility(View.GONE);
            if (networkInfo != null && networkInfo.isConnected()){
                ChatConnectionTV.setText(R.string.Internet_connected);
                ChatConnectionTV.setTextColor(Color.WHITE);
                ChatConnectionTV.setVisibility(View.GONE);

                // LAUNCH activity after certain time period
                new Timer().schedule(new TimerTask(){
                    public void run() {
                        ChatActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                ChatConnectionTV.setVisibility(View.GONE);
                            }
                        });
                    }
                }, 1000);
            } else {
                ChatConnectionTV.setText(context.getString(R.string.No_internet_connection));
                ChatConnectionTV.setTextColor(Color.WHITE);
                ChatConnectionTV.setBackgroundColor(Color.RED);
                ChatConnectionTV.setVisibility(View.VISIBLE);
            }
        }
    }

}
