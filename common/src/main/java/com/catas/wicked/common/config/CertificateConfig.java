package com.catas.wicked.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateConfig {

    private String id;

    private String name;

    private String cert;

    private String privateKey;

    private boolean isDefault;
}
