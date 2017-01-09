import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;


/**
 * Created by max on 08.01.17.
 */
public class Sheet9 {
    public static void main(String[] args) {
        Vector3D[] vs = new Vector3D[]{new Vector3D(1, 1, 1), new Vector3D(9, 1, 2),
                new Vector3D(4, 2, 1), new Vector3D(6, 5, 4), new Vector3D(3, 4, 3),
                new Vector3D(1, 4, 4)};
        Vector3D v = new Vector3D(20, 4, 4);
        double delta = 0.5;
        double[] weights = {1, 1, 1};
        for (int i = 0; i < 6; i++) {
            int n = getNearestNeighbour(vs, vs[i], weights);
            updateWeights(getClass(n) == getClass(i),weights,n,i,vs,delta);
            System.out.println(Arrays.toString(weights));
        }
        System.out.println(getNearestNeighbour(vs,v,weights));
    }

    static int getClass(int i) {
        return i / 3;
    }

    static double weightedDistance(Vector3D a, Vector3D b, double[] weights) {
        return Math.abs(a.getX() - b.getX()) * weights[0] +
                Math.abs(a.getY() - b.getY()) * weights[1] +
                Math.abs(a.getZ() - b.getZ()) * weights[2];
    }

    static void updateWeights(boolean correct, double[] weights, int a, int b, Vector3D[] vs, double delta) {
        double[] results = {Math.abs(vs[a].getX() - vs[b].getX()) * weights[0],
                Math.abs(vs[a].getY() - vs[b].getY()) * weights[1],
                Math.abs(vs[a].getZ() - vs[b].getZ()) * weights[2]};
        int min = 0;
        int max = 0;
        for (int i = 0; i < 3; i++) {
            if (results[i] < results[min]) {
                min = i;
            } else if (results[i] > results[max]) {
                max = i;
            }
        }
        weights[min]+=(correct?1:-1)*delta;
        weights[max]-=(correct?1:-1)*delta;
    }

    static int getNearestNeighbour(Vector3D[] vs, Vector3D v, double[] weights) {
        int min = 0;
        double minDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vs.length; i++) {
            if (vs[i] != v) {
                double dist = weightedDistance(vs[i], v, weights);
                if (dist < minDist) {
                    min = i;
                    minDist = dist;
                }
            }
        }
        return min;
    }
}
