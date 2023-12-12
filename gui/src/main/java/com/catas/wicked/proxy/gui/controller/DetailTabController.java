package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.constant.CodeStyle;
import com.catas.wicked.proxy.gui.componet.MessageLabel;
import com.catas.wicked.proxy.gui.componet.SideBar;
import com.catas.wicked.proxy.gui.componet.ZoomImageView;
import com.catas.wicked.proxy.gui.componet.highlight.CodeStyleLabel;
import com.catas.wicked.proxy.gui.componet.richtext.DisplayCodeArea;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextArea;
import jakarta.inject.Singleton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
@Getter
@Singleton
public class DetailTabController implements Initializable {

    @FXML
    public JFXTabPane mainTabPane;
    @FXML
    public DisplayCodeArea testCodeArea;
    @FXML
    public TableView<HeaderEntry> reqContentTable;
    @FXML
    public ZoomImageView reqImageView;
    @FXML
    public MessageLabel respHeaderMsgLabel;
    @FXML
    public MessageLabel reqHeaderMsgLabel;
    @FXML
    public MessageLabel reqContentMsgLabel;
    @FXML
    public MessageLabel respContentMsgLabel;
    @FXML
    public MessageLabel timingMsgLabel;
    @FXML
    public JFXComboBox<Labeled> reqComboBox;
    @FXML
    public SideBar respSideBar;
    @FXML
    public SideBar reqContentSideBar;
    @FXML
    public SideBar reqQuerySideBar;
    @FXML
    private JFXComboBox<Labeled> respComboBox;
    @FXML
    private ZoomImageView respImageView;
    @FXML
    private MessageLabel overViewMsgLabel;
    @FXML
    private SplitPane respSplitPane;
    @FXML
    private SplitPane reqSplitPane;
    @FXML
    private DisplayCodeArea overviewArea;
    @FXML
    private TitledPane reqPayloadTitlePane;
    @FXML
    private JFXTabPane reqPayloadTabPane;
    @FXML
    private TitledPane respHeaderPane;
    @FXML
    private TitledPane reqParamPane;
    @FXML
    private TitledPane respDataPane;
    @FXML
    private TitledPane reqHeaderPane;
    @FXML
    private TableView<HeaderEntry> reqHeaderTable;
    @FXML
    private DisplayCodeArea reqHeaderArea;
    @FXML
    private DisplayCodeArea reqParamArea;
    @FXML
    private DisplayCodeArea reqPayloadCodeArea;
    @FXML
    private JFXTextArea reqTimingArea;
    @FXML
    private DisplayCodeArea respHeaderArea;
    @FXML
    private DisplayCodeArea respContentArea;
    @FXML
    private TableView<HeaderEntry> respHeaderTable;

    private final Map<SplitPane, double[]> dividerPositionMap =new HashMap<>();

    private boolean dividerUpdating;

    private boolean midTitleCollapse;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dividerPositionMap.put(reqSplitPane, reqSplitPane.getDividerPositions().clone());
        dividerPositionMap.put(respSplitPane, respSplitPane.getDividerPositions().clone());

        addTitleListener(reqHeaderPane, reqSplitPane);
        addTitleListener(reqPayloadTitlePane, reqSplitPane);
        addTitleListener(respHeaderPane, respSplitPane);
        addTitleListener(respDataPane, respSplitPane);

