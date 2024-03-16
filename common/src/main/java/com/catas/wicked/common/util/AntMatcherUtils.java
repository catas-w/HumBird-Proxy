package com.catas.wicked.common.util;

import io.micronaut.core.util.AntPathMatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AntMatcherUtils {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();


    public static boolean matches(String pattern, String source) {
        if (!pattern.startsWith(WebUtils.HTTP_PREFIX) && !pattern.startsWith(WebUtils.HTTPS_PREFIX)) {
            source = WebUtils.removeProtocol(source);
        }
        return antPathMatcher.matches(pattern, source);
    }

    public static boolean matches(List<String> patterns, String source) {
        if (source == null || patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (matches(pattern, source)) {
                return true;
            }
        }
        return false;
    }


}
