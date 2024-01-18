package com.catas.wicked.proxy.gui.componet;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * image view support zoom
 */
@Slf4j
public class ZoomImageView extends ScrollPane {

    // @FXML
    private BorderPane borderPane;
    // @FXML
    private ImageView imageView;
    private Image image;
    private String url;
    private static final String STYLE = "zoom-image-view";

    private final ContextMenu contextMenu = new ImageViewContextMenu(this);
    private DoubleProperty zoomProperty = new SimpleDoubleProperty(100);


    public ZoomImageView() {
        this.getStyleClass().add(STYLE);
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
        setContextMenu(contextMenu);

        // image = new Image("/image/start.jpg");
        imageView = new ImageView();
        imageView.setPreserveRatio(true);

        borderPane = new BorderPane();
        borderPane.setCenter(imageView);
        this.setContent(borderPane);

        borderPane.prefWidthProperty().bind(this.widthProperty().subtract(4.0));
        borderPane.prefHeightProperty().bind(this.heightProperty().subtract(4.0));
        // init();
    }

    public void setImage(Image image) {
        this.image = image;
        init();
    }

    public void setImage(InputStream inputStream) {
        this.image = new Image(inputStream);
        if (this.image.isError()) {
            throw new RuntimeException("Image load error.");
        }
        init();
    }

    public void setUrl(String url) {
        this.url = url;
        try {
            this.image = new Image(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        init();
    }

    private void init() {
        imageView.setImage(image);
        imageView.setRotate(0);
        double width = image.getWidth();
        double height = image.getHeight();
        double imageRatio = width / height;
        double parentRatio = this.getWidth() / this.getHeight();

        if (imageRatio > parentRatio && width > this.getWidth()) {
            imageView.setFitWidth(this.getWidth());
        } else if (imageRatio < parentRatio && height > this.getHeight()) {
            imageView.setFitHeight(this.getHeight());
        }

        zoomWithScroll();
    }

    private void zoomWithScroll() {
        // TODO: clean previous listeners
        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                imageView.setFitHeight(image.getHeight() * (zoomProperty.get() / 100));
            }
        });

        this.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (!event.isControlDown()) {
                    return;
                }
                if (event.getDeltaY() > 0 && zoomProperty.get() < 400) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0 && zoomProperty.get() > 25) {
                    zoomProperty.set(zoomProperty.get() * 0.9);
                }
            }
        });
    }

    /**
     * clockwise rotate
     */
    private void rotate() {
        imageView.setRotate(imageView.getRotate() + 90);
    }

    private static class ImageViewContextMenu extends ContextMenu {

        final ZoomImageView zoomImageView;

        public ImageViewContextMenu(ZoomImageView zoomImageView) {
            this.zoomImageView = zoomImageView;

            MenuItem download = new MenuItem("Save as...");
            MenuItem rotate = new MenuItem("Rotate");
            rotate.setOnAction(e -> {
                zoomImageView.rotate();
            });
            getItems().addAll(download, rotate);
        }
    }
}
