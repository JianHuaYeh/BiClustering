package edu.saic.biclustering.test;

import edu.saic.biclustering.BiCluster;

public class BiClusterTest {
	
	public static void main(String[] args) {
		BiCluster bc = new BiCluster(4, 4);
		
	 	double[] row0 = new double[]{1, 2, 0.5, 1.5};
		double[] row1 = new double[]{2, 4, 1, 3};
		double[] row2 = new double[]{4, 8, 2, 6};
		double[] row3 = new double[]{3, 6, 1.5, 4.5};
		
		/*
	 	double[] row0 = new double[]{1, 1, 1, 1};
		double[] row1 = new double[]{2, 2, 2, 2};
		double[] row2 = new double[]{3, 3, 3, 3};
		double[] row3 = new double[]{4, 4, 4, 4};
		*/
		try {
			bc.setRow(0, row0);			bc.setRow(1, row1);
			bc.setRow(2, row2);			bc.setRow(3, row3);
			double var = bc.VAR();
			double msr = bc.MSR();
			double fitness = bc.fitness();
			System.out.println("Original: VAR="+var+", MSR="+msr+", fitness="+fitness);
			BiCluster bc2 = bc.rowNormalized();
			double var2 = bc2.VAR();
			double msr2 = bc2.MSR();
			double fitness2 = bc2.fitness();
			System.out.println("Row normalized: VAR="+var2+", MSR="+msr2+", fitness="+fitness2);
			BiCluster bc3 = bc.columnNormalized();
			double var3 = bc3.VAR();
			double msr3 = bc3.MSR();
			double fitness3 = bc3.fitness();
			System.out.println("Column normalized: VAR="+var3+", MSR="+msr3+", fitness="+fitness3);
			BiCluster bc4 = (bc.rowNormalized()).columnNormalized();
			double var4 = bc4.VAR();
			double msr4 = bc4.MSR();
			double fitness4 = bc4.fitness();
			System.out.println("Row+Column normalized: VAR="+var4+", MSR="+msr4+", fitness="+fitness4);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
