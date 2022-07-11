package com.example.baitaplonandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static boolean connectionAvaialble(final Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null && connectivityManager.getActiveNetworkInfo()!=null){
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }else{
            return false;
        }
    }

    public  static void updateDeviceToken (Context context,String token){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
            DatabaseReference databaseReference = rootRef.child("Tokens").child(currentUser.getUid());

            HashMap<String ,String> hashMap = new HashMap<>();
            hashMap.put("device_token",token);

            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(context, R.string.failed_to_save_device_token, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static void sendNotification (Context context,String title,String message,String userId){
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        DatabaseReference databaseReference = rootRef.child("Tokens").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("device_token").getValue()!=null){
                    String deviceToken = snapshot.child("device_token").getValue().toString();

                    JSONObject notification = new JSONObject();
                    JSONObject notificationData = new JSONObject();

                    try{
                        notificationData.put("title",title);
                        notificationData.put("message",message);
                        notification.put("to",deviceToken);
                        notification.put("data",notificationData);

                        String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
                        String contentType = "application/json";

                        Response.Listener successListener = new Response.Listener(){
                            @Override
                            public void onResponse(Object response) {
                                Toast.makeText(context,"Notification sent",Toast.LENGTH_SHORT).show();
                            }
                        };

                        Response.ErrorListener failureListener = new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context,context.getString(R.string.failed_to_send_notification,error.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        };

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(fcmApiUrl,notification
                                ,successListener,failureListener){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map <String,String> params = new HashMap<>();
                                params.put("Authorization","key=AAAAYszcM9o:APA91bF5pAl1Ncd7OBzB9AT6iwefZNn3Pr2VbOLPWp6RRJbv57JHIVaNbIDPHPzE1yb_ESjh14SNmD_bOZc4h9yBXpA1Db9Wb-fqZMG7chiMs3tj_WlzUtL8-Fns-xJuVyDmYipSA3d1 ");
                                params.put("Sender","id=424343778266");
                                params.put("Content-Type",contentType);
                                return params;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(jsonObjectRequest);

                    }catch (JSONException e){
                        Toast.makeText(context,context.getString(R.string.failed_to_send_notification,e.getMessage()), Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context,context.getString(R.string.failed_to_send_notification,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateChatDetail (Context context,String currentUserId,String chatUserId,String lastMessage){
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference chatRef = rootRef.child("Chats").child(chatUserId).child(currentUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentCount = "0";
                if(snapshot.child("unread_count").getValue()!=null){
                    currentCount = snapshot.child("unread_count").getValue().toString();
                    Map chatMap  = new HashMap();

                    chatMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatMap.put("unread_count",Integer.valueOf(currentCount)+1);
                    chatMap.put("last_message",lastMessage);
                    chatMap.put("last_message_time",ServerValue.TIMESTAMP);


                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chats"+"/"+chatUserId+"/"+currentUserId,chatMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error!=null){
                                Toast.makeText(context, context.getString(R.string.something_went_wrong,error.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static  String getTimeAgo(long time)
    {
        final  int SECOND_MILLIS = 1000;
        final  int MINUTE_MILLIS= 60 * SECOND_MILLIS;
        final  int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final  int DAY_MILLIS = 24 * HOUR_MILLIS;

        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();

        if(time>now || time <=0)
        {
            return  "";
        }

        final  long diff = now-time;

        if(diff<MINUTE_MILLIS)
        {
            return  "just now";
        }
        else if(diff <2* MINUTE_MILLIS)
        {
            return  "a minute ago";
        }
        else if(diff <59*MINUTE_MILLIS)
        {
            return  diff/MINUTE_MILLIS + " minutes ago";
        }
        else  if(diff < 90 * MINUTE_MILLIS)
        {
            return "an hour ago";
        }
        else if(diff<24*HOUR_MILLIS){
            return  diff/HOUR_MILLIS + " hours ago";
        }
        else if( diff < 48 * HOUR_MILLIS)
        {
            return  "yesterday";
        }
        else
        {
            return  diff/DAY_MILLIS  + " days ago";
        }

    }
}
