package android.meraki.speedpost;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolBar = findViewById(R.id.allUser_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList = findViewById(R.id.Users_view);


    }
}
