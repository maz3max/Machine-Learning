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

    /**
     * Constructor which can use discrete-valued input and apply k-means to it.
     *
     * @param attributes   only used for the count of the attributes and their domains
     * @param trainingData training data
     * @param k            to specify the k in k-means
     */
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
        System.out.println("Starting with following centers: ");
        System.out.println(Arrays.deepToString(centers));
        k_means();
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
        k_means();
    }

    /**
     * Entry point
     *
     * @param args one integer to specify k
     */
    public static void main(String[] args) {
        int nCenters = 5;
        int nPPC = 10000;
        int boundary = 500;
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

    /**
     * Implementation of k-means algorithm.
     * uses 10^-7 as epsilon value
     * realigns centers until they don't change anymore
     */
    private void k_means() {
        double[][] _centers = findBetterCenters();
        System.out.println(Arrays.deepToString(_centers));
        while (centerDistance(centers, _centers) > 0.0000001d) {
            centers = _centers;
            _centers = findBetterCenters();
            System.out.println(Arrays.deepToString(_centers));
        }
    }

    /**
     * only for discrete data with labels
     *
     * @param classes names of the labels
     * @return the count of data points per label and center
     */
    public int[][] getCenterStats(String[] classes) {
        int[][] centerStats = new int[centers.length][classes.length];
        int[] centerAssignment = assignToCenters();
        for (int i = 0; i < centerAssignment.length; i++) {
            centerStats[centerAssignment[i]][(int) trainingData[i][attributes.length]]++;
        }
        return centerStats;
    }

    /**
     * @param a first center array
     * @param b second center array
     * @return the squared euclidean distance between two center arrays
     */
    private double centerDistance(double[][] a, double[][] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += euclideanSquared(a[i], b[i]);
        }
        return result;
    }

    /**
     * @param v1 vector
     * @param v2 vector
     * @return the squared euclidean distance between vector v1 and v2
     */
    private double euclideanSquared(double[] v1, double[] v2) {
        double result = 0;
        for (int i = 0; i < v1.length; i++) {
            result += Math.pow(v1[i] - v2[i], 2);
        }
        return result;
    }

    /**
     * realigns current centers to the mean of their assigned points
     *
     * @return realigned centers
     */
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
                //that usually means that the initial centers were bad
            }
        }
        return _centers;
    }

    /**
     * assigns data points to their closest center
     *
     * @return array of assigned center indices
     */
    private int[] assignToCenters() {
        int[] result = new int[trainingData.length];
        for (int i = 0; i < result.length; i++) {
            double min = Double.POSITIVE_INFINITY;
            int minIndex = 0;
            for (int j = 0; j < centers.length; j++) {
                double current = euclideanSquared(trainingData[i], centers[j]);
                if (current < min) {
                    min = current;
                    minIndex = j;
                }
            }
            result[i] = minIndex;
        }
        return result;
    }

    /**
     * scales vector a with b
     *
     * @param a vector
     * @param b scale
     */
    private void multVector(double[] a, double b) {
        for (int i = 0; i < a.length; i++) {
            a[i] *= b;
        }
    }

    /**
     * adds vector b to a
     *
     * @param a vector
     * @param b vector
     */
    private void addVector(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
    }

    /**
     * sets current centers to randomly chosen values
     */
    private void findRandomCenters() {
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers[i].length; j++) {
                centers[i][j] = Math.random() * attributes[j].length;
            }
        }
    }
}
