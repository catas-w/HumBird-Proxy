package com.catas.wicked.common.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface WindowBlurLibrary extends Library {
    WindowBlurLibrary INSTANCE = Native.load(
            "WindowBlurLib",
            WindowBlurLibrary.class,
            Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

    void setBlurWindow(NativeLong address, NativeLong address2);
}