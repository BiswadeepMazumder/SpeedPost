package android.meraki.speedpost;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mysel on 16-02-2018.
 */

public class UserViewHolder extends ViewHolder {
    private static View mView;
   // View mView;
    public UserViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public static void setName(String name) {
        TextView userNameview = (TextView) mView.findViewById(R.id.user_single_name);
        userNameview.setText(name);
    }

    public static void setStatus(String status) {
        TextView userStatus = (TextView) mView.findViewById(R.id.user_single_status);
        userStatus.setText(status);
    }

    public static void setImage(String image) {

    }
}
