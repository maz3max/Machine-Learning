How to Run:
    The program is written in Java 8 and requires a recent JRE to run.
    Place the "P3.jar" file and the "car.data" file in one folder and execute "java -jar P3.jar" in a Terminal.
    The result will be (over-)written to "car_tree.xml".
Discussion:
    The result is a decision tree, which perfectly classifies the training examples.
    That means, the method used (ID3) is likely to overfit the data.
    It is possible to cross-check while using ID3, but in this case,
    the instances completely cover the attribute space.
    Therefore, we don't have to bother with overfitting.