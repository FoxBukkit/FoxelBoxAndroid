package de.doridian.foxelbox.app.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.doridian.foxelbox.app.R;
import de.doridian.foxelbox.app.util.WebUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class PlayerListFragment extends MainActivity.PlaceholderFragment {
    public PlayerListFragment() { }

    public PlayerListFragment(int sectionNumber) {
        super(sectionNumber);
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
                        items.add(new CategoricListArrayAdapter.CategoricListItem(ChatFormatterUtility.formatString(playerInfo.getString("display_name"))));
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
