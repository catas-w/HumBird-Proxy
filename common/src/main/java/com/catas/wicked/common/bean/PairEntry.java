package com.catas.wicked.common.bean;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * data module for tableView
 */
public class PairEntry extends RecursiveTreeObject<PairEntry> {

    private StringProperty key;
    private StringProperty val;
    private FloatProperty time;
    private StringProperty tooltip;

    public PairEntry(String key, String val) {
        this.key = new SimpleStringProperty(key);
        this.val = new SimpleStringProperty(val);
    }

    public PairEntry(String key) {
        this.key = new SimpleStringProperty(key);
        this.val = new SimpleStringProperty("-");
    }

    public PairEntry(String key, String val, String toolTip) {
        this.key = new SimpleStringProperty(key);
        this.val = new SimpleStringProperty(val);
        this.tooltip = new SimpleStringProperty(toolTip);
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

    public String getTooltip() {
        return tooltip.get();
    }

    public StringProperty tooltipProperty() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip.set(tooltip);
    }
}
