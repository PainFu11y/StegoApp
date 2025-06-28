package org.platform.logic;
import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LSBDecoder {

    public static String decode(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));


        byte[] metaData = new byte[12];
        for (int i = 0; i < 96; i++) {
            int pixelIndex = i / 3;
            int x = pixelIndex % image.getWidth();
            int y = pixelIndex / image.getWidth();

            int rgb = image.getRGB(x, y);
            int colorBit = switch (i % 3) {
                case 0 -> (rgb >> 16) & 1; // R
                case 1 -> (rgb >> 8) & 1;  // G
                default -> rgb & 1;        // B
            };


            metaData[i / 8] = (byte)((metaData[i / 8] << 1) | colorBit);
        }


        int messageLength = ((metaData[0] & 0xFF) << 24) |
                ((metaData[1] & 0xFF) << 16) |
                ((metaData[2] & 0xFF) << 8)  |
                (metaData[3] & 0xFF);


        long seed = 0;
        for (int i = 4; i < 12; i++) {
            seed = (seed << 8) | (metaData[i] & 0xFF);
        }


        List<Point> allPixels = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                allPixels.add(new Point(x, y));
            }
        }


        List<Point> messagePixels = allPixels.subList(32, allPixels.size());


        Collections.shuffle(messagePixels, new Random(seed));


        byte[] messageBytes = new byte[messageLength];
        int messageBitIndex = 0;

        for (Point p : messagePixels) {
            if (messageBitIndex >= messageLength * 8) break;

            int rgb = image.getRGB(p.x, p.y);

            for (int channel = 0; channel < 3 && messageBitIndex < messageLength * 8; channel++) {
                int bit = switch (channel) {
                    case 0 -> (rgb >> 16) & 1; // R
                    case 1 -> (rgb >> 8) & 1;  // G
                    case 2 -> rgb & 1;         // B
                    default -> throw new IllegalStateException("Unexpected channel");
                };

                messageBytes[messageBitIndex / 8] = (byte)((messageBytes[messageBitIndex / 8] << 1) | bit);
                messageBitIndex++;
            }
        }


        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
