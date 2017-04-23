package edu.saic.biclustering;

import edu.saic.biclustering.util.ASObject;

public class CalcCostThread implements Runnable {
	private int[] r;
	private DataMatrix data;
	private ASObject[] objs;
	private int id;
	private boolean DEBUG = true;
	
	public CalcCostThread(DataMatrix dm, int[] sol, ASObject[] array, int i) {
		this.data = dm;
		this.r = sol;
		this.objs = array;
		this.id = i;
	}
	
	public void run() {
		int dim=0;
   		for (int v: r) dim += v;
   		double cost2 = 1.0/dim;
   		double cost3 = this.cost(r);
   		objs[id] = new ASObject(id, cost2, cost3);
	}
	
	public double cost(int[] sol) {
    	int[][] result = splitAssignment(sol);
    	int[] ra = result[0];
    	int[] ca = result[1];
    	
    	// now we have row/column assignment
    	try {
    		BiCluster bc = this.data.getBiCluster(ra, ca);
    		//BiCluster bc2 = (bc.rowNormalized()).columnNormalized();
    		//double fit = bc2.fitness();
    		double fit = bc.fitness(BiCluster.FITNESS_PCC);
    		return fit;
    	} catch (Exception e) {
    		if (DEBUG)
    			e.printStackTrace(System.err);
    	}
    	return Double.MAX_VALUE;
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
