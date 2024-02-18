package com.catas.wicked.proxy.gui.componet;

import com.catas.wicked.common.constant.ProxyProtocol;
import javafx.scene.control.Label;

public abstract class ProxyTypeLabel extends Label {

    public ProxyTypeLabel(String name) {
        super(name);
    }

    public abstract ProxyProtocol getProxyType();
}
