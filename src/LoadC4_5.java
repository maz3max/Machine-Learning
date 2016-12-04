import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by max on 01.12.16.
 */
public class LoadC4_5 {

    /**
     * loads a line from the .data file to an entry of the trainingData array
     *
     * @param line         input string
     * @param classes      classes for string comparison
     * @param attributes   attributes for string comparison
     * @param trainingData array to save training examples
     * @param l            current line number
     */
    private static void loadLine(String line, String[] classes, String[][] attributes, int[][] trainingData, int l) {
        String[] keys = line.split(",");
        if (keys.length == attributes.length + 1) {
            for (int i = 0; i < attributes.length; i++) {
                for (int j = 0; j < attributes[i].length; j++) {
                    if (keys[i].equals(attributes[i][j])) {
                        trainingData[l][i] = j;
                        break;
                    }
                }
            }
            for (int i = 0; i < classes.length; i++) {
                if (keys[keys.length - 1].equals(classes[i])) {
                    trainingData[l][keys.length - 1] = i;
                    break;
                }
            }
        }
    }

    static class CarData {
        String[] classes;
        String[][] attributes;
        String[] attributeNames;
        int[][] trainingData;

        public CarData() {
            //load the car_data examples
            classes = new String[]{
                    "unacc", "acc", "good", "vgood"
            };
            attributes = new String[][]{
                    {"vhigh", "high", "med", "low"},
                    {"vhigh", "high", "med", "low"},
                    {"2", "3", "4", "5more"},
                    {"2", "4", "more"},
                    {"small", "med", "big"},
                    {"low", "med", "high"}
            };
            attributeNames = new String[]{"buying", "maint", "doors", "persons", "lug_boot", "safety"};
            trainingData = new int[1728][attributes.length + 1];
            System.out.println("Loading Training Data...");
            try {
                try (BufferedReader br = new BufferedReader(new FileReader("car.data"))) {
                    int i = 0;
                    String line = br.readLine();
                    while (line != null) {
                        loadLine(line, classes, attributes, trainingData, i++);
                        line = br.readLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
