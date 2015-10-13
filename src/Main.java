import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by nigonzalez on 9/21/15.
 */
public class Main {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;

    enum Channel {
        RED,
        GREEN,
        BLUE
    }

    static class RGB {

        int r;
        int g;
        int b;

        public RGB() {
            r = 0;
            g = 0;
            b = 0;
        }

        public RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public RGB(RGB pixel) {
            this.r = pixel.r;
            this.g = pixel.g;
            this.b = pixel.b;
        }

    }

    static class Rect {

        int x;
        int y;
        int w;
        int h;

    }

    public static void saveImage(RGB[] data, String filename) {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                int index = j * WIDTH + i;
                RGB pixel = data[index];
                int rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b;
                bufferedImage.setRGB(i, j, rgb);
            }
        }
        File imageFile = new File(filename + ".png");
        try {
            ImageIO.write(bufferedImage, "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveImage(RGB[] data, int scale, String filename) {
        if (scale < 1) {
            return;
        }
        BufferedImage bufferedImage = new BufferedImage(scale * WIDTH, scale * HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < scale * WIDTH; i += scale) {
            for (int j = 0; j < scale * HEIGHT; j += scale) {
                int index = (j / scale) * WIDTH + (i / scale);
                RGB pixel = data[index];
                int rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b;
                for (int si = 0; si < scale; ++si) {
                    for (int sj = 0; sj < scale; sj++) {
                        bufferedImage.setRGB(i + si, j + sj, rgb);
                    }
                }
            }
        }
        File imageFile = new File(filename + "_" + Integer.toString(scale) + ".png");
        try {
            ImageIO.write(bufferedImage, "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void saveHistogram(TreeMap<Integer, Integer> data, String filename) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename + ".csv"), "utf-8"));
            for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
                String line = Integer.toString(entry.getKey()) + "," + Integer.toString(entry.getValue()) + "\n";
                writer.write(line);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TreeMap<Integer, Integer> getHistogram(RGB[] data, Channel channel) {
        TreeMap<Integer, Integer> histogram = new TreeMap<>();
        for (int i = 0; i < data.length; ++i) {
            int value;
            switch (channel) {
                case RED:
                    value = data[i].r;
                    break;
                case GREEN:
                    value = data[i].g;
                    break;
                case BLUE:
                    value = data[i].b;
                    break;
                default: // should never happen
                    value = 0;
                    break;
            }
            if (histogram.containsKey(value)) {
                int count = histogram.get(value);
                count++;
                histogram.put(value, count);
            } else {
                histogram.put(value, 1);
            }
        }
        return histogram;
    }

    public static RGB[] getBinaryImage(RGB[] image, Channel channel) {
        RGB[] binaryImage = new RGB[image.length];
        for (int i = 0; i < image.length; ++i) {
            int binaryRed = 0, binaryGreen = 0, binaryBlue = 0;
            switch (channel) {
                case RED:
                    int r = (image[i].r - image[i].g) + (image[i].r - image[i].b);
                    binaryRed = (r > 40) ? 255 : 0;
                    break;
                case GREEN:
                    int g = (image[i].g - image[i].r) + (image[i].g - image[i].b);
                    binaryGreen = (g > 40) ? 255 : 0;
                    break;
                case BLUE:
                    int b = (image[i].b - image[i].r) + (image[i].b - image[i].g);
                    binaryBlue = (b > 40) ? 255 : 0;
                    break;
            }
            binaryImage[i] = new RGB(binaryRed, binaryGreen, binaryBlue);
        }
        return binaryImage;
    }

    public static int getColorForPixel(RGB[] image, int x, int y, Channel channel) {
        int index = y * WIDTH + x;
        switch (channel) {
            case RED:
                return image[index].r;
            case GREEN:
                return image[index].g;
            case BLUE:
                return image[index].b;
        }
        return -1; // should never happen
    }

    public static Rect[] sDistribution = new Rect[WIDTH * HEIGHT];

    public static RGB[] getDistributionImage(RGB[] image, Channel channel) {
        RGB[] distribution = new RGB[image.length];
        int[] absolutes = new int[image.length];
        int max = 0;
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                int index = j * WIDTH + i;
                int color = getColorForPixel(image, i, j, channel);
                int t = j, r = i, b = j, l = i;
                if (color > 0) {
                    while (getColorForPixel(image, i, t, channel) > 40) {
                        t--;
                        if (t > 0) {
                            if (getColorForPixel(image, i, t, channel) == 0) {
                                t++;
                                break;
                            }
                        } else {
                            t++;
                            break;
                        }
                    }
                    while (getColorForPixel(image, r, j, channel) > 40) {
                        r++;
                        if (r < WIDTH - 1) {
                            if (getColorForPixel(image, r, j, channel) == 0) {
                                r--;
                                break;
                            }
                        } else {
                            r--;
                            break;
                        }
                    }
                    while (getColorForPixel(image, i, b, channel) > 40) {
                        b++;
                        if (b < HEIGHT - 1) {
                            if (getColorForPixel(image, i, b, channel) == 0) {
                                b--;
                                break;
                            }
                        } else {
                            b--;
                            break;
                        }
                    }
                    while (getColorForPixel(image, l, j, channel) > 40) {
                        l--;
                        if (l > 0) {
                            if (getColorForPixel(image, l, j, channel) == 0) {
                                l++;
                                break;
                            }
                        } else {
                            l++;
                            break;
                        }
                    }
                    absolutes[index] = (b - t) * (r - l);
                    if (absolutes[index] > max) {
                        max = absolutes[index];
                    }
                } else {
                    absolutes[index] = 0;
                }
                Rect rect = new Rect();
                rect.h = b - t;
                rect.w = r - l;
                rect.x = l + rect.w / 2;
                rect.y = t + rect.h / 2;
                sDistribution[index] = rect;
            }
        }
        for (int i = 0; i < image.length; ++i) {
            int color = (int)(255.0 * ((double)absolutes[i] / max));
            switch (channel) {
                case RED:
                    distribution[i] = new RGB(color, 0, 0);
                    break;
                case GREEN:
                    distribution[i] = new RGB(0, color, 0);
                    break;
                case BLUE:
                    distribution[i] = new RGB(0, 0, color);
                    break;
            }
        }
        return distribution;
    }

    public static Rect getRectForImage(RGB[] image, Channel channel) {
        int iteration = 0;
        while (iteration < WIDTH * HEIGHT / 2) {
            int pixel = (int)Math.floor(Math.random() * WIDTH * HEIGHT);
            int x = pixel % WIDTH;
            int y = pixel / WIDTH;
            int area = 0;
            int newArea = 1;
            Rect rect = null;
            while (newArea > area) {
                area = newArea;
                if (getColorForPixel(image, x, y, channel) > 0) {
                    rect = sDistribution[pixel];
                    newArea = rect.w * rect.h;
                    x = rect.x;
                    y = rect.y;
                }
            }
            if (rect != null) {
                return rect;
            }
            iteration++;
        }
        return null;
    }

    public static RGB[] granularityFilter(RGB[] data, Channel channel) {
        RGB[] granularity = new RGB[data.length];
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                int index = j * WIDTH + i;
                if (i == 0 || i == WIDTH - 1 || j == 0 || j == HEIGHT - 1) {
                    granularity[index] = new RGB();
                    continue;
                }
                int[] indexes = new int[8];
                indexes[0] = (j - 1) * WIDTH + (i - 1);
                indexes[1] = (j - 1) * WIDTH + i;
                indexes[2] = (j - 1) * WIDTH + (i + 1);
                indexes[3] = j * WIDTH + (i - 1);
                indexes[4] = j * WIDTH + (i + 1);
                indexes[5] = (j + 1) * WIDTH + (i - 1);
                indexes[6] = (j + 1) * WIDTH + i;
                indexes[7] = (j + 1) * WIDTH + (i + 1);
                int counter = 0;
                for (int k = 0; k < indexes.length; ++k) {
                    switch (channel) {
                        case RED:
                            if (data[indexes[k]].r > 0) counter++;
                            break;
                        case GREEN:
                            if (data[indexes[k]].g > 0) counter++;
                            break;
                        case BLUE:
                            if (data[indexes[k]].b > 0) counter++;
                            break;
                    }
                }
                if (counter > 4) {
                    RGB pixel = new RGB();
                    switch (channel) {
                        case RED:
                            pixel = new RGB(255, 0, 0);
                            break;
                        case GREEN:
                            pixel = new RGB(0, 255, 0);
                            break;
                        case BLUE:
                            pixel = new RGB(0, 0, 255);
                            break;
                    }
                    granularity[index] = pixel;
                } else {
                    granularity[index] = new RGB();
                }
            }
        }
        return granularity;
    }

    public static void drawRectangleInImage(RGB[] image, int x, int y, int w, int h) {
        int index;
        int x0 = x - w / 2;
        int y0 = y - h / 2;
        for (int i = 0; i <= w; ++i) {
            index = y0 * WIDTH + (x0 + i);
            image[index] = new RGB(0, 255, 0);
            index = (y0 + h) * WIDTH + (x0 + i);
            image[index] = new RGB(0, 255, 0);
        }
        for (int i = 0; i <= h; ++i) {
            index = (y0 + i) * WIDTH + x0;
            image[index] = new RGB(0, 255, 0);
            index = (y0 + i) * WIDTH + (x0 + w);
            image[index] = new RGB(0, 255, 0);
        }
    }

    public static RGB[] getImage(int[] rawData) {
        int index;
        RGB[] image = new RGB[WIDTH * HEIGHT];
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                index = j * WIDTH + i;
                int h = rawData[2 * index];
                int l = rawData[2 * index + 1];
                int r = h & 0xF8;
                int g = ((h & 0x07) << 5) | ((l & 0xE0) >> 3);
                int b = (l & 0x1F) << 3;
                RGB pixel = new RGB(r, g, b);
                image[index] = pixel;
            }
        }
        return image;
    }

    public static RGB[] getAverageImage(RGB[] image) {
        RGB[] avgImage = new RGB[image.length];
        for (int i = 0; i < image.length; ++i) {
            int avgRed = 0, avgGreen = 0, avgBlue = 0;
            RGB avgPixel;
            int x = i % WIDTH;
            int y = i / WIDTH;
            if (x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1) {
                avgPixel = new RGB(image[i]);
            } else {
                int[] indexes = new int[9];
                indexes[0] = (y - 1) * WIDTH + (x + 1);
                indexes[1] = (y - 1) * WIDTH + x;
                indexes[2] = (y - 1) * WIDTH + (x + 1);
                indexes[3] = y * WIDTH + (x + 1);
                indexes[4] = y * WIDTH + x;
                indexes[5] = y * WIDTH + (x + 1);
                indexes[6] = (y + 1) * WIDTH + (x + 1);
                indexes[7] = (y + 1) * WIDTH + x;
                indexes[8] = (y + 1) * WIDTH + (x + 1);
                for (int k = 0; k < indexes.length; ++k) {
                    avgRed += image[indexes[k]].r;
                    avgGreen += image[indexes[k]].g;
                    avgBlue += image[indexes[k]].b;
                }
                avgPixel = new RGB(avgRed / 9, avgGreen / 9, avgBlue / 9);
            }
            avgImage[i] = avgPixel;
        }
        return avgImage;
    }

    public static RGB[] getMedianImage(RGB[] image) {
        RGB[] medianImage = new RGB[image.length];
        for (int i = 0; i < image.length; ++i) {
            int medianRed, medianGreen, medianBlue;
            RGB medianPixel;
            int x = i % WIDTH;
            int y = i / WIDTH;
            if (x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1) {
                medianPixel = new RGB(image[i]);
            } else {
                ArrayList<Integer> medianReds = new ArrayList<>();
                ArrayList<Integer> medianGreens = new ArrayList<>();
                ArrayList<Integer> medianBlues = new ArrayList<>();
                int[] indexes = new int[9];
                indexes[0] = (y - 1) * WIDTH + (x + 1);
                indexes[1] = (y - 1) * WIDTH + x;
                indexes[2] = (y - 1) * WIDTH + (x + 1);
                indexes[3] = y * WIDTH + (x + 1);
                indexes[4] = y * WIDTH + x;
                indexes[5] = y * WIDTH + (x + 1);
                indexes[6] = (y + 1) * WIDTH + (x + 1);
                indexes[7] = (y + 1) * WIDTH + x;
                indexes[8] = (y + 1) * WIDTH + (x + 1);
                for (int k = 0; k < indexes.length; ++k) {
                    medianReds.add(image[indexes[k]].r);
                    medianGreens.add(image[indexes[k]].g);
                    medianBlues.add(image[indexes[k]].b);
                }
                Collections.sort(medianReds);
                Collections.sort(medianGreens);
                Collections.sort(medianBlues);
                medianRed = medianReds.get(4);
                medianGreen = medianGreens.get(4);
                medianBlue = medianBlues.get(4);
                medianPixel = new RGB(medianRed, medianGreen, medianBlue);
            }
            medianImage[i] = medianPixel;
        }
        return medianImage;
    }

    public static void generateFramesFromFile(String filename) {
        InputStream inputStream = Main.class.getResourceAsStream(filename);
        Scanner scanner = new Scanner(inputStream);
        int index = 0;
        int frame = 0;
        int[] data = new int[2 * WIDTH * HEIGHT];
        while (scanner.hasNextInt()) {
            data[index++] = scanner.nextInt();
            if (index == data.length) {
                index = 0;
                String file = String.format("frame2_%d", frame++);
                saveImage(getImage(data), file);
            }
        }
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.exit(0);
        }
        String filename = args[0];
        int frameToLook = Integer.parseInt(args[1]);
        InputStream inputStream = Main.class.getResourceAsStream(filename);
        Scanner scanner = new Scanner(inputStream);
        int index = 0;
        int frame = 0;
        int[] data = new int[2 * WIDTH * HEIGHT];
        while (scanner.hasNextInt()) {
            data[index++] = scanner.nextInt();
            if (index == data.length) {
                index = 0;
                if (frame == frameToLook) {
                    break;
                } else {
                    frame++;
                }
            }
        }
        RGB[] image = getImage(data);
        saveImage(image, 3, "frame");
        RGB[] average = getAverageImage(image);
        RGB[] median = getMedianImage(image);
        RGB[] binary = getBinaryImage(median, Channel.RED);
        RGB[] granularity = granularityFilter(binary, Channel.RED);
        RGB[] distribution = getDistributionImage(granularity, Channel.RED);
        Rect rect = getRectForImage(granularity, Channel.RED);
        if (rect != null) {
            drawRectangleInImage(image, rect.x, rect.y, rect.w, rect.h);
            saveImage(image, 3, "frame_detected");
        }
        saveImage(average, 3, "frame_average");
        saveImage(median, 3, "frame_median");
        saveImage(binary, 3, "frame_binary");
        saveImage(granularity, 3, "frame_granularity");
        saveImage(distribution, 3, "frame_distribution");
        saveHistogram(getHistogram(image, Channel.RED), "histogram");
        saveHistogram(getHistogram(average, Channel.RED), "histogram_average");
        saveHistogram(getHistogram(median, Channel.RED), "histogram_median");

        //generateFramesFromFile(args[0]);
    }

}
