package com.foxelbox.app.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.EditText;
import com.foxelbox.app.R;
import com.foxelbox.app.data.MCPlayer;
import com.foxelbox.app.json.BaseResponse;
import com.foxelbox.app.service.ChatPollService;
import com.foxelbox.app.util.LoginUtility;
import com.foxelbox.app.util.WebUtility;

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

    private Dialog loginDialog = null;

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
            protected void onSuccess(BaseResponse result) {
                super.onSuccess(result);
                loginDialog.dismiss();
                loginDialog = null;
            }

            @Override
            protected void onError(String message) {
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
        MenuItemCompat.setActionView(loaderSpinner, R.layout.actionbar_immediate_progress);
        loaderSpinner.setVisible(WebUtility.isRunning());
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setSubtitle(mTitle);
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;

        final int pos = position + 1;

        switch (pos) {
            case 1:
                fragment = new ChatFragment();
                break;
            case 2:
                fragment = new ProfileFragment();
                break;
            case 3:
                fragment = new MapFragment();
                break;
            case 4:
                fragment = new PlayerListFragment();
                break;
            case 5:
                fragment = new PlaceholderFragment();
                break;
            case 6:
                LoginUtility.username = null;
                LoginUtility.password = null;
                LoginUtility.saveCredentials(this);
            case 7:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                final Dialog logoutDialog = builder.setView(inflater.inflate(R.layout.fragment_dialog_logout, null)).setCancelable(false).create();
                logoutDialog.show();

                new LoginUtility(null, this, getApplicationContext()) {
                    @Override
                    protected void onSuccess(BaseResponse result) {
                        onDone();
                    }

                    @Override
                    protected void onError(String message) {
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

        Bundle args = new Bundle();
        args.putInt(PlaceholderFragment.ARG_SECTION_NUMBER, pos);
        fragment.setArguments(args);

        replaceContentFragment(fragment, false);
    }

    public void openPlayerProfile(MCPlayer player) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("uuid", player.getUuid());
        profileFragment.setArguments(args);
        replaceContentFragment(profileFragment, true);
    }

    public void replaceContentFragment(Fragment fragment, boolean addToStack) {
        if(addToStack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
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
                mTitle = getString(R.string.title_main_server_map);
                break;
            case 4:
                mTitle = getString(R.string.title_players);
                break;
            case 5:
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

        protected ActionBarActivity getActionBarActivity() {
            return (ActionBarActivity)getActivity();
        }
    }
}