        reqQuerySideBar.setTargetCodeArea(reqParamArea);
        reqContentSideBar.setTargetCodeArea(reqPayloadCodeArea);
        respSideBar.setTargetCodeArea(respContentArea);
        initComboBox(respComboBox, respSideBar);
        initComboBox(reqComboBox, reqContentSideBar);
    }

    private void initComboBox(ComboBox<Labeled> comboBox, SideBar sideBar) {
        if (comboBox.getItems().isEmpty()) {
            // comboBox.setButtonCell(new Gra);
            comboBox.getItems().add(new CodeStyleLabel("Plain", CodeStyle.PLAIN));
            comboBox.getItems().add(new CodeStyleLabel("Json", CodeStyle.JSON));
            comboBox.getItems().add(new CodeStyleLabel("Html", CodeStyle.HTML));
            comboBox.getItems().add(new CodeStyleLabel("Xml", CodeStyle.XML));
            comboBox.getItems().add(new CodeStyleLabel("Javascript", CodeStyle.JAVASCRIPT));

            comboBox.valueProperty().addListener(new ChangeListener<Labeled>() {
                @Override
                public void changed(ObservableValue<? extends Labeled> observable, Labeled oldValue, Labeled newValue) {
                    CodeStyle codeStyle = CodeStyle.valueOf(newValue.getText().toUpperCase());
                    // codeArea.setCodeStyle(codeStyle);
                    sideBar.setCodeStyle(codeStyle, false);
                }
            });
        }
        comboBox.getSelectionModel().selectFirst();
    }

    /**
     * synchronized dividers
     * @deprecated
     */
    private void bindDividerPosition(SplitPane splitPane) {
        if (splitPane.getDividers().size() < 2) {
            return;
        }
        ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
        dividers.get(0).positionProperty().addListener(((observable, oldValue, newValue) -> {
            if (dividerUpdating || splitPane.getDividers().size() < 2 || reqParamPane.isExpanded()) {
                return;
            }
            // System.out.println("Divider-0: " + newValue);
            if (newValue.doubleValue() > 0.95) {
                dividers.get(0).setPosition(0.95);
                dividers.get(1).setPosition(1.0);
                return;
            }
            dividerUpdating = true;
            double delta = newValue.doubleValue() - oldValue.doubleValue();
            dividers.get(1).setPosition(dividers.get(1).positionProperty().doubleValue() + delta);
            dividerUpdating = false;
        }));

        dividers.get(1).positionProperty().addListener(((observable, oldValue, newValue) -> {
            if (dividerUpdating || splitPane.getDividers().size() < 2 || reqParamPane.isExpanded()) {
                return;
            }
            // System.out.println("Divider-1: " + newValue);
            if (!midTitleCollapse) {
                return;
            }
            if (newValue.doubleValue() < 0.05) {
                dividers.get(0).setPosition(0.0);
                dividers.get(1).setPosition(0.05);
                return;
            }
            dividerUpdating = true;
            double delta = newValue.doubleValue() - oldValue.doubleValue();
            dividers.get(0).setPosition(dividers.get(0).positionProperty().doubleValue() + delta);
            dividerUpdating = false;
        }));
    }

    private void addTitleListener(TitledPane pane, SplitPane splitPane) {
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // open
                pane.maxHeightProperty().set(Double.POSITIVE_INFINITY);
                if (splitPane.getItems().size() == 2) {
                    splitPane.setDividerPositions(dividerPositionMap.get(splitPane));
                } else if (splitPane.getItems().size() == 3) {
                    // TODO bug: titledPane-1 expanded, titledPane-2,3 closed, tiledPane-3 cannot expand
                    int expandedNum = getExpandedNum(splitPane);
                    System.out.println("Expanded num: " + expandedNum);
                    if (expandedNum != 2) {
                        midTitleCollapse = false;
                        splitPane.setDividerPositions(0.33333, 0.66666);
                        return;
                    }
                    if (!reqParamPane.isExpanded()) {
                        ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
                        dividers.get(0).setPosition(0.5);
                        dividers.get(1).setPosition(0.5);
                    }
                }
            } else {
                // close
                if (getExpandedNum(splitPane) > 1) {
                    dividerPositionMap.put(splitPane, splitPane.getDividerPositions().clone());
                }
                pane.maxHeightProperty().set(Double.NEGATIVE_INFINITY);
                if (splitPane.getItems().size() == 3) {
                    if (getExpandedNum(splitPane) == 2 && !reqParamPane.isExpanded()) {
                        midTitleCollapse = true;
                    }
                }
            }
        });
    }
    private int getExpandedNum(SplitPane splitPane) {
        int res = 0;
        for (Node item : splitPane.getItems()) {
            if (item instanceof TitledPane titledPane) {
                res += titledPane.isExpanded() ? 1 : 0;
            }
        }
        return res;
    }

    public String getActiveRequestTab() {
        Tab selectedTab = this.mainTabPane.getSelectionModel().getSelectedItem();
        return selectedTab.getText();
    }
}
