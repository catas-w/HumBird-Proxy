package com.catas.wicked.common.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface CertInstallerLibrary extends Library {

    CertInstallerLibrary INSTANCE = Native.load(
            "CertInstallerLib",
            CertInstallerLibrary.class,
            Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

    boolean installCert(String certPath);
}
