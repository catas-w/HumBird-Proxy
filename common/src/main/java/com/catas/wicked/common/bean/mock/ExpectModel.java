package com.catas.wicked.common.bean.mock;

import com.catas.wicked.common.bean.StrPair;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.common.constant.GeneralContentType;
import lombok.Data;

import java.util.List;

@Data
public class ExpectModel {

    private ClientStatus status;

    private int code;

    private List<StrPair> headers;

    private GeneralContentType contentType;

    private Object content;

    private List<ContainsItem> containsList;

    private boolean requestOversize;

    private boolean respOversize;

    @Data
    public static class ContainsItem {
        String type;
        String content;
    }
}
