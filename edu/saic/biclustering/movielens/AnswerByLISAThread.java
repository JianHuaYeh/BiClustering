/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering.movielens;

import java.util.*;
import java.io.*;
//import com.capstone.lisa.bonacich.*;
/**
 *
 * @author jhyeh
 */
public class AnswerByLISAThread extends Thread {
    private HashMap index;
    private double threshold;
    private String line;
    private double degeneration;
    private static int DIM_LIMIT = 1000;
    private double sorpScore = 0.0;
    private int method;
    private VirtualSession vs;

    public class ASObject implements Comparable, java.io.Serializable {
        public String label;
        public double cosine;

        public ASObject(String s, double d) {
            this.label = s;
            this.cosine = d;
        }

        public int compareTo(Object obj) {
            ASObject other = (ASObject)obj;
            if (this.cosine > other.cosine) return -1;
            else if (this.cosine < other.cosine) return 1;
            else return 0;
        }
    }

    public AnswerByLISAThread(String line, HashMap index, double thr,
            double deg, int method) {
        this.line = line;
        this.index = index;
        this.threshold = thr;
        this.degeneration = deg;
        if (degeneration>=1.0 || degeneration<=0.0)
            this.degeneration = 0.95;   // default value
        this.method = method;
        this.vs = new VirtualSession();
    }

    public void run() {
        System.err.println("AnswerByLISAThread: process line ["+line+"]");
        StringTokenizer st = new StringTokenizer(line);
        String uid = st.nextToken().trim();
        String mid = st.nextToken().trim();
        String ans = st.nextToken().trim();
        double guess = -1.0;
        double th = this.threshold;
        while (guess < 0.0) {
            guess = this.calcAnswer(uid, mid, th);
            if (guess < 0.0) // degeneration needed
                th *= this.degeneration;
            if (th < 0.1) { // too many tries, not meaningful
                System.err.println("Too many tries for ["+line+
                        "], use SORP score("+this.sorpScore+") instead.");
                guess = this.sorpScore;
            }
        }
        //System.err.println(line.trim()+" "+guess+" "+this.sorpScore);
        //System.out.println(line.trim()+" "+guess+" "+this.sorpScore);
        System.out.println(uid+" "+mid+" "+ans+" "+guess+" ");
    }

