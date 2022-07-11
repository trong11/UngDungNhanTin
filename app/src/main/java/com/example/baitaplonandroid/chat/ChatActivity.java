package com.example.baitaplonandroid.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.Util;
import com.example.baitaplonandroid.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView ivSend,ivProfile;
    private TextView txtFullname;
    private EditText etMessage;
    private DatabaseReference mRootRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId,chatUserId;

    private RecyclerView rvMessage;
    private SwipeRefreshLayout srlMessages;
    private MessagesAdapter messagesAdapter;
    private List<Message> messagesList;
    private String userName,photoName;

    private int currentPage = 1;
    private static final int RECORD_PER_PAGE = 30;

    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;

    private ChipGroup cgSmartReplies;
    private List <TextMessage> conversation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("");
            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar,null);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()|ActionBar.DISPLAY_SHOW_CUSTOM);

        }

        cgSmartReplies = findViewById(R.id.cgSmartReplies);
        conversation = new ArrayList<>();

        ivSend = findViewById(R.id.ivSend);
        etMessage = findViewById(R.id.etMessage);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        txtFullname = (TextView) findViewById(R.id.txtChatName);

        ivSend.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if(getIntent().hasExtra("user_key")){
            chatUserId = getIntent().getStringExtra("user_key");
            photoName = chatUserId + ".jpg";
        }

        if(getIntent().hasExtra("user_name")){
            userName = getIntent().getStringExtra("user_name");
        }

        txtFullname.setText(userName);

        StorageReference photoRef = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference().child("images/"+photoName);

        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.defaultpropic)
                        .error(R.drawable.defaultpropic)
                        .into(ivProfile);
            }
        });



        rvMessage = findViewById(R.id.rvMessages);
        srlMessages = findViewById(R.id.srlMessages);

        messagesList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this,messagesList);

        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        rvMessage.setAdapter(messagesAdapter);

        loadMessages();

        mRootRef.child("Chats").child(currentUserId).child(chatUserId).child("unread_count").setValue("0");


        rvMessage.scrollToPosition(messagesList.size()-1);



        srlMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMessages();
            }
        });



    }

    private void loadMessages(){
        messagesList.clear();
        conversation.clear();
        cgSmartReplies.removeAllViews();
        databaseReferenceMessages = mRootRef.child("Messages").child(currentUserId).child(chatUserId);

        Query messageQuery = databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE);

        if(childEventListener !=null)
            messageQuery.removeEventListener(childEventListener);

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    messagesList.add(message);
                    messagesAdapter.notifyDataSetChanged();
                    rvMessage.scrollToPosition(messagesList.size()-1);
                    srlMessages.setRefreshing(false);
                    showSmartReplies(message);

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    srlMessages.setRefreshing(false);
                }
            };
            messageQuery.addChildEventListener(childEventListener);
        }


    private void sendMessage (String msg,String msgType,String pushId){
        try{
            if(!msg.equals("")){
                HashMap messageMap = new HashMap();
                messageMap.put("messageId",pushId);
                messageMap.put("message",msg);
                messageMap.put("messageType",msgType);
                messageMap.put("messageFrom",currentUserId);
                messageMap.put("messageTime", ServerValue.TIMESTAMP);

                String currentUserRef = "Messages/"+currentUserId+"/"+chatUserId;
                String chatUserRef = "Messages/"+chatUserId+"/"+currentUserId;

                HashMap messageUserMap = new HashMap();

                messageUserMap.put(currentUserRef + "/"+pushId, messageMap);
                messageUserMap.put(chatUserRef+"/"+pushId,messageMap);

                etMessage.setText("");
                conversation.add(TextMessage.createForLocalUser(msg,System.currentTimeMillis()));

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error!=null){
                            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message,error.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                        {
                            Toast.makeText(ChatActivity.this, "Message sent success", Toast.LENGTH_SHORT).show();

                            String title = "New Message";
                            Util.sendNotification(ChatActivity.this,title,msg,chatUserId);

                            String lastMessage = !title.equals("New Message")?title:msg;

                            Util.updateChatDetail(ChatActivity.this,currentUserId,chatUserId,lastMessage);

            }
        }
    });

            }

        }catch (Exception e){
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message,e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivSend:
                if(Util.connectionAvaialble(this)) {
                    DatabaseReference messagePush = mRootRef.child("Messages").child(currentUserId).child(chatUserId).push();
                    String pushId = messagePush.getKey();
                    sendMessage(etMessage.getText().toString().trim(), "text", pushId);
                }else{
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mRootRef.child("Chats").child(currentUserId).child(chatUserId).child("unread_count").setValue("0");
        super.onBackPressed();
    }

    private void showSmartReplies(Message message1) {
        conversation.clear();
        cgSmartReplies.removeAllViews();

        DatabaseReference databaseReference = mRootRef.child("Messages").child(currentUserId).child(chatUserId);
        Query lastQuery = databaseReference.orderByKey().limitToLast(1);

        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Message message = data.getValue(Message.class);

                    if (message.getMessageFrom().equals(chatUserId) && message1.getMessageId().equals(message.getMessageId()) && message.getMessageType().equals("text")) {
                        conversation.add(TextMessage.createForRemoteUser(message.getMessage(), System.currentTimeMillis(), chatUserId));

                        if (!conversation.isEmpty()) {
                            SmartReplyGenerator smartReply = SmartReply.getClient();
                            smartReply.suggestReplies(conversation).addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                                @Override
                                public void onSuccess(SmartReplySuggestionResult result) {
                                    if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                        Toast.makeText(ChatActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                                    } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                        for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                            String replyText = suggestion.getText();

                                            Chip chip = new Chip(ChatActivity.this);
                                            ChipDrawable drawable = ChipDrawable.createFromAttributes(ChatActivity.this, null, 0, R.style.Widget_MaterialComponents_Chip_Action);
                                            chip.setChipDrawable(drawable);

                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                                            params.setMargins(16, 16, 16, 16);
                                            chip.setLayoutParams(params);

                                            chip.setText(replyText);
                                            chip.setTag(replyText);

                                            chip.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    DatabaseReference messageRef = mRootRef.child("Messages").child(currentUserId).child(chatUserId).push();

                                                    String newMessageId = messageRef.getKey();

                                                    sendMessage(view.getTag().toString(), "text", newMessageId);
                                                }
                                            });
                                            cgSmartReplies.addView(chip);
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChatActivity.this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}