/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering.util;

/**
 *
 * @author jhyeh
 */
public class Similarity {

    public static double eucledianSimilarity(double[] vec0, double[] vec1) {
        double sum = 0.0;
        if (vec0.length != vec1.length) return 0.0;
        for (int i=0; i<vec0.length; i++) {
            double score = vec0[i];
            double score2 = vec1[i];
            if ((score>0) && (score2>0))
                sum += (score-score2)*(score-score2);
        }
        double d = Math.sqrt(sum);
        return 1.0/(d+1);
    }

    public static double pearsonSimilarity(double[] vec0, double[] vec1) {
        double sum1=0.0, sum2=0.0;
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        if (vec0.length != vec1.length) return 0.0;
        int n = vec0.length;
        int count = 0;
        for (int i=0; i<n; i++) {
            double r1 = vec0[i];
            double r2 = vec1[i];
            if ((r1>0) && (r2>0)) {
                sum1 += r1;
                sum2 += r2;
                sum1sq += r1*r1;
                sum2sq += r2*r2;
                psum += r1*r2;
                count++;
            }
        }
        double num = (count==0)?0:(psum-(sum1*sum2)/count);
        double d = (count==0)?0:((sum1sq-sum1*sum1/count)*(sum2sq-sum2*sum2/count));
        double den=(d<0.0)?0.0:Math.sqrt(d);
        if (den == 0.0) return 0.0;
        double result = num/den;
        result = (result<0.0)?0.0:result;
        return result;
    }

    public static double cosineSimilarity(double[] vec0, double[] vec1) {
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        if (vec0.length != vec1.length) return 0.0;
        int n = vec0.length;
        for (int i=0; i<n; i++) {
            double r1 = vec0[i];
            double r2 = vec1[i];
            if ((r1>0) && (r2>0)) {
                sum1sq += r1*r1;
                sum2sq += r2*r2;
                psum += r1*r2;
            }
        }
        double den=Math.sqrt(sum1sq)*Math.sqrt(sum2sq);
        if (den == 0.0) return 0.0;
        return psum/den;
    }
}
