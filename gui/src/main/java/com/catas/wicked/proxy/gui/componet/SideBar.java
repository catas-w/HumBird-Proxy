package com.catas.wicked.proxy.gui.componet;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class SideBar extends HBox {

    @FXML
    private Button collapseBtn;

    @FXML
    private DropShadow shadow;

    private static final String SELECTED_STYLE = "selected";

    public SideBar() {
        URL resource = getClass().getResource("/fxml/component/side_bar.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        collapseBtn.setOnAction(new SideBarEventHandler(collapseBtn, this));

        ObservableList<Node> children = getChildren();
        children.addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        Node node = c.getAddedSubList().get(0);
                        node.setOnMouseClicked(event -> {
                            // System.out.println("Clicked!");
                            if (node.getStyleClass().contains(SELECTED_STYLE)) {
                                return;
                            }

                            for (Node sibling : getChildren()) {
                                sibling.getStyleClass().remove(SELECTED_STYLE);
                            }
                            node.getStyleClass().add(SELECTED_STYLE);
                        });

                    }
                }
            }
        });
    }

    static class SideBarEventHandler implements EventHandler<ActionEvent> {

        private Button collapseBtn;
        private double expandedWidth;
        private SideBar sideBar;
        private double minWidth;
        private boolean collapsed;
        private static final String COLLAPSE_ICON = "fas-angle-double-right";
        private static final String EXPAND_ICON = "fas-angle-double-left";
        private static final double DURATION = 500;

        public SideBarEventHandler(Button collapseBtn, SideBar sideBar) {
            this.collapseBtn = collapseBtn;
            this.sideBar = sideBar;
            this.minWidth = collapseBtn.getMinWidth();
        }

        @Override
        public void handle(ActionEvent event) {
            expandedWidth = sideBar.getWidth();

            // collapse animation
            final Animation hideSidebar = new Transition() {
                {
                    setCycleDuration(Duration.millis(DURATION));
                }

                @Override
                protected void interpolate(double frac) {
                    final double remainWidth = expandedWidth * (1.0 - frac);
                    final double curWidth = expandedWidth * frac;
                    if (remainWidth <= minWidth) {
                        return;
                    }
                    // sideBar.setPrefWidth(curWidth);
                    // collapse sideBar to minWidth
                    sideBar.setTranslateX(curWidth);
                }
            };

            hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent actionEvent) {
                    collapsed = true;
                    sideBar.setTranslateX(expandedWidth - minWidth);
                    for (Node child : sideBar.getChildren()) {
                        if (child != collapseBtn) {
                            child.setVisible(false);
                        }
                    }
                    FontIcon icon = (FontIcon) collapseBtn.getGraphic();
                    icon.setIconLiteral(EXPAND_ICON);
                }
            });

            // expand animation
            final Animation showSidebar = new Transition() {
                {
                    setCycleDuration(Duration.millis(250));
                }

                @Override
                protected void interpolate(double frac) {
                    final double curWidth = expandedWidth * frac;
                    if (curWidth >= expandedWidth - minWidth) {
                        return;
                    }
                    // sideBar.setPrefWidth(curWidth);
                    sideBar.setTranslateX(expandedWidth - curWidth - minWidth);
                }
            };

            showSidebar.onFinishedProperty().set(actionEvent -> {
                sideBar.setTranslateX(0);
                FontIcon icon = (FontIcon) collapseBtn.getGraphic();
                icon.setIconLiteral(COLLAPSE_ICON);
            });

            if (showSidebar.statusProperty().get() == Animation.Status.STOPPED
                    && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
                if (!collapsed) {
                    hideSidebar.play();
                } else {
                    for (Node child : sideBar.getChildren()) {
                        child.setVisible(true);
                    }
                    collapsed = false;
                    showSidebar.play();
                }
            }
        }
    }
}
