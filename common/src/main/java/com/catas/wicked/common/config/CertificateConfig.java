package com.catas.wicked.common.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateConfig {

    private String id;

    private String name;

    private String cert;

    private String privateKey;

    private boolean isDefault;
}
