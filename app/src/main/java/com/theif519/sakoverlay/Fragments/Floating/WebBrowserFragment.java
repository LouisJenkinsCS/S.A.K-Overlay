package com.theif519.sakoverlay.Fragments.Floating;

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

/**
 * Created by theif519 on 11/6/2015.
 * <p/>
 * WebBrowser is a simple WebView with some modifications to make it run faster, because god knows it
 * needs every bit of optimization/performance boost it can get. It is ungodly slow, and apparently this
 * is standard somehow.
 * <p/>
 * I sped things up by enabling the Hardware Acceleration layer as well as disabling caching, and finally
 * by changing the Rendering thread's priority, which apparently is deprecated, but oh well. The goals
 * for the end-game Browser isn't too big, however they aren't small either.
 * <p/>
 * 1) Be able to support tabbing, easily accomplished using a tab view.
 * <p/>
 * 2) Save user's history, last used page, and last location on said page.
 * <p/>
 * 3) Proxy everything through an AdBlock or identity hiding/spoofing service, although the web-based
 * ones are rather rare these days.
 * <p/>
 * 4) Be able to determine if a link should be opened in another app. I.E, google playstore link should open
 * in Google Playstore, etc.
 */
public class WebBrowserFragment extends FloatingFragment {

    protected static final String IDENTIFIER = "Web Browser";
    private static final String DEFAULT_HOMEPAGE = "http://www.google.com", HOME = "Home", REFRESH = "Refresh";
    private WebView mBrowser;

    private Button mBrowserBack, mBrowserForward;

    public static WebBrowserFragment newInstance() {
        WebBrowserFragment fragment = new WebBrowserFragment();
        fragment.LAYOUT_ID = R.layout.web_browser;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.browser;
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
        mBrowser.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // Screw the deprecation, this thing needs as much help as it can get.
        mBrowser.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mBrowser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mBrowser.getSettings().setBuiltInZoomControls(true);
        mBrowser.getSettings().setLoadWithOverviewMode(true);
        mBrowser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        mBrowser.getSettings().setUseWideViewPort(true);
        mBrowser.getSettings().setSupportZoom(true);
        mBrowser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mBrowser.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (mBrowser.canGoBack()) {
                    mBrowserBack.setVisibility(View.VISIBLE);
                } else mBrowserBack.setVisibility(View.INVISIBLE);
                if (mBrowser.canGoForward()) {
                    mBrowserForward.setVisibility(View.VISIBLE);
                } else mBrowserForward.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ((EditText) getContentView().findViewById(R.id.browser_action_text)).setText(url);
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
                    String url = ((EditText) v).getText().toString();
                    StringBuilder completeUrl = new StringBuilder();
                    if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
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


}
