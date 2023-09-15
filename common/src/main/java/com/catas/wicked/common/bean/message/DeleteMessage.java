package com.catas.wicked.common.bean.message;

import com.catas.wicked.common.bean.RequestCell;
import lombok.Data;

@Data
public class DeleteMessage extends BaseMessage{

    private RequestCell requestCell;

    public DeleteMessage(RequestCell requestCell) {
        this.requestCell = requestCell;
    }
}
