package de.doridian.foxelbox.app.gui;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.EditText;
import de.doridian.foxelbox.app.R;
import de.doridian.foxelbox.app.service.ChatPollService;
import de.doridian.foxelbox.app.util.LoginUtility;
import de.doridian.foxelbox.app.util.WebUtility;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Dialog loginDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        needLogin();
        startService(new Intent(this, ChatPollService.class));
    }

    private void needLogin() {
        if(LoginUtility.username != null && LoginUtility.password != null && LoginUtility.hasSessionId())
            return;
        LoginUtility.loadCredentials(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_dialog_login, null);
        loginDialog = builder.setView(dialogView).setTitle(R.string.login_title).setCancelable(false).create();
        loginDialog.show();
        if(LoginUtility.username != null && LoginUtility.password != null) {
            ((EditText) loginDialog.findViewById(R.id.login_username)).setText(LoginUtility.username);
            ((EditText) loginDialog.findViewById(R.id.login_password)).setText(LoginUtility.password);
            doLogin(dialogView);
        }
    }

    public void doLogin(final View view) {
        loginDialog.findViewById(R.id.login_button).setEnabled(false);
        loginDialog.findViewById(R.id.login_progressbar).setVisibility(View.VISIBLE);

        LoginUtility.username = ((EditText) loginDialog.findViewById(R.id.login_username)).getText().toString();
        LoginUtility.password = ((EditText) loginDialog.findViewById(R.id.login_password)).getText().toString();
        LoginUtility.saveCredentials(this);

        LoginUtility.enabled = true;
        new LoginUtility(null, this, getApplicationContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                super.onSuccess(result);
                loginDialog.dismiss();
                loginDialog = null;
            }

            @Override
            protected void onError(String message) throws JSONException {
                super.onError(message);
                loginDialog.findViewById(R.id.login_button).setEnabled(true);
                loginDialog.findViewById(R.id.login_progressbar).setVisibility(View.INVISIBLE);
            }
        }.login();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(loginDialog != null) {
            loginDialog.dismiss();
            loginDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_global, menu);
        MenuItem loaderSpinner = menu.findItem(R.id.menuLoaderSpinner);
        loaderSpinner.setActionView(R.layout.actionbar_immediate_progress);
        loaderSpinner.setVisible(WebUtility.isRunning());
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setSubtitle(mTitle);
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        Fragment fragment;

        final int pos = position + 1;

        switch (pos) {
            case 1:
                fragment = new ChatFragment(pos);
                break;
            case 2:
                fragment = new ProfileFragment(pos);
                break;
            case 3:
                fragment = new PlayerListFragment(pos);
                break;
            case 4:
                fragment = new PlayerListFragment(pos);
                break;
            case 5:
                LoginUtility.username = null;
                LoginUtility.password = null;
                LoginUtility.saveCredentials(this);
            case 6:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                final Dialog logoutDialog = builder.setView(inflater.inflate(R.layout.fragment_dialog_logout, null)).setCancelable(false).create();
                logoutDialog.show();

                new LoginUtility(null, this, getApplicationContext()) {
                    @Override
                    protected void onSuccess(JSONObject result) throws JSONException {
                        onDone();
                    }

                    @Override
                    protected void onError(String message) throws JSONException {
                        onDone();
                    }

                    private void onDone() {
                        logoutDialog.dismiss();
                        stopService(new Intent(MainActivity.this, ChatPollService.class));
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }.logout();
                return;
            default:
                return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
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
                mTitle = getString(R.string.title_players);
                break;
            case 4:
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

        public PlaceholderFragment() {

        }

        public PlaceholderFragment(int sectionNumber) {
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            setArguments(args);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
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
