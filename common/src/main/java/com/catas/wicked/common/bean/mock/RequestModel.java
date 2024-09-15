package com.catas.wicked.common.bean.mock;

import com.catas.wicked.common.bean.StrPair;
import com.catas.wicked.common.constant.GeneralContentType;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;

import java.util.List;

/**
 * Request data for mocking TODO: move to test package
 */
@Data
public class RequestModel {

    private String protocol;

    private String url;

    private HttpMethod method;

    private List<StrPair> queryParams;

    private List<StrPair> headers;

    private List<StrPair> formData;

    private GeneralContentType contentType;

    private JsonNode content;

    private ExpectModel expect;
}
