package com.catas.wicked.common.jna;

import com.catas.wicked.common.constant.BlurOption;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.quantum.WindowStage;
import com.sun.jna.NativeLong;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JnaUtils {

    public static void setBlurWindow(long windowPtr, BlurOption blurOption) {
        if (blurOption == null) {
            throw new IllegalArgumentException();
        }
        setBlurWindow(windowPtr, blurOption.getNativeName());
    }

    public static void setBlurWindow(long windowPtr, String nativeAppearanceName) {
        NativeLong nsWindow = new NativeLong(windowPtr);
        WindowBlurLibrary.INSTANCE.setBlurWindow(nsWindow, FoundationLibrary.fromJavaString(nativeAppearanceName));
    }

    public static long getNativeHandleOfStage(Window stage) {
        try {
            WindowStage peer = (WindowStage) WindowHelper.getPeer(stage);
            com.sun.glass.ui.Window platformWindow = peer.getPlatformWindow();
            return platformWindow.getNativeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0L;
        }
    }

}
