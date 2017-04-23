package edu.saic.biclustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.saic.biclustering.util.ASObject;

public class BiClusteringGA {
	// constants, GA params
    private static int POPSIZE = 100;
    private static double ELITE = 0.2;
    private static int MAXITER = 100;
    private static double MUTPROB = 0.2;
    private static int RESTARTMAX = 50;
    // contants, bicluster params
	private static int MINDIM = 3;
	private static double FITNESSLIMIT = 1.0;
	private boolean DRAWSOLUTION = false;

	private DataMatrix data;
	private int delta; // threshold for delta-bicluster
	private int solutionLen;
	ArrayList<int[]> solution;
    
    private static boolean DEBUG = true;
	
	public static void main(String[] args) {
		//BiClusteringGA bcga = new BiClusteringGA(args[0]);
		String fname = "syntheticData.txt_bicluster_1.txt";
		BiClusteringGA bcga = new BiClusteringGA(fname);
		bcga.run();
	}
	
	public BiClusteringGA(String s) {
		this(s, false);
	}
	
	public BiClusteringGA(String s, boolean draw) {
		this.data = new DataMatrix(s);
		this.solutionLen = this.data.getNumRows()+this.data.getNumColumns();
		this.delta = this.solutionLen; // currently we use the maximal delta
		this.DRAWSOLUTION = draw;
		this.solution = null;
	}
	
	public BiClusteringGA(DataMatrix d) {
		this(d, false);
	}
	
	public BiClusteringGA(DataMatrix d, boolean draw) {
		this.data = d;
		this.solutionLen = this.data.getNumRows()+this.data.getNumColumns();
		this.delta = this.solutionLen; // currently we use the maximal delta
		this.DRAWSOLUTION = draw;
		this.solution = null;
	}
	
	public ArrayList<int[]> getSolution() {
		if (this.solution == null) {
			run();
		}
		return this.solution;
	}
	
