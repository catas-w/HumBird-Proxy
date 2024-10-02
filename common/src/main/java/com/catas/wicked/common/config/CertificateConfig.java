package com.catas.wicked.common.config;

import lombok.Data;

@Data
public class CertificateConfig {

    private String id;

    private String name;

    private String cert;

    private String privateKey;
}
