package com.foxelbox.app.gui;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.foxelbox.app.R;
import com.foxelbox.app.json.player.profile.ProfileField;
import com.foxelbox.app.json.player.profile.ProfileResponse;
import com.foxelbox.app.util.WebUtility;

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

        String myUUID = "myself";
        Bundle arguments = getArguments();
        if(arguments.containsKey("uuid"))
            myUUID = getArguments().getSerializable("uuid").toString();

        new WebUtility<ProfileResponse>(getActionBarActivity(), fragmentView.getContext()) {
            @Override
            public ProfileResponse createResponse() {
                return new ProfileResponse();
            }

            @Override
            public Class<ProfileResponse> getResponseClass() {
                return ProfileResponse.class;
            }

            @Override
            protected void onSuccess(ProfileResponse result) {
                super.onSuccess(result);
                items.clear();
                for(ProfileField field : result.fields)
                    items.add(ChatFormatterUtility.formatString(field.title + ": " + field.value, false));
            }
        }.execute("player/info", WebUtility.encodeData("uuid", myUUID));
    }
}
