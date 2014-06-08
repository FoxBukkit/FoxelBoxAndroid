package de.doridian.foxelbox.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        needLogin();
    }

    private Dialog loginDialog = null;

    private void needLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        loginDialog = builder.setView(inflater.inflate(R.layout.fragment_dialog_login, null)).setTitle(R.string.login_title).setCancelable(false).create();
        loginDialog.show();
    }

    public void doLogin(final View view) {
        loginDialog.findViewById(R.id.login_button).setEnabled(false);
        loginDialog.findViewById(R.id.login_progressbar).setVisibility(View.VISIBLE);

        LoginUtility.username = ((EditText) loginDialog.findViewById(R.id.login_username)).getText();
        LoginUtility.password = ((EditText) loginDialog.findViewById(R.id.login_password)).getText();

        new LoginUtility(null, getApplicationContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                super.onSuccess(result);
                loginDialog.hide();
                loginDialog = null;
            }

            @Override
            protected void onError(String message) throws JSONException {
                super.onError(message);
                loginDialog.findViewById(R.id.login_button).setEnabled(true);
                loginDialog.findViewById(R.id.login_progressbar).setVisibility(View.INVISIBLE);
            }
        }.execute();
    }

    public void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)view.findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        new WebUtility(getApplicationContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {

            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_chat);
                break;
            case 2:
                mTitle = getString(R.string.title_me);
                break;
            case 3:
                mTitle = getString(R.string.title_settings);
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int fragment = R.layout.fragment_main;
            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    fragment = R.layout.fragment_chat;
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            View rootView = inflater.inflate(fragment, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
