package com.example.whatsapp6;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwindo extends AppCompatActivity {
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn;
    EditText textmsg;

    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    // AES key (this will be retrieved from SharedPreferences)
    SecretKey secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);

        try {
            secretKey = AESUtils.generateKey(this); // Retrieve AES key from SharedPreferences
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve information passed from the previous activity
        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        // Initialize UI elements
        messagesArrayList = new ArrayList<>();
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);

        // Set up RecyclerView to show messages
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        // Set receiver's profile picture and name
        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText("" + reciverName);

        // Get the current user's UID
        SenderUID = firebaseAuth.getUid();

        // Create unique rooms for sender and receiver
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        // Reference to Firebase user data
        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        // Listen for changes in the chat messages
        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    // Decrypt the message before displaying it
                    try {
                        String decryptedMessage = AESUtils.decrypt(messages.getMessage(), secretKey);
                        messages.setMessage(decryptedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messagesArrayList.add(messages);  // message store
                }
                mmessagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any error in case of failure
            }
        });

        // Set up the send button click listener
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                try {
                    // Encrypt the message before sending it
                    String encryptedMessage = AESUtils.encrypt(message, secretKey);

                    // Create a new message object
                    msgModelclass messagess = new msgModelclass(encryptedMessage, SenderUID, new Date().getTime());

                    // Store the encrypted message in Firebase for both sender and receiver
                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    database.getReference().child("chats")
                                            .child(reciverRoom)
                                            .child("messages")
                                            .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    // Handle completion
                                                }
                                            });
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
