package com.catas.wicked.common.util;

import com.catas.wicked.BaseTest;
import com.catas.wicked.common.constant.OsArch;
import org.junit.jupiter.api.Test;

public class SystemUtilsTest extends BaseTest {

    @Test
    public void testSysInfo() {
        String osInfo = SystemUtils.getOsInfo();
        System.out.println(osInfo);
        System.out.println(OsArch.getCurrent());
    }
}
