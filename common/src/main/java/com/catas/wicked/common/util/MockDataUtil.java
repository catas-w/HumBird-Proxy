package com.catas.wicked.common.util;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.test.RequestModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MockDataUtil {

    private final ObjectMapper objectMapper;

    public MockDataUtil() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /**
     * parse json file into requestModel
     * @param fileName json file
     */
    public List<RequestModel> loadRequestModel(String fileName) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return loadRequestModel(fileName, this.getClass());
    }

    /**
     * parse json file into requestModel
     * @param fileName json file
     * @param clazz class to get resource
     */
    public List<RequestModel> loadRequestModel(String fileName, Class<?> clazz) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(fileName);
        return loadRequestModel(inputStream);
    }


    public List<RequestModel> loadRequestModel(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        List<RequestModel> list = objectMapper.readValue(inputStream, new TypeReference<List<RequestModel>>() {});
        log.info("RequestModel list size: {}", list.size());
        return list;
    }

    /**
     * get HttpUriRequest from requestModel
     */
    public HttpUriRequest getUriRequest(RequestModel requestModel) throws UnsupportedEncodingException {
        if (requestModel == null) {
            throw new IllegalArgumentException();
        }
        RequestBuilder builder = RequestBuilder
                .create(requestModel.getMethod().name())
                .setUri(requestModel.getUrl());
        if (requestModel.getQueryParams() != null) {
            requestModel.getQueryParams().forEach(item -> builder.addParameter(item.getKey(), item.getValue()));
        }
        if (requestModel.getHeaders() != null) {
            requestModel.getHeaders().forEach(item -> builder.addHeader(item.getKey(), item.getValue()));
        }
        if (requestModel.getContent() != null && requestModel.getContentType() != null) {
            JsonNode content = requestModel.getContent();
            switch (requestModel.getContentType()) {
                case TEXT_JSON,TEXT,TEXT_XML,TEXT_HTML -> {
                    HttpEntity entity = new StringEntity(content.asText());
                    builder.setEntity(entity);
                }
                case IMAGE, BINARY -> {
                    FileEntity entity = new FileEntity(new File(content.get("file").asText()));
                    builder.setEntity(entity);
                }
                case QUERY_FORM -> {
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
                    content.fields().forEachRemaining(field ->
                            parameters.add(new BasicNameValuePair(field.getKey(), field.getValue().asText())));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
                    builder.setEntity(entity);
                }
                case MULTIPART_FORM -> {
                    // String only
                    MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
                    content.fields().forEachRemaining(field -> multipartBuilder.addPart(field.getKey(),
                                    new StringBody(field.getValue().asText(),  ContentType.TEXT_PLAIN)));
                    builder.setEntity(multipartBuilder.build());
                }
            }
        }
        return builder.build();
    }

    public boolean validate(RequestModel requestModel, RequestMessage requestMessage) {
        return false;
    }

    public boolean validate(RequestModel requestModel, ResponseMessage respMessage) {
        return false;
    }

}
