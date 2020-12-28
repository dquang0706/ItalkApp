package com.example.italkapp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.italkapp.ChatActivity;
import com.example.italkapp.PostDetailActivity;
import com.example.italkapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;
//    tạo một service có tên là MyFirebaseService và extends từ FirebaseMessagingService

public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID="admin_channel";

//    onMessageReceived(): Phương thức này sẽ chạy khi có thông báo từ firebase gửi về
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
        String savedCurrentUser=sp.getString("Current_USERID","None");

        String notificationType=remoteMessage.getData().get("notificationType");
        if(notificationType.equals("PostNotification")){
            //post notification
            String sender=remoteMessage.getData().get("sender");
            String pId=remoteMessage.getData().get("pId");
            String pTitle=remoteMessage.getData().get("pTitle");
            String pDescription=remoteMessage.getData().get("pDescription");

            // if user is same that has posted don't show notification
            if(!sender.equals(savedCurrentUser)){
                showPostNotification(""+pId,""+pTitle,""+ pDescription);
            }

        }else if(notificationType.equals("ChatNotification")){
            // chat notification
            String sent=remoteMessage.getData().get("sent");
            String user=remoteMessage.getData().get("user");
            FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
            if(fUser!=null && sent.equals(fUser.getUid())){
                if(!savedCurrentUser.equals(user)){
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                        sendAndAboveNotification(remoteMessage);
                    }else {
                        sendNormalNotication(remoteMessage);
                    }
                }
            }
        }
    }

    private void showPostNotification(String pId,String pTitle,String pDescription) {
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID=new Random().nextInt(3000);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            setupPostNotification(notificationManager);
        }

        Intent intent=new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        /// Largeicon
        Bitmap largeIcon= BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
        // âm thanh mặc định cho thông báo
        Uri notificationUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationUri)
                .setContentIntent(pendingIntent);
        //show notification
        notificationManager.notify(notificationID,notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotification(NotificationManager notificationManager) {
        CharSequence channelName="New Notification";
        String channelDescription=" Device to device post notification";
        NotificationChannel addiminChanel=new NotificationChannel(ADMIN_CHANNEL_ID,channelName,NotificationManager.IMPORTANCE_HIGH);
        addiminChanel.setDescription(channelDescription);
        addiminChanel.enableLights(true);
        addiminChanel.setLightColor(Color.RED);
        addiminChanel.enableVibration(true);
        if(notificationManager!=null){
            notificationManager.createNotificationChannel(addiminChanel);
        }
    }

    private void sendNormalNotication(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");

//        int i=Integer.parseInt(user.replaceAll("[\\D]",""));

        Intent intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

//        sử dụng lớp PendingIntent để bắt sự kiện khi kích vào thông báo
        PendingIntent pIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri deSoundfUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(deSoundfUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        int j=0;
//        if(i>0){
//            j=i;
//        }
        notificationManager.notify(0,builder.build());
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendAndAboveNotification(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");
//        int i=Integer.parseInt(user.replaceAll("[\\D]",""));

        Intent intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri deSoundfUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1=new OreoAndAboveNotification(this);
        Notification.Builder builder=notification1.getONotifications(title,body,pIntent,deSoundfUri,icon);

//        int j=0;
//        if(i>0){
//            j=i;
//        }
        notification1.getManager().notify(0,builder.build());

    }
//    Khi một thiết bị cài đặt ứng dụng thì nó sẽ đăng ký một device_token lên cho Firebase
//    để Firebase có thể dựa vào các token này để gửi các thông báo về thiết bị
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            newToKen(s);
        }
    }
//1
    private void newToKen(String newtoken) {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token=new Token(newtoken);
        ref.child(user.getUid()).setValue(token);
    }
}
