package com.foxelbox.app.gui;

import android.os.Bundle;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.foxelbox.app.R;
import com.foxelbox.app.json.BaseResponse;
import com.foxelbox.app.json.player.profile.ProfileField;
import com.foxelbox.app.util.WebUtility;
import com.foxelbox.app.util.chat.ChatFormatterUtility;
import com.google.gson.reflect.TypeToken;

public class ProfileFragment extends MainActivity.PlaceholderFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        final ArrayAdapter<Spanned> items = new ArrayAdapter<>(fragmentView.getContext(), android.R.layout.simple_list_item_1);
        ((ListView)fragmentView.findViewById(R.id.profileFieldList)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View fragmentView = getView();
        if(fragmentView == null) {
            return;
        }
        final ArrayAdapter<Spanned> items = (ArrayAdapter<Spanned>)((ListView)fragmentView.findViewById(R.id.profileFieldList)).getAdapter();
        items.add(new SpannedString("Please wait. Loading..."));

        String myUUID = "myself";
        Bundle arguments = getArguments();
        if(arguments.containsKey("uuid")) {
            myUUID = getArguments().getSerializable("uuid").toString();
        }

        new WebUtility<ProfileField[]>(getAppCompatActivity(), fragmentView.getContext()) {
            @Override
            protected void onSuccess(ProfileField[] result) {
                super.onSuccess(result);
                items.clear();
                for(ProfileField field : result)
                    items.add(ChatFormatterUtility.formatString(field.title + ": " + field.value, false));
            }

            @Override
            protected TypeToken<BaseResponse<ProfileField[]>> getTypeToken() {
                return new TypeToken<BaseResponse<ProfileField[]>>(){};
            }
        }.execute("GET", "player/" + myUUID);
    }
}
