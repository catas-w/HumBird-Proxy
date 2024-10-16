package com.catas.wicked.common.bean.message;

import com.catas.wicked.common.bean.RequestCell;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteMessage extends BaseMessage{

    public enum Source {
        TREE_VIEW,
        LIST_VIEW
    }

    private Source source;

    private RequestCell requestCell;

    private boolean cleanLeaves;

    private boolean removeAll;
}
