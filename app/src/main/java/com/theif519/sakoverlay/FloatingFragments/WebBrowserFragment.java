package com.theif519.sakoverlay.FloatingFragments;

import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.theif519.sakoverlay.R;

import java.util.ArrayList;

/**
 * Created by theif519 on 11/6/2015.
 */
public class WebBrowserFragment extends FloatingFragment {

    private static final String DEFAULT_HOMEPAGE = "http://www.google.com", HOME = "Home", REFRESH = "Refresh";

    public static final String IDENTIFIER = "Web Browser";

    private WebView mBrowser;

    private Button mBrowserBack, mBrowserForward;

    public static WebBrowserFragment newInstance(){
        WebBrowserFragment fragment = new WebBrowserFragment();
        fragment.LAYOUT_ID = R.layout.web_browser;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.browser;
        fragment.mOptions = new ArrayList<>();
        fragment.mOptions.add(HOME);
        fragment.mOptions.add(REFRESH);
        return fragment;
    }

    @Override
    public void setup() {
        super.setup();
        mBrowser = (WebView) getContentView().findViewById(R.id.browser_view);
        mBrowserBack = (Button) getContentView().findViewById(R.id.browser_action_back);
        mBrowserBack.setVisibility(View.INVISIBLE);
        mBrowserForward = (Button) getContentView().findViewById(R.id.browser_action_forward);
        mBrowserForward.setVisibility(View.INVISIBLE);
        mBrowser.getSettings().setJavaScriptEnabled(true);
        mBrowser.getSettings().setBuiltInZoomControls(true);
        mBrowser.getSettings().setLoadWithOverviewMode(true);
        mBrowser.getSettings().setUseWideViewPort(true);
        mBrowser.getSettings().setSupportZoom(true);
        mBrowser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mBrowser.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if(mBrowser.canGoBack()){
                    mBrowserBack.setVisibility(View.VISIBLE);
                } else mBrowserBack.setVisibility(View.INVISIBLE);
                if(mBrowser.canGoForward()){
                    mBrowserForward.setVisibility(View.VISIBLE);
                } else mBrowserForward.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ((EditText)getContentView().findViewById(R.id.browser_action_text)).setText(url);
            }
        });
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
        /*
            Here we allow hitting enter/return on the edit text to submit and send HTTP requests. We also
            check to see if the URL begins with the necessary prefix, "http" or "https", appending our own if
            need be.
         */
        getContentView().findViewById(R.id.browser_action_text).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String url = ((EditText)v).getText().toString();
                    StringBuilder completeUrl = new StringBuilder();
                    if(!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")){
                        completeUrl.append("http://");
                    }
                    completeUrl.append(url);
                    mBrowser.loadUrl(completeUrl.toString());
                }
                return true;
            }
        });
        mBrowser.loadUrl(DEFAULT_HOMEPAGE);
        ((EditText) getContentView().findViewById(R.id.browser_action_text)).setText(DEFAULT_HOMEPAGE);
    }

    @Override
    public void onItemSelected(String string) {
        super.onItemSelected(string);
        switch(string){
            case HOME:
                mBrowser.loadUrl(DEFAULT_HOMEPAGE);
                break;
            case REFRESH:
                mBrowser.reload();
                break;
        }
    }
}
