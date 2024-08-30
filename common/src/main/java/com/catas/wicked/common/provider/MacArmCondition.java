package com.catas.wicked.common.provider;

import com.catas.wicked.common.constant.OsArch;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.context.condition.OperatingSystem;

/**
 * condition for MacOS & arm64
 */
public class MacArmCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context) {
        return OperatingSystem.getCurrent().isMacOs() && OsArch.getCurrent() == OsArch.ARM64;
    }
}
