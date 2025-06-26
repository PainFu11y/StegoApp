package org.platform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LSBEncoder {

    public static void encode(String inputImagePath, String outputImagePath, String message) throws IOException {
        BufferedImage original = ImageIO.read(new File(inputImagePath));


        BufferedImage image = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = image.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();


        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;


        byte[] lengthBytes = new byte[]{
                (byte) (messageLength >> 24),
                (byte) (messageLength >> 16),
                (byte) (messageLength >> 8),
                (byte) (messageLength)
        };


        byte[] fullData = new byte[lengthBytes.length + messageBytes.length];
        System.arraycopy(lengthBytes, 0, fullData, 0, 4);
        System.arraycopy(messageBytes, 0, fullData, 4, messageBytes.length);


        int capacity = image.getWidth() * image.getHeight() * 3;
        if (fullData.length * 8 > capacity) {
            throw new IllegalArgumentException("Message is too long to encode in this image.");
        }


        int dataBitIndex = 0;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);

                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                if (dataBitIndex < fullData.length * 8) {
                    red = (red & 0xFE) | getBit(fullData, dataBitIndex++);
                }
                if (dataBitIndex < fullData.length * 8) {
                    green = (green & 0xFE) | getBit(fullData, dataBitIndex++);
                }
                if (dataBitIndex < fullData.length * 8) {
                    blue = (blue & 0xFE) | getBit(fullData, dataBitIndex++);
                }

                int newARGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newARGB);

                if (dataBitIndex >= fullData.length * 8) break outer;
            }
        }




        ImageIO.write(image, "png", new File(outputImagePath));
    }

    private static int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitInByte = 7 - (bitIndex % 8);
        return (data[byteIndex] >> bitInByte) & 1;
    }
}
