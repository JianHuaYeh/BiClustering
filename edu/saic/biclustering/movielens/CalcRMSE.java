/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering.movielens;

import java.io.*;
import java.util.*;
/**
 *
 * @author jhyeh
 */
public class CalcRMSE {
    private String qafile;

    public static void main(String[] args) {
        CalcRMSE rmse = new CalcRMSE(args[0]);
        rmse.doCalc();
    }

    public CalcRMSE(String str) {
        this.qafile = str;
    }

    public void doCalc() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(qafile));
            String line="";
            double sum=0.0, sum2=0.0;
            int count=0;
            int miss=0, miss2=0;
            // line format: uid, mid, anwer, guess
            while ((line=br.readLine()) != null) {
                //if (count%100==0) System.out.println("Question "+count);
                StringTokenizer st = new StringTokenizer(line);
                String uid = st.nextToken().trim();
                String mid = st.nextToken().trim();
                double answer = Double.parseDouble(st.nextToken().trim());
                double lisa = Double.parseDouble(st.nextToken().trim());
                if (answer != 0) {
                	sum += (lisa-answer)*(lisa-answer);
                    sum2 += (1.0-lisa/5.0)*(1.0-lisa/5.0);
                }
                count++;
            }
            br.close();
            //System.out.println("Question count = "+count);
            //System.out.println("LISA prediction stat:");
            //System.out.println("Error sum square = "+sum);
            double rmse = Math.sqrt(sum/count);
            //System.out.println("RMSE = "+rmse);
            System.out.println(rmse);
            rmse = Math.sqrt(sum2/count);
            System.out.println(rmse+"(KDD RMSE formula)");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
