package com.foxelbox.app.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.foxelbox.app.R;
import com.foxelbox.app.data.MCPlayer;
import com.foxelbox.app.json.BaseResponse;
import com.foxelbox.app.util.WebUtility;
import org.json.JSONArray;
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

    public class PlayerListPlayer {
        public UUID uuid;
        public String name;
        public String display_name;
    }

    public class PlayerListServer {
        public String server;
        public PlayerListPlayer[] players;
    }

    public class PlayerListResponse extends BaseResponse {
        public PlayerListServer[] list;
    }

    private void refreshPlayerList() {
        new WebUtility<PlayerListResponse>(getActivity()) {
            @Override
            public PlayerListResponse createResponse() {
                return new PlayerListResponse();
            }

            @Override
            public Class<PlayerListResponse> getResponseClass() {
                return PlayerListResponse.class;
            }

            @Override
            protected void onSuccess(PlayerListResponse result) {
                final CategoricListArrayAdapter items = (CategoricListArrayAdapter)((ListView)getView().findViewById(R.id.playerListView)).getAdapter();
                items.clear();
                for(PlayerListServer server : result.list) {
                    items.add(new CategoricListArrayAdapter.CategoricListHeader(server.server));
                    for(PlayerListPlayer jsonPlayer : server.players) {
                        MCPlayer player = new MCPlayer(jsonPlayer.uuid);
                        player.setDisplayName(jsonPlayer.display_name);
                        player.setName(jsonPlayer.name);
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
