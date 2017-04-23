package edu.saic.biclustering.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.saic.biclustering.BiCluster;
import edu.saic.biclustering.BiClusteringGA;
import edu.saic.biclustering.DataMatrix;
import edu.saic.biclustering.util.Similarity;

public class ExprMovieLens {
	private String trname;
	private String tsname;
	private DataMatrix data;
	private int method;
	private HashSet<Integer>[] recomm;
	
	private boolean DEBUG = true;
	
	public static void main(String[] args) {
		String trfname = "u1.base";
		String tsfname = "u1.test";
		
		ExprMovieLens expr = new ExprMovieLens(trfname, tsfname);
		expr.go();
	}
	
	public ExprMovieLens(String tr, String ts, int m) {
		this.trname = tr;
		this.tsname = ts;
		this.method = m;
		this.data = loadData(tr);
		if (this.data == null) {
			System.err.println("Null data, stop.");
			System.exit(-1);
		}
		if (DEBUG) System.err.println("Data loaded.");
	}
	
	public ExprMovieLens(String tr, String ts) {
		this(tr, ts, 0);
	}
	
	public DataMatrix loadData(String fname) {
		// count rows and columns first
		int maxr = 0;
		int maxc = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line = "";
			while ((line=br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				int r = Integer.parseInt(st.nextToken());
				int c = Integer.parseInt(st.nextToken());
				maxr = (r>maxr)?r:maxr;
				maxc = (c>maxc)?c:maxc;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		if (DEBUG) System.err.println("Data matrix size: "+maxr+"x"+maxc);
		double[][] result = new double[maxr][maxc];
		HashSet<Integer> rset = new HashSet<Integer>();
		HashSet<Integer> cset = new HashSet<Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line = "";
			while ((line=br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				int uid = Integer.parseInt(st.nextToken())-1;
				int mid = Integer.parseInt(st.nextToken())-1;
				rset.add(uid);
				cset.add(mid);
				int v = Integer.parseInt(st.nextToken());
				result[uid][mid] = v;
			}
			br.close();
			this.recomm = new HashSet[2];
			this.recomm[0] = rset;
			this.recomm[1] = cset;
			DataMatrix dm = new DataMatrix(result);
			return dm;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public void saveModel(ArrayList<int[]> sols) {
		String fname = trname+".model";
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(fname));
			for (int[] sol: sols) {
				String line="";
				for (int v: sol) {
					line += v+" ";
				}
				pw.println(line.trim());
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void go() {
		// training model creation
		if (DEBUG) System.err.println("Start training phase.");
		BiClusteringGA ga = new BiClusteringGA(this.data);
		try {
			if (DEBUG) System.err.println("Starting GA...");
			ArrayList<int[]> sols = ga.randomRestartGA(this.recomm);
			this.saveModel(sols);
			if (DEBUG) System.err.println("GA finished.");
		
			// now test it with RMSE calculation
			if (DEBUG) System.err.println("Start test phase.");
			BufferedReader br = new BufferedReader(new FileReader(this.tsname));
			String line = "";
			double sum = 0.0;
			double sum2 = 0.0; // for baseline, all 1
			double sum3 = 0.0; // for baseline, all 5
			int count=0;
			while ((line=br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				int uid = Integer.parseInt(st.nextToken())-1;
				int mid = Integer.parseInt(st.nextToken())-1;
				int a = Integer.parseInt(st.nextToken());
				double g = doGuess(this.data, sols, uid, mid);
				sum += (g-a)*(g-a);
				sum2 += (1.0-a)*(1.0-a);
				sum3 += (5.0-a)*(5.0-a);
				count++;
			}
			br.close();
			double rmse = (count!=0)?Math.sqrt(sum/count):-1.0;
			double rmse2 = (count!=0)?Math.sqrt(sum2/count):-1.0;
			double rmse3 = (count!=0)?Math.sqrt(sum3/count):-1.0;
            System.out.println("RMSE="+rmse+", baseline(1)="+rmse2+", baseline(5)="+rmse3);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public double doGuess(DataMatrix dm, ArrayList<int[]> sols, int r, int c) {
		double sum=0.0;
		double weight=0.0;
		for (int[] sol: sols) {
			try {
				double[] result = calcWeightedAverage(dm, sol, r, c);
				sum += result[0]*result[1];
				weight += result[1];
			} catch (Exception e) {	}
		}
		double guess = (weight==0.0)?0.0:sum/weight;
		return guess;
	}
	
	public double[] calcWeightedAverage(DataMatrix dm, int[] sol, int r, int c) throws Exception {
		int ordinal=0;
		for (int i=0; i<r; i++) if (sol[i] == 1) ordinal++;
		int[][] split = splitAssignment(sol);
    	int[] ra = split[0];
    	int[] ca = split[1];
		BiCluster bc = dm.getBiCluster(ra, ca);
		int rows = bc.getNumRows();
		double[] selfrow = bc.getRowCopy(ordinal);
		
		double sum=0.0;
		double weight=0.0;
		for (int i=0; i<rows; i++) {
			if (i==ordinal) continue; // skip self
			double rate = dm.getDataCell(r, c);
			if (rate > 0) { // user r has watched movie c
				double[] row = bc.getRowCopy(i);
				double sim = similarity(selfrow, row, this.method);
				sum += rate*sim;
				weight += sim;
			}
		}
		sum /= weight;
		double[] result = new double[]{sum, weight};
		return result;
	}
	
	private double similarity(double[] vec0, double[] vec1, int method) {
        switch (method) {
            case 0: return Similarity.eucledianSimilarity(vec0, vec1);
            case 1: return Similarity.pearsonSimilarity(vec0, vec1);
            case 2:
            default: return Similarity.cosineSimilarity(vec0, vec1);
        }
    }
	
	private int[][] splitAssignment(int[] sol) {
    	int[] ra = new int[this.data.getNumRows()];
    	int[] ca = new int[this.data.getNumColumns()];
    	
    	int count=0;
    	while (count<ra.length) {
    		ra[count] = sol[count];
    		count++;
    	}
    	while (count<ca.length+ra.length) {
    		ca[count-ra.length] = sol[count];
    		count++;
    	}
    	int[][] result = new int[2][];
    	result[0] = ra;
    	result[1] = ca;
    	return result;
    }

}
