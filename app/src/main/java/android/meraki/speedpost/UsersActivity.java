package android.meraki.speedpost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mUsersList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolBar = findViewById(R.id.allUser_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList = findViewById(R.id.Users_view);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this  ));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }


    @Override
    protected void onStart() {
        super.onStart();

      FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
              Users.class,
              R.layout.users_single_view,
              UserViewHolder.class,
              mDatabase

      ) {
          @Override
          protected void populateViewHolder(UserViewHolder viewHolder, Users model, int position) {
              UserViewHolder.setUserImage(model.getThumb_image(),getApplicationContext());
              UserViewHolder.setName(model.getName());
              UserViewHolder.setStatus(model.getStatus());
              UserViewHolder.setImage(model.getImage());

              final String user_id = getRef(position).getKey();


              UserViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                      profileIntent.putExtra("user_id",user_id);
                      startActivity(profileIntent);

                  }
              });


          }
      };

            mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

}
