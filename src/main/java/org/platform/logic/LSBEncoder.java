package org.platform.logic;

import org.platform.exception.MessageTooLargeException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class LSBEncoder {

    public static void encode(String inputImagePath, String outputImagePath, String message, long seed)
            throws IOException, MessageTooLargeException {
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


        byte[] lengthBytes = new byte[] {
                (byte) (messageLength >> 24),
                (byte) (messageLength >> 16),
                (byte) (messageLength >> 8),
                (byte) messageLength
        };

        byte[] seedBytes = new byte[] {
                (byte) (seed >> 56),
                (byte) (seed >> 48),
                (byte) (seed >> 40),
                (byte) (seed >> 32),
                (byte) (seed >> 24),
                (byte) (seed >> 16),
                (byte) (seed >> 8),
                (byte) seed
        };

        byte[] metaData = new byte[12];
        System.arraycopy(lengthBytes, 0, metaData, 0, 4);
        System.arraycopy(seedBytes, 0, metaData, 4, 8);


        if (!canEncode(original, message)) {
            throw new MessageTooLargeException("Message is too long to encode in this image.");
        }


        int metaBitIndex = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >> 8)  & 0xFF;
                int blue  = argb & 0xFF;

                if (metaBitIndex < metaData.length * 8)
                    red = (red & 0xFE) | getBit(metaData, metaBitIndex++);
                if (metaBitIndex < metaData.length * 8)
                    green = (green & 0xFE) | getBit(metaData, metaBitIndex++);
                if (metaBitIndex < metaData.length * 8)
                    blue = (blue & 0xFE) | getBit(metaData, metaBitIndex++);

                int newARGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newARGB);

                if (metaBitIndex >= metaData.length * 8)
                    break outer;
            }
        }


        List<Point> pixels = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixels.add(new Point(x, y));
            }
        }


        pixels = pixels.subList(96 / 3, pixels.size());

        Collections.shuffle(pixels, new Random(seed));

        int dataBitIndex = 0;
        for (Point p : pixels) {
            if (dataBitIndex >= messageBytes.length * 8) break;

            int argb = image.getRGB(p.x, p.y);
            int alpha = (argb >> 24) & 0xFF;
            int red   = (argb >> 16) & 0xFF;
            int green = (argb >> 8)  & 0xFF;
            int blue  = argb & 0xFF;

            if (dataBitIndex < messageBytes.length * 8)
                red = (red & 0xFE) | getBit(messageBytes, dataBitIndex++);
            if (dataBitIndex < messageBytes.length * 8)
                green = (green & 0xFE) | getBit(messageBytes, dataBitIndex++);
            if (dataBitIndex < messageBytes.length * 8)
                blue = (blue & 0xFE) | getBit(messageBytes, dataBitIndex++);

            int newARGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
            image.setRGB(p.x, p.y, newARGB);
        }


        ImageIO.write(image, "png", new File(outputImagePath));
    }

    public static boolean canEncode(BufferedImage image, String message) {
        int capacityBits = image.getWidth() * image.getHeight() * 3;
        int requiredBits = (12 + message.getBytes(StandardCharsets.UTF_8).length) * 8;
        return requiredBits <= capacityBits;
    }

    private static int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitInByte = 7 - (bitIndex % 8);
        return (data[byteIndex] >> bitInByte) & 1;
    }
}
