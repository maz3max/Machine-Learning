import java.util.Arrays;
import java.util.Random;

/**
 * Created by max on 03.12.16.
 */
public class TestDataSet {
    double[][] trainingData;
    int[][] centers;

    public TestDataSet(int nCenters, int nPointsPerCenter, int boundary, int sDeviation) {
        Random r = new Random();
        int k = 0;
        trainingData = new double[nCenters * nPointsPerCenter][2];
        centers = new int[nCenters][2];
        for (int i = 0; i < nCenters; i++) {
            int x = r.nextInt(2 * boundary) - boundary;
            int y = r.nextInt(2 * boundary) - boundary;
            centers[i][0] = x;
            centers[i][1] = y;
            for (int j = 0; j < nPointsPerCenter; j++) {
                trainingData[k][0] = r.nextGaussian() * sDeviation + x;
                trainingData[k][1] = r.nextGaussian() * sDeviation + y;
                k++;
            }
        }
        System.out.println("Test Data Centers are at: " + Arrays.deepToString(centers));
    }
}
