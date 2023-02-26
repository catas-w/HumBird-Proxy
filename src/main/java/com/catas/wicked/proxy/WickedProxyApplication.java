package com.catas.wicked.proxy;

import com.catas.wicked.proxy.gui.ApplicationView;
import com.catas.wicked.proxy.gui.CustomLoadingView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WickedProxyApplication extends AbstractJavaFxApplicationSupport {


    public static void main(String[] args) {
        launch(WickedProxyApplication.class, ApplicationView.class, new CustomLoadingView(), args);
    }
}
