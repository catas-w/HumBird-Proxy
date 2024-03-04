package com.catas.wicked.common.util;

import com.catas.wicked.common.bean.StrPair;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.mock.RequestModel;
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
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
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
    public HttpUriRequest getUriRequest(RequestModel requestModel) throws UnsupportedEncodingException, URISyntaxException {
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
        if (requestModel.getContentType() != null) {
            JsonNode content = requestModel.getContent();
            List<StrPair> formData = requestModel.getFormData();
            switch (requestModel.getContentType()) {
                case TEXT_JSON,TEXT,TEXT_XML,TEXT_HTML -> {
                    if (content != null) {
                        HttpEntity entity = new StringEntity(content.asText());
                        builder.setEntity(entity);
                    }
                }
                case IMAGE, BINARY -> {
                    if (content != null) {
                        FileEntity entity = new FileEntity(new File(content.get("file").asText()));
                        builder.setEntity(entity);
                    }
                }
                case QUERY_FORM -> {
                    if (formData != null) {
                        List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
                        formData.forEach(strPair ->
                                parameters.add(new BasicNameValuePair(strPair.getValue(), strPair.getValue())));
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
                        builder.setEntity(entity);
                    }
                }
                case MULTIPART_FORM -> {
                    if (formData == null) {
                        break;
                    }
                    // HttpPost httpPost = new HttpPost(requestModel.getUrl());
                    MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
                    for (StrPair item : formData) {
                        ContentBody contentBody = null;
                        if (item.getValue().startsWith("file:")) {
                            URL fileUrl = getClass().getClassLoader().getResource(item.getValue().substring(5));
                            contentBody = new FileBody(new File(fileUrl.toURI()), ContentType.MULTIPART_FORM_DATA);
                        } else {
                            contentBody = new StringBody(item.getValue(), ContentType.TEXT_PLAIN);
                        }
                        multipartBuilder.addPart(item.getKey(), contentBody);
                    }
                    HttpEntity multipartEntity = multipartBuilder.build();
                    // httpPost.setEntity(multipartEntity);
                    // return httpPost;
                    builder.setEntity(multipartEntity);
                    // builder.addHeader(multipartEntity.getContentType());
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
