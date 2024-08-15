package com.catas.wicked.common.config;

import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.constant.SystemProxyStatus;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObservableConfig {

    /**
     * status of proxy server
     */
    private final SimpleObjectProperty<ServerStatus> serverStatus = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<SystemProxyStatus> systemProxyStatus = new SimpleObjectProperty<>();

    public ObservableConfig() {
        serverStatus.addListener((observable, oldValue, newValue) -> {
            // TODO display alert
            log.info("Observable Server status: " + newValue);
        });

        systemProxyStatus.addListener(((observable, oldValue, newValue) -> {
            // TODO update gui
            System.out.println("== SystemProxyStatus: " + newValue);
        }));
    }

    public ServerStatus getServerStatus() {
        return serverStatus.get();
    }

    public SimpleObjectProperty<ServerStatus> serverStatusProperty() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus.set(serverStatus);
    }

    public SystemProxyStatus getSystemProxyStatus() {
        return systemProxyStatus.get();
    }

    public SimpleObjectProperty<SystemProxyStatus> systemProxyStatusProperty() {
        return systemProxyStatus;
    }

    public void setSystemProxyStatus(SystemProxyStatus systemProxyStatus) {
        this.systemProxyStatus.set(systemProxyStatus);
    }
}
