import java.util.Arrays;


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
    /**
     * Entry point
     *
     * @param args one integer to specify k
     */
    public static void main(String[] args) {
        LoadC4_5.CarData carData = new LoadC4_5.CarData();
        int k = 4;
        if (args.length > 0 && args[0].chars().allMatch(Character::isDigit)) {
            k = Integer.parseInt(args[0]);
        }
        K_Means k_means = new K_Means(carData.attributes, carData.trainingData, k);
        System.out.println(Arrays.deepToString(k_means.getCenterStats(carData.classes)));
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
                double current = euclideanSquared(centers[j], trainingData[i]);
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
