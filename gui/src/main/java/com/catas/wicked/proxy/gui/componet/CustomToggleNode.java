package com.catas.wicked.proxy.gui.componet;

import com.jfoenix.controls.JFXToggleNode;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ToggleButtonSkin;

public class CustomToggleNode extends JFXToggleNode {

    @Override
    protected Skin<?> createDefaultSkin() {
        // fix: rippler color remains bug
        return new ToggleButtonSkin(this);
    }
}
