package com.catas.wicked.proxy.gui.componet;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ButtonSkin;

public class CustomButton extends JFXButton {

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ButtonSkin(this);
    }
}
