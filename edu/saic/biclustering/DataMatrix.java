package edu.saic.biclustering;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DataMatrix {
	private double[][] data;
	
	public DataMatrix(String s) {
		if (!loadData(s)) {
			System.err.println("Data loading error, stop.");
			System.exit(-1);
		}
	}
	
	public DataMatrix(double[][] ii) {
		this.data = (double[][])ii.clone();
	}
	
	public int getNumRows() { return this.data.length; }
    public int getNumColumns() { return this.data[0].length; }
    
    public double[] getRowCopy(int r) throws Exception {
        if (data == null) throw new Exception("Null data.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
        return this.data[r].clone();
    }
    
    public double getDataCell(int r, int c) throws Exception {
    	if (data == null) throw new Exception("Null data.");
        if (r >= data.length) throw new Exception("Row assignment out of range.");
        if (c >= data[0].length) throw new Exception("Column assignment out of range.");
        return this.data[r][c];
    }

    public double[][] getData() {
    	double[][] newdata = this.data.clone();
    	return newdata;
    }
    
    public boolean loadData(String s) {
    	try {
    		// count rows
    		BufferedReader br = new BufferedReader(new FileReader(s));
    		int rows=0;
    		while (br.readLine() != null) rows++;
    		br.close();
    		rows--; // skip the frist header line
    		
    		// reopen for real data input
    		br = new BufferedReader(new FileReader(s));
    		String line="";
    		// first row and column are headers 
    		line = br.readLine();
    		StringTokenizer st = new StringTokenizer(line, "\t");
    		int cols = st.countTokens()-1;
    		
    		this.data = new double[rows][cols];
    		int count=0;    		
    		while ((line=br.readLine()) != null) {
    			// sample: p2	4.22	6.2	2.65	3.04	1.39	2.43	3.22	3.87	1.05	0.482	0.324	-0.645	0.571	-0.251	0.697	1.55	0.221	-1.56	0.708	-0.484	
    			// Tabbed separation
    			st = new StringTokenizer(line, "\t");
    			double[] row = new double[cols];
    			int idx=0;
    			st.nextToken();
    			while (st.hasMoreTokens()) {
    				double val = Double.parseDouble(st.nextToken());
    				row[idx++] = val;
    			}
    			this.data[count++] = row;
    		}
    		br.close();
    		return true;
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    	}
    	return false;
    }
    
    public BiCluster getBiCluster(int[] ra, int[] ca) throws Exception {
    	// ra/ca: row/column assignment, e.g. {0, 1, 1, 0, ...}
    	int rlen=0;
    	for (int v: ra) rlen += v;
    	int clen=0;
    	for (int v: ca) clen += v;
    	BiCluster bc = new BiCluster(rlen, clen);
    	
    	double[] newrow = new double[clen];
    	int idx=0;
    	for (int i=0; i<ra.length; i++) {
    		if (ra[i] == 0) continue;    		
    		double[] row = this.data[i];
    		int idx2=0;
    		for (int j=0; j<ca.length; j++) {
    			if (ca[j] == 0) continue;
    			newrow[idx2] = row[j];
    			idx2++;
    		}    		
    		bc.setRow(idx, newrow);
    		idx++;
    	}
    	return bc;
    }
    
    public DataMatrix rearrange(int[] sol) {
    	int[][] result = splitAssignment(sol);
    	return rearrange(result[0], result[1]);
    }

    public DataMatrix rearrange(int[] ra, int[] ca) {
    	double[][] newdata = this.data.clone();
    	// row rearrange
    	int[] ra2 = (int[])ra.clone();
    	int i=0;
    	while (i<ra2.length) {
    		if (ra2[i] == 0) {
    			int j=i+1;
    			boolean found=false;
    			while ((j<ra2.length) && (!found)) {
    				if (ra2[j] == 1) found=true;
    				else j++;
    			}
    			if (found) {
    				// swap assignment i,j
    				int tmp=ra2[i];	ra2[i]=ra2[j]; ra2[j]=tmp;
    				// swap row data
    				double[] tmp2=newdata[i]; newdata[i]=newdata[j]; newdata[j]=tmp2;
    			}
    			else break;
    		}
    		i++;
    	}
    	// column rearrange
    	int[] ca2 = (int[])ca.clone();
    	i=0;
    	while (i<ca2.length) {
    		if (ca2[i] == 0) {
    			int j=i+1;
    			boolean found=false;
    			while ((j<ca2.length) && (!found)) {
    				if (ca2[j] == 1) found=true;
    				else j++;
    			}
    			if (found) {
    				// swap assignment i,j
    				int tmp=ca2[i];	ca2[i]=ca2[j]; ca2[j]=tmp;
    				// swap column data
    				for (int k=0; k<newdata.length;k++) {
    					// for each row, swap specific column position
    					double tmp2=newdata[k][i]; newdata[k][i]=newdata[k][j]; newdata[k][j]=tmp2; 
    				}
    			}
    			else break;
    		}
    		i++;
    	}
    	DataMatrix mat = new DataMatrix(newdata);
    	return mat;
    }
    
    private int[][] splitAssignment(int[] sol) {
    	int[] ra = new int[getNumRows()];
    	int[] ca = new int[getNumColumns()];
    	
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
    
    private double[] findParam(double[][] dmd) {
    	double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	for (double[] row: dmd) {
    		for (double cell: row) {
    			if (cell < min) min = cell;
    			if (cell > max) max = cell;
    		}
    	}
    	double range = max-min;
    	range = (range==0.0)?1.0:range;
    	double scale = 255.0/range;
    	double[] result={min, scale};
    	return result;
    	
    }
    
    public void paintSolution(String title, int BLOCKSIZE) {
    	double[][] dmd = this.data;
    	// find base, scale
    	double[] param = findParam(dmd);
    	double base = param[0];
    	double scale = param[1];
    	//System.err.println("base="+base+", scale="+scale);
    	int rows = dmd.length;
    	int cols = dmd[0].length;
    	BufferedImage img = new BufferedImage(rows*BLOCKSIZE, cols*BLOCKSIZE, BufferedImage.TYPE_3BYTE_BGR);
    	Graphics g = img.getGraphics();
    	for (int i=0; i<dmd.length; i++) {
    		for (int j=0; j<dmd[0].length; j++) {
    			// paint 4x4 block
    			double c = (dmd[i][j]-base)*scale;
    			//System.err.println("data="+dmd[i][j]+", color="+c);
    			Color color = new Color(0, 0, (int)c);
    			g.setColor(color);
    			g.fillRect(i*BLOCKSIZE, j*BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
    		}
    	}
    	
   		JFrame frame = new JFrame(title);
   		int wsize=(dmd.length+1)*BLOCKSIZE;
   		int hsize=(dmd[0].length+1)*BLOCKSIZE;
   		frame.setSize(wsize+2*BLOCKSIZE, hsize+2*BLOCKSIZE+40);
   		Container pane = frame.getContentPane();
   		JLabel picLabel = new JLabel(new ImageIcon(img));
   		pane.add(picLabel);
   		pane.repaint(); 
   		frame.setVisible(true);
    }
	
    // for test
	public static void main(String[] args) throws Exception {
		/*String fname = "/home/jhyeh/Desktop/expr/bicluster/ref/syntheticData.txt_bicluster_1.txt";
		DataMatrix dm = new DataMatrix(fname);
		int[] ra = {1, 0, 1};
		int[] ca = {0, 1, 0, 1};
		BiCluster bc = dm.getBiCluster(ra, ca);
		System.err.println(bc);*/
		double[][] dd = new double[4][4];
		dd[0] = new double[]{1,2,3,4};
		dd[1] = new double[]{5,6,7,8};
		dd[2] = new double[]{9,10,11,12};
		dd[3] = new double[]{13,14,15,16};
		DataMatrix dm = new DataMatrix(dd);
		dd = dm.getData();
		for (double[] dl: dd) {
			for (double d: dl) {
				System.err.print(d+" ");
			}
			System.err.println();
		}
		System.err.println("====================");
		int[] ra = new int[]{0,1,0,1};
		int[] ca = new int[]{1,1,0,1};
		DataMatrix dm2 = dm.rearrange(ra, ca);
		double[][] dd2 = dm2.getData();
		for (double[] dl: dd2) {
			for (double d: dl) {
				System.err.print(d+" ");
			}
			System.err.println();
		}
		dm2.paintSolution("test", 40);
	}
	
}
