package org.flyve.admin.dashboard.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.flyve.admin.dashboard.R;
import org.flyve.admin.dashboard.adapter.UserAdapter;
import org.flyve.admin.dashboard.adapter.UserTouchHelper;
import org.flyve.admin.dashboard.model.UserModel;
import org.flyve.admin.dashboard.utils.FlyveLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private ProgressBar pb;
    private RecyclerView lst;
    private List<UserModel> data;
    UserAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        pb = v.findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        lst = v.findViewById(R.id.lst);

        LinearLayoutManager llm = new LinearLayoutManager(UserFragment.this.getActivity());
        lst.setLayoutManager(llm);

        lst.setItemAnimator(new DefaultItemAnimator());
        lst.addItemDecoration(new DividerItemDecoration(UserFragment.this.getContext(), DividerItemDecoration.VERTICAL));

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new UserTouchHelper(0, ItemTouchHelper.LEFT, new UserTouchHelper.RecyclerItemTouchHelperListener() {

            /**
             * callback when recycler view is swiped
             * item will be removed on swiped
             * undo option will be provided in snackbar to restore the item
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof UserAdapter.DataViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = data.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final UserModel deletedItem = data.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    // remove the item from recycler view
                    mAdapter.removeItem(viewHolder.getAdapterPosition());

                    // showing snack bar with Undo option
                    Snackbar snackbar = Snackbar
                            .make(UserFragment.this.getView(), " removed from list", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // undo is selected, restore the deleted item
                            mAdapter.restoreItem(deletedItem, deletedIndex);
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(lst);


        load(loadJSONFromAsset());

        return v;
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getActivity().getAssets().open("json/users.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            FlyveLog.e(ex.getMessage());
            return null;
        }
        return json;
    }

    public void load(String jsonStr) {
        data = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonStr);

            JSONArray items = json.getJSONArray("data");
            for (int y = 0; y < items.length(); y++) {

                JSONObject obj = items.getJSONObject(y);

                UserModel model = new UserModel(UserModel.NO_HEADER);
                model.setEmail(obj.getString("User.name"));
                if(obj.getString("User.realname").trim().equalsIgnoreCase("")) {
                    model.setName("without name");
                } else {
                    model.setName(obj.getString("User.realname"));
                }

                if(!obj.getString("User.realname").equalsIgnoreCase("null")) {
                    data.add(model);
                }
            }

            pb.setVisibility(View.GONE);

            mAdapter = new UserAdapter(data, new UserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(UserModel item) {
                    openDetail(item);
                }
            });
            lst.setAdapter(mAdapter);

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            FlyveLog.e(ex.getMessage());
        }
    }

    private void openDetail(UserModel item) {
        Intent miIntent = new Intent(UserFragment.this.getActivity(), UserDetailActivity.class);
        UserFragment.this.startActivity(miIntent);
    }

}
