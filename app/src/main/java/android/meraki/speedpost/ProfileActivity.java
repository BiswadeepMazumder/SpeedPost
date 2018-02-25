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

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqButton;
    private DatabaseReference mUserDatabase;
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
        mCurrent_User = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendReqButton = findViewById(R.id.profile_send_req_button);
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

                mProgressbar.show();
                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);
                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.profile_pic).into(mProfileImage);
                mProgressbar.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqButton.setEnabled(false);

                //---------------- NOT FRIEND STATE -------------------
                if(mState.equals("not_friends"))
                {
                    mFriendRequestDatabase.child(mCurrent_User.getUid()).child(user_Id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendRequestDatabase.child(user_Id).child(mCurrent_User.getUid()).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            mProfileSendReqButton.setEnabled(true);
                                            mState = "request_sent";
                                            mProfileSendReqButton.setText("Cancel Friend Request");
                                            Toast.makeText(ProfileActivity.this,"Request Sent",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Problem in sending Friend Request. Try again later.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }




    //---------------- CANCEL FRIEND REQUEST STATE -------------------


}
