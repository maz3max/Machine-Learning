import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Stack;

/**
 * This class tries to build a decision tree out of training examples.
 * It is used in it's main method to solve the P.3 programming assignment for the Machine Learning module.
 * <p>
 * The most important methods are:
 * public void ID3()
 * private double getEntropy(*)
 * private double getInformationGain(*)
 * private Node getNodeWithMostGain(*)
 * <p>
 * written by Maximilian Deubel
 */
public class DecisionTree {
    private String[] classes;
    private String[][] attributes;
    private String[] attributeNames;
    private Node root;
    private int[][] trainingData;

    /**
     * initializes a new decision tree
     *
     * @param classes        classes to assign to the input
     * @param attributes     attributes to decide on
     * @param trainingData   training examples for building the tree
     * @param attributeNames names of the attributes
     */
    public DecisionTree(String[] classes, String[][] attributes, int[][] trainingData, String[] attributeNames) {
        this.classes = classes;
        this.attributes = attributes;
        this.trainingData = trainingData;
        this.attributeNames = attributeNames;
    }

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

    /**
     * entry point
     * contains all the specific input related to the assignment
     *
     * @param args not used
     */
    public static void main(String[] args) {
        //load the car_data examples
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
        String[] attributeNames = {"buying", "maint", "doors", "persons", "lug_boot", "safety"};
        int[][] trainingData = new int[1728][attributes.length + 1];
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
        System.out.println("Creating Decision Tree via ID3...");
        DecisionTree d = new DecisionTree(classes, attributes, trainingData, attributeNames); //create new decision tree with car_data
        d.ID3(); //run ID3 algorithm to build up the tree
        System.out.println("Writing XML File...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("car_tree.xml"))) {
            writer.write(d.toXML());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Checking Test Examples...");
        for (int[] aTrainingData : trainingData) {
            if (d.decide(aTrainingData) != aTrainingData[attributes.length])
                System.out.println("failed");
        }
    }

    /**
     * uses the decision tree to decide which class fits the attributes best
     *
     * @param attributes attributes to check
     * @return class
     */
    public int decide(int[] attributes) {
        return root.getClass_(attributes);
    }

    /**
     * @param chosenOnes set of training examples (indexes) to reach this node
     * @return entropy of the given set
     */
    private double getEntropy(ArrayList<Integer> chosenOnes) {
        int[] classDistribution = new int[classes.length];
        for (int i = 0; i < chosenOnes.size(); i++) {
            classDistribution[trainingData[chosenOnes.get(i)][trainingData[i].length - 1]]++;
        }
        return getEntropy(classDistribution, chosenOnes.size());
    }

    /**
     * @param classDistribution array of sums of instances by class
     * @param size              total instances
     * @return entropy of the given set
     */
    private double getEntropy(int[] classDistribution, int size) {
        double result = 0;
        for (int i : classDistribution) {
            double a = i / (double) size;
            if (a != 0) {
                result -= a * Math.log(a) / Math.log(classes.length);
            }
        }
        return result;
    }

    /**
     * @param chosenOnes      set of training examples (indexes)
     * @param attribute       attribute, on which the information gain is calculated
     * @param entropyOfChosen entropy of chosenOnes
     * @return information gain of the given training examples with respect of a particular attribute
     */
    private double getInformationGain(ArrayList<Integer> chosenOnes, int attribute, double entropyOfChosen) {
        double result = entropyOfChosen;
        ArrayList<ArrayList<Integer>> chosenbyAttribute = seperateByAttribute(chosenOnes, attribute);
        for (ArrayList<Integer> chosen : chosenbyAttribute) {
            result -= getEntropy(chosen) * chosen.size() / chosenOnes.size();
        }
        return result;
    }

    /**
     * @param chosenOnes set of training examples (indexes)
     * @param attribute  attribute to seperate the examples on
     * @return an ArrayList of the separated sets according to the value of the given attribute
     */
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

    /**
     * @param chosenOnes      set of training examples (indexes)
     * @param entropyOfChosen entropy of chosenOnes
     * @param parent          parent node
     * @return a new node that parts the given training examples best according to information gain
     */
    private Node getNodeWithMostGain(ArrayList<Integer> chosenOnes, double entropyOfChosen, InternalNode parent) {
        if (entropyOfChosen == 0) { //that means the given set is perfectly classified
            assert trainingData.length > 0;
            return new LeafNode(trainingData[chosenOnes.get(0)][attributes.length], chosenOnes, parent);
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
            return new InternalNode(max, chosenOnes, parent);
        }
    }

    /**
     * implementation of the "ID3 Top-Down Induction" algorithm described in the lecture notes
     */
    public void ID3() {
        //root node gets initialized
        ArrayList<Integer> chosen = new ArrayList<>(trainingData.length);
        for (int i = 0; i < trainingData.length; i++) {
            chosen.add(i);
        }
        root = getNodeWithMostGain(chosen, getEntropy(chosen), null);

        ArrayDeque<Node> workingQueue = new ArrayDeque<>(); //queue to hold current leaves
        workingQueue.add(root);
        while (!workingQueue.isEmpty()) {
            if (workingQueue.peek().getClass() != (LeafNode.class)) { //if we need to generate children for the current node
                InternalNode current = (InternalNode) workingQueue.poll();
                for (int i = 0; i < attributes[current.my_attribute].length; i++) {
                    ArrayList<ArrayList<Integer>> chosenbyAttribute = seperateByAttribute(current.chosenOnes, current.my_attribute);
                    //create descendants for each value with decision attributes to maximise information gain
                    current.children[i] = getNodeWithMostGain(chosenbyAttribute.get(i), getEntropy(chosenbyAttribute.get(i)), current);
                    workingQueue.add(current.children[i]);
                    //System.out.println("added Node " + current.children[i]);
                }
            } else { //skip finished leaf nodes
                workingQueue.poll();
            }
        }
    }

    /**
     * converts decision tree to XML string
     *
     * @return XML string
     */
    public String toXML() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");

        sb.append(nodeToXML(root, 0, 0)).append("\n"); //root node gets some special handling
        if (root.getClass() == InternalNode.class) { //if the whole tree is just a leaf, we don't need a stack and tab handling at all
            int numtabs = 0;
            Stack<Node> working_stack = new Stack<>(); //stack to hold the leaf nodes (and closing tokens)
            Stack<Integer> attrValStack = new Stack<>(); //stack to hold the attribute values of the nodes
            InternalNode rootInternal = (InternalNode) root;

            for (int i = 0; i < rootInternal.children.length; i++) {
                working_stack.push(rootInternal.children[i]);
                attrValStack.push(i);
            }
            numtabs++; //one level up
            while (!working_stack.isEmpty()) {
                if (working_stack.peek() == null) { //here, we use null as a closing token
                    working_stack.pop();
                    numtabs--; //one level up
                    for (int i = 0; i < numtabs; i++) { //tab handling
                        sb.append("\t");
                    }
                    sb.append("</node>\n"); //insert actual closing tag
                } else if (working_stack.peek().getClass() == InternalNode.class) { //internal nodes have children to consider
                    InternalNode c = (InternalNode) working_stack.pop();
                    sb.append(nodeToXML(c, attrValStack.pop(), numtabs)).append("\n");
                    numtabs++; //one level down
                    working_stack.push(null); //push closing token
                    for (int i = 0; i < c.children.length; i++) {
                        working_stack.push(c.children[i]);
                        attrValStack.push(i);
                    }
                } else { //leaf nodes have to be printed along with their class names
                    LeafNode c = (LeafNode) working_stack.pop();
                    sb.append(nodeToXML(c, attrValStack.pop(), numtabs)).append(classes[c.my_class]).append("</node>\n");
                }
            }
        }
        sb.append("</tree>"); //closing the root node
        return sb.toString();
    }

