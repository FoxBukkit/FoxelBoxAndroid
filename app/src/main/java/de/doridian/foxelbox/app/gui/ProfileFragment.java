package de.doridian.foxelbox.app.gui;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.doridian.foxelbox.app.R;
import de.doridian.foxelbox.app.util.WebUtility;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ProfileFragment extends MainActivity.PlaceholderFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        final ArrayAdapter<Spannable> items = new ArrayAdapter<Spannable>(fragmentView.getContext(), android.R.layout.simple_list_item_1);
        ((ListView)fragmentView.findViewById(R.id.profileFieldList)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View fragmentView = getView();
        final ArrayAdapter<Spannable> items = (ArrayAdapter<Spannable>)((ListView)fragmentView.findViewById(R.id.profileFieldList)).getAdapter();
        items.add(new SpannableString("Please wait. Loading..."));

        new WebUtility(getActivity(), fragmentView.getContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                super.onSuccess(result);
                Iterator keyIterator = result.keys();
                String key, value;
                items.clear();
                while(keyIterator.hasNext()) {
                    key = (String)keyIterator.next();
                    value = result.getString(key);
                    items.add(ChatFormatterUtility.formatString(key + ": " + value));
                }
            }
        }.execute("player/info", WebUtility.encodeData("uuid", getArguments().getString("uuid", "myself")));
    }
}
