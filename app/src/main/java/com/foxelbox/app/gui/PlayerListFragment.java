package com.foxelbox.app.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.foxelbox.app.R;
import com.foxelbox.app.data.MCPlayer;
import com.foxelbox.app.util.WebUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.UUID;

public class PlayerListFragment extends MainActivity.PlaceholderFragment {
    private class PlayerListItem extends CategoricListArrayAdapter.CategoricListItem {
        private final MCPlayer player;

        private PlayerListItem(MCPlayer player) {
            super(ChatFormatterUtility.formatString(player.getDisplayName()));
            this.player = player;
        }

        @Override
        public View getView(LayoutInflater inflater, View convertView) {
            final View view = super.getView(inflater, convertView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)getActivity()).openPlayerProfile(player);
                }
            });
            return view;
        }
    }

    private void refreshPlayerList() {
        new WebUtility(getActivity()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                final CategoricListArrayAdapter items = (CategoricListArrayAdapter)((ListView)getView().findViewById(R.id.playerListView)).getAdapter();
                items.clear();
                Iterator keyIterator = result.keys();
                String key; JSONArray players; int playerLen;
                items.clear();
                while(keyIterator.hasNext()) {
                    key = (String)keyIterator.next();
                    players = result.getJSONArray(key);
                    playerLen = players.length();
                    items.add(new CategoricListArrayAdapter.CategoricListHeader(key));
                    for(int i = 0; i < playerLen; i++) {
                        JSONObject playerInfo = players.getJSONObject(i);
                        MCPlayer player = new MCPlayer(UUID.fromString(playerInfo.getString("uuid")));
                        player.setDisplayName(playerInfo.getString("display_name"));
                        player.setName(playerInfo.getString("name"));
                        items.add(new PlayerListItem(player));
                    }
                }
            }
        }.execute("player/list", WebUtility.encodeData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_playerlist, container, false);

        final CategoricListArrayAdapter items = new CategoricListArrayAdapter(fragmentView.getContext());
        ((ListView)fragmentView.findViewById(R.id.playerListView)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPlayerList();
    }
}