	public void run() {
		//runGA();
		try {
			this.solution = randomRestartGA();
			System.out.println("Solutions set size="+this.solution.size());
            int count=0;
            for (int[] sol2: this.solution) {
            	printSol(count, sol2);
            	System.out.println("==================================================================");
            	if (DRAWSOLUTION) {
            		// paint solution
            		DataMatrix result = this.data.rearrange(sol2);
            		BiCluster bc = new BiCluster(result.getData());
            		bc = bc.rowNormalized().columnNormalized();
            		result = new DataMatrix(bc.getData());
            		result.paintSolution(""+count, 10);
            	}
            	count++;
            }
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public double fitness(BiCluster bc) {
		//return bc.fitness(this.delta);
		return bc.fitness(BiCluster.FITNESS_MSR);
	}
	
	public void runGA() {
		//int[] bestSol = this.geneticOptimize();
		ArrayList<int[]> sols = this.geneticOptimize(null);
		int[] bestSol = sols.get(0);
		try {
			int[][] result = splitAssignment(bestSol);
	    	int[] ra = result[0];
	    	int[] ca = result[1];
    		BiCluster bc = this.data.getBiCluster(ra, ca);
    		System.out.println(bc);
    		BiCluster bc2 = (bc.rowNormalized()).columnNormalized();
    		double var = bc2.VAR();
			double msr = bc2.MSR();
			//double fit = bc2.fitness();
			double fit = fitness(bc);
    		System.out.println("Normalized VAR="+var+", MSR="+msr+", fitness(pcc)="+fit);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void printSol(int id, int[] sol) throws Exception {
		int[][] result = splitAssignment(sol);
    	int[] ra = result[0];
    	int[] ca = result[1];
		BiCluster bc = this.data.getBiCluster(ra, ca);
		double var = bc.VAR();
		double msr = bc.MSR();
		double fit = fitness(bc);
        //System.out.println(bc);
		//BiCluster bc2 = (bc.rowNormalized()).columnNormalized();
		//double var2 = bc2.VAR();
		//double msr2 = bc2.MSR();
		//double fit2 = bc2.fitness();
		System.out.print("Solution "+id+": ");
		int dim=0;
		for (int v: sol) {
			System.out.print(v+" ");
			dim += v;
		}
		System.out.println();
		System.out.println("Fitness="+fit+", VAR="+var+", MSR="+msr+", dim="+dim);
		//System.out.println("Normalized VAR="+var2+", MSR="+msr2+", MSR/VAR="+(msr2/var2)+", fitness="+fit2+", dim="+dim);
		//System.out.println("BiCluster type="+bc.getTypeString()+", fitness="+fit+", dim="+dim);
	}
	
	private void mergeSol(ArrayList<int[]> allsols, ArrayList<int[]> sols) {
		for (int[] sol: sols) {
			boolean found=false;
			for (int[] sol2: allsols) {
				if (Arrays.equals(sol, sol2)) {
					found = true;
					break;
				}
			}
			if (found) continue;
			allsols.add(sol);
		}
	}
	
	public ArrayList<int[]> randomRestartGA() throws Exception {
		return this.randomRestartGA(null);
	}

    public ArrayList<int[]> randomRestartGA(Set<Integer>[] recomm) throws Exception {
        double best = Double.MAX_VALUE;
        int restartMax = RESTARTMAX;
        int[] bestsol = null;
        ArrayList<int[]> allsols = new ArrayList<int[]>();
        //ArrayList<int[]> bestsols = new ArrayList<int[]>();
        int iteration = 0;
        int bestiter = 0;
        while (true) {
            //int[] sol = this.geneticOptimize();
        	if (DEBUG) System.err.println("Iteration "+iteration);
        	ArrayList<int[]> sols = this.geneticOptimize(recomm);
        	if (DEBUG) System.err.println("Single iteration done.");
        	mergeSol(allsols, sols);
        	int[] sol = sols.get(0);
            double cost = this.cost(sol);
            iteration++;
            
            /*if (DEBUG) {
            	System.err.println("Iteration "+iteration+", current lowest cost="+cost+" (best cost: "+best+")");
            	System.err.println("Current solution candidate set size="+allsols.size());
            	for (int i: sol) System.err.print(i+" ");
            	System.err.println();
            }*/
            
            if (cost < best) {
                best = cost;
                bestsol = sol;
                //bestsols = sols;
                bestiter = iteration;
                int dim=0;
                for (int v: sol) dim += v;
                System.out.println("Iteration "+iteration+", best cost="+best+", dim="+
    					dim+", solution set size: "+allsols.size());
            }
            else {
           		int dim=0;
           		for (int v: sol) dim += v;
           		System.err.println("Iteration "+iteration+", better cost="+cost+", dim="+
           				dim+", solution set size: "+allsols.size()+", best not replaced.");
            }
            double coveragepct = calcRowCoverage(allsols);
            if ((coveragepct >= 1.0) || (iteration-bestiter >= restartMax)) {
                //double coveragepct = calcRowCoverage(allsols);
                System.out.println("Maybe stablized, stop at iteration "+iteration+", solution space row coverage="+coveragepct);
                ASObject[] array = new ASObject[allsols.size()];
                for (int i=0; i<allsols.size(); i++) {
                    int[] r = (int[])allsols.get(i);
                    //double cost2 = this.cost(r);
                    int dim=0;
               		for (int v: r) dim += v;
               		double cost2 = 1.0/dim;
               		double cost3 = this.cost(r);
                    array[i] = new ASObject(i, cost2, cost3);
                }
                Arrays.sort(array);
                ArrayList<int[]> allsols2 = cutAtRowCoverage(allsols, array, 1.0); 
                
                return allsols2;
                
                /*System.out.println("Solutions set of coverage% >= 1.0, size="+allsols2.size());
                int count=0;
                for (int[] sol2: allsols2) {
                	printSol(count, sol2);
                	System.out.println("==================================================================");
                	if (DRAWSOLUTION) {
                		// paint solution
                		DataMatrix result = this.data.rearrange(sol2);
                		BiCluster bc = new BiCluster(result.getData());
                		bc = bc.rowNormalized().columnNormalized();
                		result = new DataMatrix(bc.getData());
                		result.paintSolution(""+count, 10);
                	}
                	count++;
                }*/
                
                /*System.out.println("Top solutions(10 out of "+allsols.size()+"): ");
                int count=0;
                for (ASObject obj: array) {
                	int idx = obj.label;
                	int[] sol2 = allsols.get(idx);
                	printSol(count, sol2);
                	System.out.println("==================================================================");
                	if (DRAWSOLUTION) {
                		// paint solution
                		DataMatrix result = this.data.rearrange(sol2);
                		BiCluster bc = new BiCluster(result.getData());
                		bc = bc.rowNormalized().columnNormalized();
                		result = new DataMatrix(bc.getData());
                		result.paintSolution(""+count, 10);
                	}

                	count++;
                	if (count >= 10) break;
                }*/
                
                /*ASObject obj = array[0];
                int idx = obj.label;
            	int[] sol2 = allsols.get(idx);
            	DataMatrix result = this.data.rearrange(sol2);
            	BiCluster bc = new BiCluster(result.getData());
            	bc = bc.rowNormalized().columnNormalized();
            	result = new DataMatrix(bc.getData());
            	result.paintSolution(10);*/
                //break;
            }
        }
    }
    
    public ArrayList<int[]> cutAtRowCoverage(ArrayList<int[]> allsols, ASObject[] array, double threshold) {
    	HashSet<Integer> rowtag = new HashSet<Integer>();
    	ArrayList<int[]> result = new ArrayList<int[]>();
    	int[][] split = this.splitAssignment(allsols.get(0));
    	int rows = split[0].length;
    	for (ASObject obj: array) {
        	int idx = obj.label;
        	int[] sol = allsols.get(idx);
        	result.add(sol);
        	split = this.splitAssignment(sol);
    		for (int i=0; i<split[0].length; i++) {
    			if (split[0][i] == 1) rowtag.add(i);
    		}
    		double coverage = rowtag.size()/(double)rows;
    		if (DEBUG) System.err.println("Coverage: "+coverage);
    		if (coverage >= 1.0) break;
    	}
    	return result;
    }
    
    public double calcRowCoverage(ArrayList<int[]> allsols) {
    	HashSet<Integer> rowtag = new HashSet<Integer>();
    	int rows = 0;
    	for (int[] sol: allsols) {
    		int[][] split = this.splitAssignment(sol);
    		rows = split[0].length;
    		for (int i=0; i<split[0].length; i++) {
    			if (split[0][i] == 1) rowtag.add(i);
    		}
    	}
    	return rowtag.size()/(double)rows;
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
    
    public double cost(int[] sol) {
    	int[][] result = splitAssignment(sol);
    	int[] ra = result[0];
    	int[] ca = result[1];
    	
    	// now we have row/column assignment
    	try {
    		BiCluster bc = this.data.getBiCluster(ra, ca);
    		//BiCluster bc2 = (bc.rowNormalized()).columnNormalized();
    		//double fit = bc2.fitness();
    		double fit = fitness(bc);
    		return fit;
    	} catch (Exception e) {
    		if (DEBUG)
    			e.printStackTrace(System.err);
    	}
    	return Double.MAX_VALUE;
    }
    
    private boolean isSolution(int[] sol) {
    	// (row dim >= 3) && (col dim >= 3)
    	int[][] result = splitAssignment(sol);
    	int[] ra = result[0];
    	int[] ca = result[1];
    	
    	int rowcount=0;
    	for (int v: ra) rowcount += v;
    	int colcount=0;
    	for (int v: ca) colcount += v;
    	
    	if ((rowcount == 0) || (colcount == 0)) return false;
    	if ((rowcount<MINDIM) || (colcount<MINDIM)) return false;
    	//if (cost(sol) > FITNESSLIMIT) return false;
    	
    	return true;
    }
    
    public ArrayList<int[]> filterSolution(ArrayList<int[]> pop) {
    	if (DEBUG) {
    		System.err.println("Solution population size to filter: "+pop.size());
    	}
    	int popsize = POPSIZE;
    	double elite = ELITE;
    	int topelite=(int)(elite*popsize);
    	ArrayList<int[]> result = new ArrayList<int[]>();
    	for (int[] sol: pop) {
    		/*result.add(sol);
			if (result.size() >= topelite) break;*/
    		try {
    			int[][] split = splitAssignment(sol);
    			int[] ra = split[0];
    			int[] ca = split[1];
    			BiCluster bc = this.data.getBiCluster(ra, ca);
    			//BiCluster bc2 = (bc.rowNormalized()).columnNormalized();
    			//double fit = bc2.fitness();
    			double fit = fitness(bc);
    			if (fit > 0) result.add(sol);
    			//result.add(sol);
    			if (result.size() >= topelite) break;
    		} catch (Exception e) {
    			if (DEBUG)
    				e.printStackTrace(System.err);
    		}
    	}
    	return result;
    }

    public ArrayList<int[]> geneticOptimize(Set<Integer>[] recomm) {
    	int popsize = POPSIZE;
    	double elite = ELITE;
    	int maxiter = MAXITER;
    	double mutprob = MUTPROB;

        // frist generation: random solution
        ArrayList<int[]> pop = new ArrayList<int[]>();
        if (DEBUG) System.err.println("Generating initial population...");
        if (recomm == null) {
        	while (pop.size() < popsize) {
                int[] sol = new int[this.solutionLen];
                for (int j=0; j<sol.length; j++) {
                    sol[j] = (Math.random()>0.5)?1:0;
                }
                if (isSolution(sol)) pop.add(sol);
                //if (DEBUG) System.err.println("Init first generation, pop size = "+pop.size());
            }
        }
        else {
            Set<Integer> rset = recomm[0];
            Set<Integer> cset = recomm[1];
        	while (pop.size() < popsize) {
                int[] sol = new int[this.solutionLen];
                int rows = this.data.getNumRows();
                int cols = this.data.getNumColumns();
                for (int r: rset) {
                	sol[r] = (Math.random()>0.5)?1:0;
                }
                for (int c: cset) {
                	sol[rows+c] = (Math.random()>0.5)?1:0;
                }
                if (isSolution(sol)) pop.add(sol);
            }        	
        }

        int topelite=(int)(elite*popsize);
        int count=0;
        while (count < maxiter) {
        	if (DEBUG) System.err.println("GeneticOptimize iteration "+count+", pop size="+pop.size());
            ASObject[] array = new ASObject[pop.size()];
            
            //ExecutorService es = Executors.newFixedThreadPool(6);
            
            for (int i=0; i<pop.size(); i++) {
                int[] r = (int[])pop.get(i);

                int dim=0;
           		for (int v: r) dim += v;
           		double cost2 = 1.0/dim;
           		double cost3 = this.cost(r);
                array[i] = new ASObject(i, cost2, cost3);
                // now parallel version
                //CalcCostThread ccthread = new CalcCostThread(this.data, r, array, i);
                //es.submit(ccthread);
            }
            /*es.shutdown();
            while (!es.isTerminated()) {            	
            	try {
            		Thread.sleep(1000); // wake up every minute to check 
            	} catch (Exception e) {}
            }*/
            
            Arrays.sort(array);
            ArrayList<int[]> pop2 = new ArrayList<int[]>();
            for (int i=0; i<topelite; i++) {
                pop2.add(pop.get(array[i].label));
            }
            pop = pop2;

            while (pop.size() < popsize) {
            	//if (DEBUG) System.err.println("GeneticOptimize: generating next generation, pop size="+pop.size());
                if (Math.random() < mutprob) {
                    int c = (int)(Math.random()*topelite);
                    int[] sol = mutate((int[])pop.get(c));
                    if (isSolution(sol)) pop.add(sol);
                }
                else {
                    int c1 = (int)(Math.random()*topelite);
                    int c2 = (int)(Math.random()*topelite);
                    int[] sol = crossover((int[])pop.get(c1), (int[])pop.get(c2));
                    if (isSolution(sol)) pop.add(sol);
                }
            }
            count++;
        }

        //return (int[])pop.get(0);
        //return filterSolution(pop);
        return pop;
    }

    // threading version
    public ArrayList<int[]> geneticOptimizeThreading(Set<Integer>[] recomm) {
    	int popsize = POPSIZE;
    	double elite = ELITE;
    	int maxiter = MAXITER;
    	double mutprob = MUTPROB;

        // frist generation: random solution
        ArrayList<int[]> pop = new ArrayList<int[]>();
        if (DEBUG) System.err.println("Generating initial population...");
        if (recomm == null) {
        	while (pop.size() < popsize) {
                int[] sol = new int[this.solutionLen];
                for (int j=0; j<sol.length; j++) {
                    sol[j] = (Math.random()>0.5)?1:0;
                }
                if (isSolution(sol)) pop.add(sol);
                //if (DEBUG) System.err.println("Init first generation, pop size = "+pop.size());
            }
        }
        else {
            Set<Integer> rset = recomm[0];
            Set<Integer> cset = recomm[1];
        	while (pop.size() < popsize) {
                int[] sol = new int[this.solutionLen];
                int rows = this.data.getNumRows();
                int cols = this.data.getNumColumns();
                for (int r: rset) {
                	sol[r] = (Math.random()>0.5)?1:0;
                }
                for (int c: cset) {
                	sol[rows+c] = (Math.random()>0.5)?1:0;
                }
                if (isSolution(sol)) pop.add(sol);
            }        	
        }

        int topelite=(int)(elite*popsize);
        int count=0;
        while (count < maxiter) {
        	if (DEBUG) System.err.println("GeneticOptimize iteration "+count+", pop size="+pop.size());
            ASObject[] array = new ASObject[pop.size()];
            
            ExecutorService es = Executors.newFixedThreadPool(6);
            
            for (int i=0; i<pop.size(); i++) {
                int[] r = (int[])pop.get(i);

                //int dim=0;
           		//for (int v: r) dim += v;
           		//double cost2 = 1.0/dim;
           		//double cost3 = this.cost(r);
                //array[i] = new ASObject(i, cost2, cost3);
                // now parallel version
                CalcCostThread ccthread = new CalcCostThread(this.data, r, array, i);
                es.submit(ccthread);
            }
            es.shutdown();
            while (!es.isTerminated()) {            	
            	try {
            		Thread.sleep(1000); // wake up every minute to check 
            	} catch (Exception e) {}
            }
            
            Arrays.sort(array);
            ArrayList<int[]> pop2 = new ArrayList<int[]>();
            for (int i=0; i<topelite; i++) {
                pop2.add(pop.get(array[i].label));
            }
            pop = pop2;

            while (pop.size() < popsize) {
            	//if (DEBUG) System.err.println("GeneticOptimize: generating next generation, pop size="+pop.size());
                if (Math.random() < mutprob) {
                    int c = (int)(Math.random()*topelite);
                    int[] sol = mutate((int[])pop.get(c));
                    if (isSolution(sol)) pop.add(sol);
                }
                else {
                    int c1 = (int)(Math.random()*topelite);
                    int c2 = (int)(Math.random()*topelite);
                    int[] sol = crossover((int[])pop.get(c1), (int[])pop.get(c2));
                    if (isSolution(sol)) pop.add(sol);
                }
            }
            count++;
        }

        //return (int[])pop.get(0);
        //return filterSolution(pop);
        return pop;
    }

    private int[] copy(int[] sol) {
	    return sol.clone();
	}

	public int[] mutate(int[] r) {
	    int[] vec = copy(r);
	    int i = (int)(Math.random()*vec.length);
	    if (Math.random()<0.5) {
	    	if (vec[i] == 1) vec[i]=0;
	    	else vec[i] = 1;
	    }
	    return vec;
	}

	public int[] crossover(int[] r1, int[] r2) {
	    int[] vec = copy(r1);
	    int pos = (int)(Math.random()*vec.length);
	    for (int i=pos; i<vec.length; i++) {
	        vec[i] = r2[i];
	    }
	    return vec;
	}
}
