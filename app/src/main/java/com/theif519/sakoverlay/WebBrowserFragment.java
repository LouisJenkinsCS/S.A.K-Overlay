package com.theif519.sakoverlay;

import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by theif519 on 11/6/2015.
 */
public class WebBrowserFragment extends FloatingFragment {

    private static final String DEFAULT_HOMEPAGE = "http://www.google.com";
    public static final String IDENTIFIER = "Web Browser";

    private WebView mBrowser;

    public static WebBrowserFragment newInstance(){
        WebBrowserFragment fragment = new WebBrowserFragment();
        fragment.LAYOUT_ID = R.layout.web_browser;
        fragment.LAYOUT_TAG = IDENTIFIER;
        return fragment;
    }

    @Override
    public void setup() {
        super.setup();
        mBrowser = (WebView) getContentView().findViewById(R.id.browser_view);
        // Uh-Oh!
        mBrowser.getSettings().setJavaScriptEnabled(true);
        mBrowser.getSettings().setBuiltInZoomControls(true);
        mBrowser.getSettings().setLoadWithOverviewMode(true);
        mBrowser.getSettings().setUseWideViewPort(true);
        mBrowser.getSettings().setSupportZoom(true);
        mBrowser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mBrowser.setWebViewClient(new WebViewClient());
        mBrowser.setWebChromeClient(new WebChromeClient());
        mBrowser.setInitialScale(1);
        getContentView().findViewById(R.id.browser_action_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowser.goBack();
            }
        });
        getContentView().findViewById(R.id.browser_action_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowser.goForward();
            }
        });
        getContentView().findViewById(R.id.browser_action_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowser.reload();
            }
        });
        getContentView().findViewById(R.id.browser_action_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrowser.loadUrl(DEFAULT_HOMEPAGE);
            }
        });
        ((EditText) getContentView().findViewById(R.id.browser_action_text)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mBrowser.loadUrl(v.getText().toString());
                }
                return true;
            }
        });
        mBrowser.loadUrl(DEFAULT_HOMEPAGE);
        ((EditText) getContentView().findViewById(R.id.browser_action_text)).setText(DEFAULT_HOMEPAGE);
    }

    @Override
    public ArrayMap<String, String> serialize() {
        return super.serialize();
    }

    @Override
    public void unpack() {
        super.unpack();
    }
}
