package com.catas.wicked.server.cert.spi;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Author: Proxyee
 */
public interface CertGenerator {
    /**
     * 生成服务端自签名证书.
     * @param issuer 元数据(X509 Names)
     * @param caPriKey 用于进行签名的 CA 私钥.
     * @param caNotBefore 证书生效时间, 在这个时间之前证书也是失效的.
     * @param caNotAfter 证书失效时间, 过了这个时间后证书即失效.
     * @param serverPubKey 服务端证书公钥.
     * @param hosts 证书所属域名.
     * @return 返回指定域名所属的服务端 X509 证书.
     * @throws Exception 当发生任意异常时, 异常将直接抛出至调用方.
     */
    X509Certificate generateServerCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
                                       Date caNotAfter, PublicKey serverPubKey,
                                       String... hosts) throws Exception;

    /**
     * 生成 CA 证书(自签名).
     * @param subject 元数据(X509 Names)
     * @param caNotBefore 证书生效时间, 在这个时间之前证书也是失效的.
     * @param caNotAfter 证书失效时间, 过了这个时间后证书即失效.
     * @param keyPair RSA 密钥对.
     * @return 返回自签名 CA 证书.
     * @throws Exception 当发生任意异常时, 异常将直接抛出至调用方.
     */
    X509Certificate generateCaCert(String subject, Date caNotBefore, Date caNotAfter, KeyPair keyPair) throws Exception;

}
