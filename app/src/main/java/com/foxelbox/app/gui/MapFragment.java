package com.foxelbox.app.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.foxelbox.app.R;

public class MapFragment extends MainActivity.PlaceholderFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_map, container, false);

        WebView webView = (WebView)fragmentView.findViewById(R.id.mapWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://mc.foxelbox.com:8123/");

        return fragmentView;
    }
}
