/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering;

import java.util.*;

/**
 *
 * @author jhyeh
 */
public class BiCluster implements Cloneable {
	//private static final int TYPE_CONSTANT = 0;
	//private static final int TYPE_CONSTANTROWS = 1;
	//private static final int TYPE_CONSTANTCOLUMNS = 2;
	//private static final int TYPE_COHERENT = 3;
	//private static final int TYPE_UNDEFINED = -1;
	public static final int FITNESS_MSR = 0;
	public static final int FITNESS_M_INVV = 1;
	public static final int FITNESS_PCC = 2;
	
	private boolean debug = true;
    private double[][] data;
    //private int type; // 0: constant, 1: constant rows, 2: constant columns, 3: coherent additive/mutiplicative
    
    /*public static void main(String[] args) {
    	double[][] m = {{1,2,5}, {2,4,6}};
    	BiCluster bc = new BiCluster(m);
    	System.out.println(bc.getTypeString());
    }*/
    
    public BiCluster(int r, int c) { 
    	this.data = new double[r][c]; 
    	//type=TYPE_UNDEFINED;
    }
    public BiCluster(double[][] val) { 
    	this.data = val; 
    	//type=TYPE_UNDEFINED;
    }
    
    public BiCluster rowNormalized(int r) throws Exception {
    	if (data == null) throw new Exception("Null data.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
    	BiCluster copy = this.clone();
    	double[] row = copy.getRowCopy(r);
    	double mean = copy.rowMean(r);
    	for (int i=0; i<row.length; i++)
    		row[i] /= mean;
    	copy.setRow(r, row);
    	return copy;
    }
    
    public BiCluster rowNormalized() {
    	BiCluster copy = this.clone();
    	int rows = copy.getNumRows();
    	for (int r=0; r<rows; r++) {
    		try {
    			double[] row = copy.getRowCopy(r);
    			double mean = copy.rowMean(r);
    			for (int i=0; i<row.length; i++)
    				row[i] /= mean;
    			copy.setRow(r, row);
    		} catch (Exception e) {}
    	}
    	return copy;
    }
    
    public BiCluster columnNormalized(int c) throws Exception {
    	if (data == null) throw new Exception("Null data.");
    	if (c >= data[0].length) throw new Exception("Column assignment out of range.");
    	BiCluster copy = this.clone();
    	double[] col = copy.getColumnCopy(c);
    	double mean = copy.columnMean(c);
    	for (int i=0; i<col.length; i++)
    		col[i] /= mean;
    	copy.setColumn(c, col);
    	return copy;
    }
    
    public BiCluster columnNormalized() {
    	BiCluster copy = this.clone();
    	int cols = copy.getNumColumns();
    	for (int c=0; c<cols; c++) {
    		try {
    			double[] col = copy.getColumnCopy(c);
    			double mean = copy.columnMean(c);
    			for (int i=0; i<col.length; i++)
    				col[i] /= mean;
    			copy.setColumn(c, col);
    		} catch (Exception e) {}
    	}
    	return copy;
    }
    
    public double rowMean(int r) throws Exception {
    	double[] row = this.getRowCopy(r);
    	double sum=0.0;
    	for (double d: row) sum += d;
    	return sum/row.length;
    }
    
    public double columnMean(int c) throws Exception {
   		double[] column = this.getColumnCopy(c);
   		double sum=0.0;
   		for (double d: column) sum += d;
   		return sum/column.length;
    }
    
    public double mean() throws Exception {
    	double sum=0.0;
    	int rows=this.getNumRows();
   		for (int r=0; r<rows; r++) {
   			sum += rowMean(r);
   		}
   		return sum/rows;
    }
    
    public double MSR() throws Exception {
    	int rows=this.getNumRows();
    	int cols=this.getNumColumns();
    	double mean=this.mean();
    	double sum=0.0;
    	for (int r=0; r<rows; r++) {
    		double rm = this.rowMean(r);
    		for (int c=0; c<cols; c++) {
    			double cm = columnMean(c);
    			double residue = this.data[r][c]-rm-cm+mean;
    			sum += residue*residue;
    		}
    	}
    	return sum/(rows*cols);
    }
    
    public double VAR() throws Exception {
    	int rows=this.getNumRows();
    	int cols=this.getNumColumns();
    	double mean=this.mean();
    	double sum=0.0;
    	for (int r=0; r<rows; r++) {
    		for (int c=0; c<cols; c++) {
    			sum += (this.data[r][c]-mean)*(this.data[r][c]-mean);
    		}
    	}
    	return sum/(rows*cols);
    }
    
    public double PCC(int row1, int row2) throws Exception {
    	int cols=this.getNumColumns();
    	double sum1=0.0, sum2=0.0, sum3=0.0;
    	for (int c=0; c<cols; c++) {
    		double rm1 = rowMean(row1);
    		double rm2 = rowMean(row2);
    		double diff1 = (this.data[row1][c]-rm1);
    		double diff2 = (this.data[row2][c]-rm2);
    		sum1 += diff1*diff2;
    		sum2 += diff1*diff1;
    		sum3 += diff2*diff2;
    	}
    	return sum1/Math.sqrt(sum2*sum3);
    }
    
    public double invAvgPCC() throws Exception {
    	int rows=this.getNumRows();
    	double sum=0.0;
    	int count=0;
    	for (int r1=0; r1<rows; r1++) {
    		for (int r2=r1+1; r2<rows; r2++) {
    			sum += PCC(r1, r2);
    			count++;
    		}
    	}
    	return count/sum;
    }
    
    public double fitness(int which) {
    	try {
    		switch (which) {
    			case 1: return MSR()+1.0/VAR();
    			case 2: return invAvgPCC();
    			case 0:
    			default: return MSR();
    		}
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    	}
    	return -1.0;
    }
    
    public double fitness() {
    	return this.fitness(0);
    }
    
    /*public double fitness() {
    	int type = this.getType();
    	if (type == TYPE_UNDEFINED) return -1.0;
    	
    	BiCluster bc = this;
    	try {
    		switch (type) {
    			//case TYPE_CONSTANT: bc = this; break;
    			case TYPE_CONSTANTROWS: bc = this.rowNormalized(); break;
    			case TYPE_CONSTANTCOLUMNS: bc = this.columnNormalized(); break;
    			case TYPE_COHERENT: bc = this.rowNormalized().columnNormalized(); break;
    			//default
    		}
    		return bc.MSR()/bc.getNumRows()*bc.getNumColumns();
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    	}
    	return -1.0;
    }*/
    
    public double fitness(double delta) {
    	try {
    		int rows=this.getNumRows();
        	int cols=this.getNumColumns();
    		double msr = MSR();
    		if (msr <= delta) return 1.0/(rows*cols);
    		else return msr/delta;
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    	}
    	return Double.MAX_VALUE;
    }
    
    public BiCluster clone() {
    	try {
    		int rows = getNumRows();
    		int columns = getNumColumns();
    		BiCluster copy = new BiCluster(rows, columns);
    		for (int i=0; i<rows; i++) { copy.setRow(i, this.getRowCopy(i)); }
    		return copy;
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    		return null;
    	}
    }
    
    public int getNumRows() { return this.data.length; }
    public int getNumColumns() { return this.data[0].length; }
    
    /*public String getTypeString() {
    	if (type < 0) this.getType();
    	String str = "N/A";
    	switch (type) {
			case TYPE_CONSTANT: str = "constant"; break;
			case TYPE_CONSTANTROWS: str = "constant rows"; break;
			case TYPE_CONSTANTCOLUMNS: str = "constant columns"; break;
			case TYPE_COHERENT: str = "coherent"; break;
			default: str = "undefined";
    	}
    	if (debug) System.err.println("Type="+str);
    	return str;
    }
    
	public int getType() {
		if (type >= 0) return type; // been calculated
		try {
			double msr = this.MSR();
			double msr_r = this.rowNormalized().MSR();
			double msr_c = this.columnNormalized().MSR();
			double msr_rc = this.rowNormalized().columnNormalized().MSR();
			if (debug) System.err.println("(msr,msr_r,msr_c,msr_rc)=("+msr+","+msr_r+","+msr_c+","+msr_rc+")");
			// type = constant
			if (msr == 0.0) type = TYPE_CONSTANT;
			// msr_r smallest, type = constant rows
			else if ((msr_r<=msr_c) && (msr_r<=msr_rc)) type = TYPE_CONSTANTROWS;
			// msr_c smallest, type = constant columns
			else if ((msr_c<=msr_r) && (msr_c<=msr_rc)) type = TYPE_CONSTANTCOLUMNS;
			// msr_rc smallest, type = coherent
			else if ((msr_rc<=msr_r) && (msr_rc<=msr_c)) type = TYPE_COHERENT;
			return type;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return TYPE_UNDEFINED;
	}*/
    
    public void setRow(int r, double[] row) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (row.length != data[0].length) throw new Exception("Row length mismatch.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
        this.data[r] = row.clone();
    }
    
    public void setColumn(int c, double[] column) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (column.length != data.length) throw new Exception("Column length mismatch.");
        if (c >= data[0].length) throw new Exception("Column assignment out of range.");
        for (int i=0; i<column.length; i++) {
            this.data[i][c] = column[i];
        }
    }
    
    public double getCell(int r, int c) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
        if (c >= data[0].length) throw new Exception("Column assignment out of range.");
        return this.data[r][c];
    }
    
    public double[] getRowCopy(int r) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
        return this.data[r].clone();
    }
    
    public double[] getColumnCopy(int c) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (c >= data[0].length) throw new Exception("Column assignment out of range.");
        double[] column = new double[data.length];
        for (int i=0; i<column.length; i++) {
            column[i] = this.data[i][c];
        }
        return column;
    }
    
    public double[][] getData() {
    	return this.data.clone();
    }
    
    public String toString() {
    	String result = "";
    	for (int i=0; i<this.data.length; i++) {
    		for (int j=0; j<this.data[0].length; j++) {
    			result += this.data[i][j]+"\t";
    		}
    		result += "\n";
    	}
    	return result;
    }
    
    /*
    public ArrayList<BiCluster> getRowPermutation() {
        return null;
    }

    public ArrayList<BiCluster> getColumnPermutation() {
        return null;
    }

    public ArrayList<BiCluster> getAllPermutation() {
        ArrayList<BiCluster> result = new ArrayList<BiCluster>();
        result.addAll(getRowPermutation());
        result.addAll(getColumnPermutation());
        return result;
    }
	*/
}
