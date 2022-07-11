package com.example.baitaplonandroid.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baitaplonandroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView rvChatlist;
    private View progressBar;
    private TextView txtEmptyChatList;
    private ChatListAdapter chatListAdapter;
    private List<ChatModel> chatModelList;

    private DatabaseReference databaseReferenceChats,databaseReferenceUsers;
    private FirebaseUser currentUser;

    private ChildEventListener childEventListener;
    private Query query;

    private List<String> userIds;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChatlist = view.findViewById(R.id.rvChats);
        txtEmptyChatList = view.findViewById(R.id.txtEmptyChatList);

        userIds = new ArrayList<>();
        chatModelList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getActivity(),chatModelList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvChatlist.setLayoutManager(linearLayoutManager);

        rvChatlist.setAdapter(chatListAdapter);
        progressBar = view.findViewById(R.id.progressBar);
        databaseReferenceUsers = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChats = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Chats").child(currentUser.getUid());

        query = databaseReferenceChats.orderByChild("timestamp");

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,true,snapshot.getKey());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,false,snapshot.getKey());


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        query.addChildEventListener(childEventListener);
        progressBar.setVisibility(View.VISIBLE);
        txtEmptyChatList.setVisibility(View.VISIBLE);

    }
    private void updateList(DataSnapshot dataSnapshot,boolean isNew,String userId){
        progressBar.setVisibility(View.GONE);
        txtEmptyChatList.setVisibility(View.GONE);

         final String fullName,photoName,lastMessage,lastMessageTime,unreadCount;

         if(dataSnapshot.child("last_message").getValue()!=null)
             lastMessage = dataSnapshot.child("last_message").getValue().toString();
         else
             lastMessage = "";


        if(dataSnapshot.child("last_message_time").getValue()!=null)
            lastMessageTime = dataSnapshot.child("last_message_time").getValue().toString();
        else
            lastMessageTime = "";


         unreadCount = dataSnapshot.child("unread_count").getValue()==null?
                 "0":dataSnapshot.child("unread_count").getValue().toString();


         databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 String fullName = snapshot.child("name").getValue().toString()!=null?
                         snapshot.child("name").getValue().toString():"";

                 String photoName = snapshot.child("photo").getValue().toString()!=null?
                         snapshot.child("photo").getValue().toString():"";

                 ChatModel chatModel = new ChatModel(userId,fullName,photoName,unreadCount,lastMessage,lastMessageTime);
                 if(isNew) {
                     chatModelList.add(chatModel);
                     userIds.add(userId);
                 }else {
                       int indexOfClickedUser = userIds.indexOf(userId);
                       chatModelList.set(indexOfClickedUser,chatModel);
                 }

                 chatListAdapter.notifyDataSetChanged();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_chat_list,error.getMessage()), Toast.LENGTH_SHORT).show();
             }
         });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}