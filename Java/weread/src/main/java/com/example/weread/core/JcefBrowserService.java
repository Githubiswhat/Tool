package com.example.weread.core;

import cn.hutool.core.util.StrUtil;
import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

import java.awt.*;

/**
 * @author anlingyi
 * @date 2022/8/15 2:06 PM
 */
public class JcefBrowserService implements BrowserService {

    private final JBCefBrowser jbCefBrowser;

    private final CefBrowser cefBrowser;

    private final CefClient client;

    private UserAgent userAgent;

    private BrowserEventListener eventListener;

    public JcefBrowserService(String url) {
        jbCefBrowser = new JBCefBrowser(url);
        cefBrowser = jbCefBrowser.getCefBrowser();
        client = cefBrowser.getClient();
        userAgent = UserAgent.getUserAgent();
        eventListener = new BrowserEventListener() {
        };
        initAddEvent();
    }

    private void initAddEvent() {
        client.removeRequestHandler();
        client.addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
                return new CefResourceRequestHandlerAdapter() {
                    @Override
                    public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
                        String ua = userAgent.getValue();
                        if (ua != null) {
                            request.setHeaderByName("User-Agent", ua, true);
                        }
                        return false;
                    }
                };
            }
        });

        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                if (!StrUtil.startWith(url, "http")) {
                    return;
                }
                eventListener.onAddressChange(url);
            }
        });

        client.removeLifeSpanHandler();
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                if (StrUtil.endWithAnyIgnoreCase(target_url, "jpg", "png", "gif", "svg", "pdf", "bmp", "webp")) {
                    return false;
                }
                loadURL(target_url);
                return true;
            }

            @Override
            public void onBeforeClose(CefBrowser browser) {
                close();
                eventListener.onBeforeClose();
            }
        });
    }

    @Override
    public Component getUI() {
        return jbCefBrowser.getComponent();
    }

    @Override
    public void loadURL(String url) {
        jbCefBrowser.loadURL(url);
    }

    @Override
    public void close() {
        client.dispose();
        jbCefBrowser.dispose();
    }

    @Override
    public void setUserAgent(UserAgent userAgent) {
        if (userAgent == null) {
            return;
        }
        this.userAgent = userAgent;
    }

    @Override
    public void addEventListener(BrowserEventListener listener) {
        if (listener == null) {
            return;
        }
        eventListener = listener;
    }
}
