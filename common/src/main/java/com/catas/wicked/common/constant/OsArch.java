package com.catas.wicked.common.constant;

import com.catas.wicked.common.util.SystemUtils;

public enum OsArch {

    X86,
    X86_64,
    ARM,
    ARM64;

    public static OsArch getCurrent() {
        String osArch = SystemUtils.OS_ARCH;
        if (osArch.contains("x86")) {
            return X86;
        } else if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            return X86_64;
        } else if (osArch.contains("arm")) {
            return ARM;
        } else if (osArch.contains("aarch64")) {
            return ARM64;
        } else {
            return null;
        }
    }
}