    protected double calcWeightedAverage(Object[] objs, String bfname) {
        HashMap bpcHash = new HashMap();
        try {
            BufferedReader br = new BufferedReader(new FileReader(bfname));
            String line="";
            /*double sum = 0.0;
            while ((line=br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                st.nextToken();
                double r = Double.parseDouble(st.nextToken().trim());
                sum += r;
            }
            br.close();
            if (sum == 0.0) {
                System.err.println("All BPC entries are zero, return 0.0");
                return 0.0;
            }
            br = new BufferedReader(new FileReader(bfname));*/
            while ((line=br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                String id = st.nextToken().trim();
                //double r = Double.parseDouble(st.nextToken().trim())/sum;
                double r = Double.parseDouble(st.nextToken().trim());
                bpcHash.put(id, new Double(r));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return -1.0;
        }
        double result = 0.0;
        double weightsum = 0.0;
        //System.err.println("calcWeightedAverage: objs length="+objs.length);
        for (int i=0; i<objs.length; i++) {
            ASObject obj = (ASObject)objs[i];
            double weight = ((Double)bpcHash.get(""+i)).doubleValue();
            double score = obj.cosine;
            if (Double.isNaN(weight)) continue;
            result += weight*score;
            weightsum += weight;
        }
        //System.err.println("weightsum="+weightsum+
        //        ", accumulated weighted score="+result);
        if (weightsum == 0) return 0.0;
        double score=result/weightsum;
        if (score > 5.0) score=5.0;
        return score;
    }

    /*private void outputUserList(Object[] objs, String ufname) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(ufname));
            for (int i=0; i<objs.length; i++) {
                ASObject obj = (ASObject)objs[i];
                String uid = obj.label;
                pw.println(""+i+" "+uid);
            }
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    protected void outputAdjacencyMatrix(Object[] objs, String afname) {
        try {
            //System.err.println("Outputting adjacency matrix: file="+
            //        afname+", size="+objs.length);
            PrintWriter pw = new PrintWriter(new FileOutputStream(afname));
            for (int i=0; i<objs.length; i++) {
                ASObject obj1 = (ASObject)objs[i];
                double r1 = obj1.cosine;
                for (int j=i; j<objs.length; j++) {
                    if (i == j) continue;
                    ASObject obj2 = (ASObject)objs[j];
                    double r2 = obj2.cosine;
                    double edge = 0.0;
                    if ((r1!=0.0) || (r2!=0.0)) {
                        edge = r1/(r1+r2);
                        pw.println(""+i+" "+j+" "+edge);
                        edge = r2/(r1+r2);
                        pw.println(""+j+" "+i+" "+edge);
                    }
                }
                pw.flush();
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected double calcBPCAverage(Object[] objs, String uid, String mid) {
        // just in case
        this.sorpScore = 0.0;
        double sorpWeightSum = 0.0;
        double sorpSum = 0.0;

        ArrayList al = new ArrayList();
        boolean allzeros = true;
        for (int i=0; i<objs.length; i++) {
            ASObject obj = (ASObject)objs[i];
            String uid2 = obj.label;
            //VirtualSession vs = new VirtualSession(uid2);
            String fuid2 = uid2.substring(0, uid2.indexOf(".txt"));
            int score = this.vs.getScore(fuid2, mid);
            if (score > 0) {
                //System.err.println("vs.getScore("+fuid2+","+mid+")="+score);
                allzeros = false;
                obj.cosine = score;
                al.add(obj);
                sorpSum += obj.cosine*score;
                sorpWeightSum += obj.cosine;
            }
        }
        if (sorpWeightSum > 0.0) {
            this.sorpScore = sorpSum/sorpWeightSum;
            //System.err.println("sorpScore = "+this.sorpScore);
            return this.sorpScore;
        }
        return 0.0;

        // if all the ratings are 0, the result is certainly zero
        /*if (allzeros) return -1.0;
        
        // generate adjacency matrix
        Object[] objs2 = al.toArray();
        if (objs2.length == 0) {
            System.err.println("Not enough nodes for social network "+
                    "["+uid+"-"+mid+"], return.");
            return -1.0;
        }
        if (objs2.length > DIM_LIMIT) {
            Arrays.sort(objs2);
            Object[] objs3 = new Object[DIM_LIMIT];
            System.arraycopy(objs2, 0, objs3, 0, DIM_LIMIT);
            objs2 = objs3;
        }

        String afname = "adj."+uid+"-"+mid+".matrix";
        this.outputAdjacencyMatrix(objs2, afname);

        // calculate power centrality
        CalcBPC bpc = new CalcBPC(afname);
        //CalcBPC2 bpc = new CalcBPC2(afname);
        try {
            bpc.doCalc();
        } catch (Exception e) {
            System.err.println(e);
            return -1.0;
        }

        return this.calcWeightedAverage(objs2, bpc.getBPCFileName());*/
    }
    
    private double similarity(double[] vec0, double[] vec1, int method) {
        switch (method) {
            case 0: return edu.saic.biclustering.util.Similarity.eucledianSimilarity(vec0, vec1);
            case 1: return edu.saic.biclustering.util.Similarity.pearsonSimilarity(vec0, vec1);
            case 2:
            default: return edu.saic.biclustering.util.Similarity.cosineSimilarity(vec0, vec1);
        }
    }

    protected double calcAnswer(String uid, String mid, double th) {
        String fuid = uid+".txt";
        double[] vec1 = (double[])index.get(fuid);
        if (vec1 == null) {
            System.err.println("No topic similarity for uid = "+uid);
            return 0.0;
        }
        ArrayList al = new ArrayList();
        for (Iterator it=index.keySet().iterator(); it.hasNext(); ) {
            String uid2 = (String)it.next();
            if (uid2.equals(uid)) continue;
            double[] vec2 = (double[])index.get(uid2);
            if (vec2 == null) {
                System.err.println("No topic similarity for uid = "+uid2);
                continue;
            }
            //double d = cosine(vec1, vec2);
            //double d = comm.Similarity.cosineSimilarity(vec1, vec2);
            double d = similarity(vec1, vec2, this.method);
            
            if (Double.isNaN(d) || (d<th)) {
                //System.err.println("User "+uid2+" doesn't pass threshold, "+
                //        "similarity = "+d);
                continue;
            }
            //System.err.println("User "+uid2+" passes threshold, "+
            //        "similarity = "+d);
            ASObject as = new ASObject(uid2, d);
            al.add(as);
        }
        if (al.size() < 5) { // not enough nodes for social network
            System.err.println("Not enough similar users, threshold "+
                    "degenration is needed.");
            return -1.0;
        }
        Object[] array = al.toArray();
        //Arrays.sort(array);
        // pick up the significant group of users to
        // find the target mid(scan virtual session),
        // then calc the weight average
        System.err.println("Ready to calc weight avg for ["+uid+","+mid+
                "], array length="+array.length);
        return this.calcBPCAverage(array, uid, mid);
    }

}
