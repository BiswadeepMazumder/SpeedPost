package android.meraki.speedpost;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqButton, mProfileDeclineReqButton;
    private DatabaseReference mUserDatabase, mFriendsDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private ProgressDialog mProgressbar;
    private String mState;
    private FirebaseUser mCurrent_User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_Id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_Id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_User = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendReqButton = findViewById(R.id.profile_send_req_button);
        mProfileDeclineReqButton = findViewById(R.id.profile_decline_req_button);
        mProfileName = findViewById(R.id.profile_display_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalfriends);

        mState = "not_friends";

        mProgressbar = new ProgressDialog(this);
        mProgressbar.setTitle("Loading User Image");
        mProgressbar.setMessage("Wait till the picture Loads.");
        mProgressbar.setCanceledOnTouchOutside(false);


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();


                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);

                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.profile_pic).into(mProfileImage);


                //---------- FRIEND LIST / REQUEST FEATURE ----------

                mFriendRequestDatabase.child(mCurrent_User.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_Id)) {

                            String request_type = dataSnapshot.child(user_Id).child("request_type").getValue().toString();

                            if (request_type.equals("received")) {

                                mState = "request_received";
                                mProfileSendReqButton.setText("ACCEPT REQUEST");
                            } else if (request_type.equals("sent")) {
                                mState = "request_sent";
                                mProfileSendReqButton.setText("SEND FRIEND REQUEST");
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqButton.setEnabled(false);

                mProgressbar.dismiss();
                //********** HIDING THE BUTTONS SO THAT THE USER CANT SEND FRIEND REQUEST TO HIMSELF/HERSELF*********
//                if(user_Id.equals(mCurrent_User.getUid()))
//                {
//                    mProfileSendReqButton.setVisibility(View.INVISIBLE);
//                    mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
//                }


                //---------------- NOT FRIEND STATE -------------------
                if (mState.equals("not_friends")) {

                    mFriendRequestDatabase.child(mCurrent_User.getUid()).child(user_Id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendRequestDatabase.child(user_Id).child(mCurrent_User.getUid()).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mProfileSendReqButton.setEnabled(true);
                                            mState = "request_sent";
                                            mProfileSendReqButton.setText("Cancel Friend Request");
                                            //   Toast.makeText(ProfileActivity.this,"Request Sent",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Problem in sending Friend Request. Try again later.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //---------------- CANCEL FRIEND REQUEST STATE -------------------

                if (mState.equals("request_sent")) {
                    mFriendRequestDatabase.child(mCurrent_User.getUid()).child(user_Id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(user_Id).child(mCurrent_User.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            mProfileSendReqButton.setEnabled(true);
                                                            mState = "not_friends";
                                                            mProfileSendReqButton.setText("SEND FRIEND REQUEST");
                                                        } else {

                                                        }

                                                    }
                                                });
                                    } else {

                                    }
                                }
                            });
                }


                // ---------- REQUEST RECEIVED STATE ----------

                if (mState.equals("request_received")) {
                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendsDatabase.child(mCurrent_User.getUid()).child(user_Id)
                            .setValue(current_date).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                mFriendsDatabase.child(user_Id).child(mCurrent_User.getUid()).setValue(current_date).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            mFriendRequestDatabase.child(mCurrent_User.getUid()).child(user_Id).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                mFriendRequestDatabase.child(user_Id).child(mCurrent_User.getUid()).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {
                                                                                    mProfileSendReqButton.setEnabled(true);
                                                                                    mState = "friends";
                                                                                    mProfileSendReqButton.setText("UNFRIEND THIS PERSON");
                                                                                } else {

                                                                                }

                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }

                                    }
                                });
                            }

                        }
                    });
                }


            }
        });
    }
}