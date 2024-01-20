package com.catas.wicked.common.util;

import com.luciad.imageio.webp.WebPReadParam;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ImageUtils {

    private static final WebPReadParam readParam;

    static {
        readParam = new WebPReadParam();
        readParam.setBypassFiltering(true);
    }

    /**
     * convert bufferedImage to javafx image
     * @param bufferedImage bufferedImage
     * @return javafx.scene.image.Image
     */
    public static Image getJFXImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
             throw new RuntimeException("Buffered image cannot be null.");
        }
        WritableImage image = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }

    /**
     * parse webp format image
     * @param inputStream image inputStream
     * @return bufferedImage
     * @throws IOException
     */
    public static BufferedImage encodeWebpImage(InputStream inputStream) throws IOException {
        ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
        if (inputStream instanceof ImageInputStream) {
            reader.setInput(inputStream);
        } else {
            MemoryCacheImageInputStream imageInputStream = new MemoryCacheImageInputStream(inputStream);
            reader.setInput(imageInputStream);
        }
        BufferedImage image = reader.read(0, readParam);
        return image;
    }
}
