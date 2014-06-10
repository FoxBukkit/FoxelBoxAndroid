package de.doridian.foxelbox.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ProfileFragment extends MainActivity.PlaceholderFragment {
    public ProfileFragment() { }

    public ProfileFragment(int sectionNumber) {
        super(sectionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        final ArrayAdapter<String> items = new ArrayAdapter<String>(fragmentView.getContext(), R.layout.text_view_minecraft);
        ((ListView)fragmentView.findViewById(R.id.profileFieldList)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View fragmentView = getView();
        final ArrayAdapter<String> items = (ArrayAdapter<String>)((ListView)fragmentView.findViewById(R.id.profileFieldList)).getAdapter();
        items.add("Please wait. Loading...");

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
                    items.add(key + ": " + value);
                }
            }
        }.execute("profile", WebUtility.encodeData("uuid", "myself"));
    }
}
