package org.platform.logic;

import java.awt.image.BufferedImage;

public class ImageDifferenceUtil {

    /**
     * Generates a difference image between two images.
     * - Pixels that are the same are black.
     * - Pixels that differ are white.
     *
     * @param img1 The first image (original).
     * @param img2 The second image (encoded).
     * @return BufferedImage showing pixel-level differences.
     */
    public static BufferedImage generateBinaryDifferenceImage(BufferedImage img1, BufferedImage img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());

        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                if (rgb1 == rgb2) {
                    diffImage.setRGB(x, y, 0x000000); // black = no difference
                } else {
                    diffImage.setRGB(x, y, 0x94c3ff); // light sky = difference
                }
            }
        }

        return diffImage;
    }

    /**
     * Generates a colored difference image showing which channel(s) changed:
     * - Red channel changed → Red
     * - Green channel changed → Green
     * - Blue channel changed → Blue
     * - Multiple channels changed → Combined color
     *
     * @param img1 The first image (original).
     * @param img2 The second image (encoded).
     * @return BufferedImage with channel-aware difference visualization.
     */
    public static BufferedImage generateColoredDifferenceImage(BufferedImage img1, BufferedImage img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());

        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;

                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;

                int diffR = (r1 != r2) ? 255 : 0;
                int diffG = (g1 != g2) ? 255 : 0;
                int diffB = (b1 != b2) ? 255 : 0;

                int diffColor = (diffR << 16) | (diffG << 8) | diffB;
                diffImage.setRGB(x, y, diffColor);
            }
        }

        return diffImage;
    }
}
