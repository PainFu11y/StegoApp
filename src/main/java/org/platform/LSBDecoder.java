package org.platform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LSBDecoder {

    public static String decode(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));

        int bitIndex = 0;
        byte[] lengthBytes = new byte[4];


        for (int i = 0; i < 32; i++) {
            int x = (bitIndex / 3) % image.getWidth();
            int y = (bitIndex / 3) / image.getWidth();
            int rgb = image.getRGB(x, y);

            int colorComponent = switch (bitIndex % 3) {
                case 0 -> (rgb >> 16) & 1; // R
                case 1 -> (rgb >> 8) & 1;  // G
                default -> rgb & 1;        // B
            };

            lengthBytes[i / 8] = (byte)((lengthBytes[i / 8] << 1) | colorComponent);
            bitIndex++;
        }

        int messageLength = ((lengthBytes[0] & 0xFF) << 24) |
                ((lengthBytes[1] & 0xFF) << 16) |
                ((lengthBytes[2] & 0xFF) << 8) |
                (lengthBytes[3] & 0xFF);

        byte[] messageBytes = new byte[messageLength];

        for (int i = 0; i < messageLength * 8; i++) {
            int x = (bitIndex / 3) % image.getWidth();
            int y = (bitIndex / 3) / image.getWidth();
            int rgb = image.getRGB(x, y);

            int colorComponent = switch (bitIndex % 3) {
                case 0 -> (rgb >> 16) & 1;
                case 1 -> (rgb >> 8) & 1;
                default -> rgb & 1;
            };

            messageBytes[i / 8] = (byte)((messageBytes[i / 8] << 1) | colorComponent);
            bitIndex++;
        }

        return new String(messageBytes, "UTF-8");
    }
}
