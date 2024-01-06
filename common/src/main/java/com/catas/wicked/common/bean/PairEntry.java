package com.catas.wicked.common.bean;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PairEntry extends RecursiveTreeObject<PairEntry> {

    private StringProperty key;
    private StringProperty val;

    public PairEntry(String key, String val) {
        this.key = new SimpleStringProperty(key);
        this.val = new SimpleStringProperty(val);
    }

    public PairEntry() {
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getVal() {
        return val.get();
    }

    public StringProperty valProperty() {
        return val;
    }

    public void setVal(String val) {
        this.val.set(val);
    }
}
