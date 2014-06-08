package de.doridian.foxelbox.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);

        Button button = (Button)fragmentView.findViewById(R.id.buttonSendChat);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatMessage(fragmentView);
            }
        });

        return fragmentView;
    }

    public void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)getView().findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        new WebUtility(view.getContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                Toast.makeText(context, "DBG SUCCESS: " + result.toString(), Toast.LENGTH_SHORT).show();
            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }
}
