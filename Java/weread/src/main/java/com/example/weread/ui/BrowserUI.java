package com.example.weread.ui;

import com.example.weread.core.BrowserEventListener;
import com.example.weread.core.BrowserService;
import com.example.weread.core.JcefBrowserService;
import com.example.weread.core.UserAgent;

import javax.swing.*;
import java.awt.*;

/**
 * @author anlingyi
 * @date 2022/8/14 11:48 AM
 */
public class BrowserUI extends JPanel {

    private final static String HOME_PAGE = "https://weread.qq.com/";

    private BrowserService browserService;

    private String lastUrl;

    private UserAgent userAgent;

    private Component browserUI;

    public BrowserUI() {
        userAgent = UserAgent.getUserAgent();
        initPanel();
    }

    private void initPanel() {
        removeAll();

        String url = HOME_PAGE;
        if (lastUrl != null) {
            url = lastUrl;
        }

        if (browserService != null) {
            browserService.close();
        }
        browserService = new JcefBrowserService(url);
        browserService.setUserAgent(userAgent);

        browserUI = browserService.getUI();

        setLayout(new BorderLayout());
        add(browserUI, BorderLayout.CENTER);
        add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        updateUI();

        browserService.addEventListener(new BrowserEventListener() {
            @Override
            public void onAddressChange(String url) {
                lastUrl = url;
            }

            @Override
            public void onBeforeClose() {
                SwingUtilities.invokeLater(() -> initPanel());
            }
        });
    }

    public void close() {
        browserService.close();
    }

}