    /**
     * XML creation helper method
     *
     * @param n          node to convert
     * @param attr_value value of attribute of parent node to get to this node
     * @param numTabs    number of tabs to append in front
     * @return part of the XML string
     */
    private String nodeToXML(Node n, int attr_value, int numTabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numTabs; i++) {
            sb.append("\t"); //place tabs where necessary
        }
        if (n.parent == null) {
            sb.append("<tree");
        } else {
            sb.append("<node");
        }
        //build classes part of the string
        sb.append(" classes=\"");
        ArrayList<String> classStrings = new ArrayList<>();
        for (int i = 0; i < this.classes.length; i++) {
            if (n.classDistribution[i] > 0) {
                classStrings.add(classes[i] + ":" + n.classDistribution[i]);
            }
        }
        //comma handling
        sb.append(classStrings.get(0));
        for (int i = 1; i < classStrings.size(); i++) {
            sb.append(",");
            sb.append(classStrings.get(i));
        }
        //entropy part of the string
        sb.append("\" entropy=\"").append(n.entropyOfChosen).append("\"");
        if (n.parent != null) { //if n is not the root node, print attribute value
            sb.append(' ').append(attributeNames[n.parent.my_attribute]).append("=\"").append(attributes[n.parent.my_attribute][attr_value]).append("\"");
        }
        sb.append(">");
        return sb.toString();
    }

    /**
     * basic node class for building the decision tree
     */
    abstract class Node {
        InternalNode parent;
        ArrayList<Integer> chosenOnes;
        int[] classDistribution;
        double entropyOfChosen;

        /**
         * basic initialization for nodes
         *
         * @param parent     parent node
         * @param chosenOnes set of training examples (indexes) to reach this node
         */
        Node(InternalNode parent, ArrayList<Integer> chosenOnes) {
            this.parent = parent;
            this.chosenOnes = chosenOnes;
            this.classDistribution = new int[classes.length];
            for (int i = 0; i < chosenOnes.size(); i++) {
                classDistribution[trainingData[chosenOnes.get(i)][trainingData[i].length - 1]]++;
            }
            this.entropyOfChosen = getEntropy(classDistribution, chosenOnes.size());
        }

        abstract int getClass_(int[] attributes);
    }

    /**
     * leaf node class for building the decision tree
     */
    private class LeafNode extends Node {
        int my_class;

        /**
         * initializes a leaf of the decision tree
         *
         * @param c          class, which becomes the output, whenever this node is reached
         * @param chosenOnes set of training examples (indexes) to reach this node
         * @param parent     parent node
         */
        LeafNode(int c, ArrayList<Integer> chosenOnes, InternalNode parent) {
            super(parent, chosenOnes);
            this.my_class = c;
        }

        /**
         * @param attributes not used here
         * @return class of the leaf
         */
        @Override
        int getClass_(int[] attributes) {
            return my_class;
        }
    }

    /**
     * internal node class for building the decision tree
     */
    private class InternalNode extends Node {
        int my_attribute;
        Node[] children;

        /**
         * @param attr       attribute this node uses to decide
         * @param chosenOnes set of training examples (indexes) to reach this node
         * @param parent     parent node
         */
        InternalNode(int attr, ArrayList<Integer> chosenOnes, InternalNode parent) {
            super(parent, chosenOnes);
            this.my_attribute = attr;
            this.children = new Node[attributes[my_attribute].length];
        }

        /**
         * chooses the right child with respect to the attribute of the node
         *
         * @param attributes array of the attributes to decide on
         * @return class the tree assigns to the particular array of attributes
         */
        @Override
        int getClass_(int[] attributes) {
            return children[attributes[my_attribute]].getClass_(attributes);
        }
    }
}
