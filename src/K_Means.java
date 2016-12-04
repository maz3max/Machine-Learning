import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by max on 01.12.16.
 */
public class K_Means {
    private String[][] attributes; //only for discrete-valued attributes
    private double[][] trainingData;
    private double[][] centers;

    public K_Means(String[][] attributes, int[][] trainingData, int k) {
        this.attributes = attributes;
        this.trainingData = new double[trainingData.length][trainingData[0].length];
        for (int i = 0; i < this.trainingData.length; i++) {
            for (int j = 0; j < this.trainingData[0].length; j++) {
                this.trainingData[i][j] = trainingData[i][j];
            }
        }
        this.centers = new double[k][attributes.length];
        findRandomCenters();
        kmeans();
    }

    public K_Means(double[][] trainingData, int k) {
        this.trainingData = trainingData;
        double[] min = new double[trainingData[0].length];
        double[] max = new double[trainingData[0].length];

        for (int i = 0; i < trainingData[0].length; i++) {
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < trainingData.length; i++) {
            for (int j = 0; j < trainingData[0].length; j++) {
                if (trainingData[i][j] < min[j]) min[j] = trainingData[i][j];
                if (trainingData[i][j] > max[j]) max[j] = trainingData[i][j];
            }
        }
        this.centers = new double[k][trainingData[0].length];
        findRandomCenters(min, max);
        kmeans();
    }

    public static void main(String[] args) {
        /*LoadC4_5.CarData carData = new LoadC4_5.CarData();
        int k = 4;
        if (args.length > 0 && args[0].chars().allMatch(Character::isDigit)) {
            k = Integer.parseInt(args[0]);
        }
        K_Means k_means = new K_Means(carData.attributes, carData.trainingData, k);
        System.out.println(Arrays.deepToString(k_means.getCenterStats(carData.classes)));
        */
        int nCenters = 10;
        int nPPC = 500;
        int boundary = 1000;
        int sDeviation = 80;
        TestDataSet t = new TestDataSet(nCenters, nPPC, boundary, sDeviation);
        K_Means k_means = new K_Means(t.trainingData, nCenters);

        BufferedImage image = new BufferedImage(2 * (boundary + sDeviation), 2 * (boundary + sDeviation), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D grc = image.createGraphics();
        grc.setColor(Color.WHITE);
        for (int i = 0; i < t.trainingData.length; i++) {
            grc.fillOval((int) Math.round(t.trainingData[i][0] - 1) + boundary + sDeviation, (int) Math.round(t.trainingData[i][1] - 1) + boundary + sDeviation, 2, 2);
        }
        grc.setColor(Color.GREEN);
        for (int i = 0; i < t.centers.length; i++) {
            grc.fillOval(Math.round(t.centers[i][0] - 4) + boundary + sDeviation, Math.round(t.centers[i][1] - 4) + boundary + sDeviation, 8, 8);
        }
        grc.setColor(Color.RED);
        for (int i = 0; i < k_means.centers.length; i++) {
            grc.fillOval((int) Math.round(k_means.centers[i][0] - 4) + boundary + sDeviation, (int) Math.round(k_means.centers[i][1] - 4) + boundary + sDeviation, 8, 8);
        }
        try {
            ImageIO.write(image, "png", new File("out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void findRandomCenters(double[] min, double[] max) {
        Random rnd = new Random();
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers[0].length; j++) {
                centers[i][j] = rnd.nextDouble() * (max[j] - min[j]) + min[j];
            }
        }
    }

    private void kmeans() {
        double[][] _centers = findBetterCenters();
        System.out.println(Arrays.deepToString(_centers));
        while (centerDistance(centers, _centers) > 0.0000001d) {
            centers = _centers;
            _centers = findBetterCenters();
            System.out.println(Arrays.deepToString(_centers));
        }
    }

    public double[][] getCenters() {
        return centers;
    }

    public int[][] getCenterStats(String[] classes) { //only for discrete data with labels
        int[][] centerStats = new int[centers.length][classes.length];
        int[] centerAssignment = assignToCenters();
        for (int i = 0; i < centerAssignment.length; i++) {
            centerStats[centerAssignment[i]][(int) trainingData[i][attributes.length]]++;
        }
        return centerStats;
    }

    private double centerDistance(double[][] a, double[][] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result += Math.pow(a[i][j] - b[i][j], 2);
            }
        }
        return result;
    }

    private double[][] findBetterCenters() {
        double[][] _centers = centers.clone();
        int[] centerAssignment = assignToCenters();
        for (int i = 0; i < _centers.length; i++) {
            int countInstances = 0;
            double[] sumInstances = new double[centers[i].length];
            for (int j = 0; j < centerAssignment.length; j++) {
                if (centerAssignment[j] == i) {
                    countInstances++;
                    addVector(sumInstances, trainingData[j]);
                }
            }
            if (countInstances > 0) {
                multVector(sumInstances, 1 / (double) countInstances);
                _centers[i] = sumInstances;
            } else {
                System.out.println("There was a center without assigned instances.");
            }
        }
        return _centers;
    }

    private int[] assignToCenters() {
        int[] result = new int[trainingData.length];
        for (int i = 0; i < result.length; i++) {
            double min = Double.POSITIVE_INFINITY;
            int minIndex = 0;
            for (int j = 0; j < centers.length; j++) {
                double current = euclidianSquared(i, j);
                if (current < min) {
                    min = current;
                    minIndex = j;
                }
            }
            result[i] = minIndex;
        }
        return result;
    }

    private void multVector(double[] a, double b) {
        for (int i = 0; i < a.length; i++) {
            a[i] *= b;
        }
    }

    private void addVector(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
    }

    private double euclidianSquared(int instance, int center) {
        double result = 0;
        for (int i = 0; i < centers[center].length; i++) {
            result += Math.pow(centers[center][i] - trainingData[instance][i], 2);
        }
        return result;
    }

    private void findRandomCenters() {
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < attributes.length; j++) {
                centers[i][j] = Math.random() * attributes[j].length;
            }
        }
    }
}
