import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by max on 18.11.16.
 */
public class DecisionTree {
    private String[] classes;
    private String[][] attributes;
    private Node root;
    private int[][] trainingData;

    abstract class Node {
        abstract int getClass_(int[] attributes);

        ArrayList<Integer> chosenOnes;
        double entropyOfChosen;
    }

    private class LeafNode extends Node {
        int my_class;

        int getClass_(int[] attributes) {
            return my_class;
        }

        LeafNode(int c, ArrayList<Integer> chosenOnes) {
            this.my_class = c;
            this.chosenOnes = chosenOnes;
            this.entropyOfChosen = getEntropy(chosenOnes);
        }
    }

    private class InternalNode extends Node {
        int my_attribute;
        Node[] children;

        int getClass_(int[] attributes) {
            return children[attributes[my_attribute]].getClass_(attributes);
        }

        InternalNode(int attr, ArrayList<Integer> chosenOnes) {
            this.my_attribute = attr;
            this.chosenOnes = chosenOnes;
            this.entropyOfChosen = getEntropy(chosenOnes);
            this.children = new Node[attributes[my_attribute].length];
        }
    }

    private static void loadLine(String line, String[] classes, String[][] attributes, int[][] trainingData, int l) {
        String[] keys = line.split(",");
        //System.out.println(Arrays.toString(keys));
        assert keys.length == attributes.length + 1;
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

    private double getEntropy(ArrayList<Integer> chosenOnes) {
        int[] c_classes = new int[classes.length];
        double result = 0;
        for (int i = 0; i < chosenOnes.size(); i++) {
            c_classes[trainingData[chosenOnes.get(i)][trainingData[i].length - 1]]++;
        }
        for (int i : c_classes) {
            double a = i / (double) chosenOnes.size();
            if (a != 0) {
                result -= a * Math.log(a) / Math.log(classes.length);
            }
        }
        return result;
    }

    private double getInformationGain(ArrayList<Integer> chosenOnes, int attribute, double entropyOfChosen) {
        double result = entropyOfChosen;
        ArrayList<ArrayList<Integer>> chosenbyAttribute = seperateByAttribute(chosenOnes, attribute);
        for (ArrayList<Integer> chosen : chosenbyAttribute) {
            result -= getEntropy(chosen) * chosen.size() / chosenOnes.size();
        }
        return result;
    }

    private ArrayList<ArrayList<Integer>> seperateByAttribute(ArrayList<Integer> chosenOnes, int attribute) {
        ArrayList<ArrayList<Integer>> chosenbyAttribute = new ArrayList<>();
        for (int i = 0; i < attributes[attribute].length; i++) {
            chosenbyAttribute.add(new ArrayList<>());
        }
        for (Integer chosenOne : chosenOnes) {
            chosenbyAttribute.get(trainingData[chosenOne][attribute]).add(chosenOne);
        }
        return chosenbyAttribute;
    }

    private Node getNodeWithMostGain(ArrayList<Integer> chosenOnes, double entropyOfChosen) {
        if (entropyOfChosen == 0) {
            assert trainingData.length > 0;
            return new LeafNode(trainingData[chosenOnes.get(0)][classes.length], chosenOnes);
        } else {
            int max = 0;
            double maxGain = 0;
            for (int i = 0; i < attributes.length; i++) {
                double gain = getInformationGain(chosenOnes, i, entropyOfChosen);
                if (gain > maxGain) {
                    max = i;
                    maxGain = gain;
                }
            }
            return new InternalNode(max, chosenOnes);
        }
    }

    public void ID3() {
        ArrayList<Integer> chosen = new ArrayList<>(trainingData.length);
        for (int i = 0; i < trainingData.length; i++) {
            chosen.add(i);
        }
        root = getNodeWithMostGain(chosen, getEntropy(chosen));

        ArrayDeque<Node> workingQueue = new ArrayDeque<>();
        workingQueue.add(root);
        while (!workingQueue.isEmpty()) {
            if (workingQueue.peek().getClass() != (LeafNode.class)) {
                InternalNode current = (InternalNode) workingQueue.poll();
                for (int i = 0; i < attributes[current.my_attribute].length; i++) {
                    ArrayList<ArrayList<Integer>> chosenbyAttribute = seperateByAttribute(current.chosenOnes, current.my_attribute);
                    current.children[i] = getNodeWithMostGain(chosenbyAttribute.get(i), getEntropy(chosenbyAttribute.get(i)));
                    workingQueue.add(current.children[i]);
                    System.out.println("added Node " + current.children[i]);
                }
            } else {
                workingQueue.poll();
            }
        }
    }

    public DecisionTree(String[] classes, String[][] attributes, int[][] trainingData) {
        this.classes = classes;
        this.attributes = attributes;
        this.trainingData = trainingData;
    }

    public static void main(String[] args) {
        String[] classes = new String[]{
                "unacc", "acc", "good", "vgood"
        };
        String[][] attributes = new String[][]{
                {"vhigh", "high", "med", "low"},
                {"vhigh", "high", "med", "low"},
                {"2", "3", "4", "5more"},
                {"2", "4", "more"},
                {"small", "med", "big"},
                {"low", "med", "high"}
        };
        int[][] trainingData = new int[1728][attributes.length + 1];
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
        DecisionTree d = new DecisionTree(classes, attributes, trainingData);
        d.ID3();
    }
}
