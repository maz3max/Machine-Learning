import java.util.Arrays;

/**
 * Created by max on 16.01.17.
 */
public class NaiveBayes {
    String[] classes;
    String[][] attributes;
    String[] attributeNames;
    int[][] trainingData;

    public NaiveBayes(String[] classes, String[][] attributes, String[] attributeNames, int[][] trainingData) {
        this.classes = classes;
        this.attributes = attributes;
        this.attributeNames = attributeNames;
        this.trainingData = trainingData;
    }

    public double[] checkAttributes(int[] ex, double weight) {
        double[] result = new double[classes.length];
        int[] countAttr = new int[attributes.length]; //a=ai
        int[] countClass = new int[classes.length]; //v=vj
        int[][] countMatchPerClass = new int[attributes.length][classes.length]; //v=vj & a=ai
        //do the necessary counting
        for (int i = 0; i < trainingData.length; i++) {
            for (int j = 0; j < attributes.length; j++) {
                if (ex[j] == trainingData[i][j]) {
                    countAttr[j]++;
                    countMatchPerClass[j][trainingData[i][attributes.length]]++;
                    break;
                }
            }
            countClass[trainingData[i][attributes.length]]++;
        }
        //multiply p(vi)*product(p(ai|vj))
        for (int i = 0; i < result.length; i++) {
            result[i] = countClass[i] / (double) trainingData.length;
            for (int j = 0; j < attributes.length; j++) {
                result[i] *= (countMatchPerClass[j][i] + weight / (double) attributes[j].length) / (double) countClass[i] + weight;
            }
        }
        //normalize to a-posteriori probability
        double sum = Arrays.stream(result).sum();
        for (int i = 0; i < result.length; i++) {
            result[i] *= 1 / (double) sum;
        }
        return result;
    }

    public int[][] confusionMatrix() {
        int[][] confusionMatrix = new int[classes.length][classes.length];
        for (int[] sample : trainingData) {
            double[] estimate = checkAttributes(sample, 0);
            double max = 0;
            int maxI = 0;
            for (int i = 0; i < estimate.length; i++) {
                if (estimate[i] > max) {
                    maxI = i;
                    max = estimate[i];
                }
            }
            confusionMatrix[sample[attributes.length]][maxI]++;
        }
        return confusionMatrix;
    }

    public static void main(String[] args) {
        LoadC4_5.CarData carData = new LoadC4_5.CarData();
        NaiveBayes naiveBayes = new NaiveBayes(carData.classes, carData.attributes, carData.attributeNames, carData.trainingData);
        int[][] confusionMatrix = naiveBayes.confusionMatrix();
        System.out.println("rows: real class, columns: estimated class");
        for (int i = 0; i < carData.classes.length; i++) {
            System.out.println(Arrays.toString(confusionMatrix[i]));
        }
    }
}
