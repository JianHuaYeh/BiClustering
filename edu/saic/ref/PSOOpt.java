package edu.saic.ref;

import java.util.ArrayList;


public class PSOOpt {
	public static final int SWARM_SIZE = 30;
	public static final int MAX_ITERATION = 1000;
	public static final double CONST_C1 = 2.0;
	public static final double CONST_C2 = 2.0;
	private int swarmSize;
	private int maxIteration;
	private double C1;
	private double C2;
	private ArrayList<Particle> swarm;
	
	public static void main(String[] args) {
		PSOOpt pso = new PSOOpt();
		pso.go();
	}
	
	public PSOOpt() { this(SWARM_SIZE, MAX_ITERATION, CONST_C1, CONST_C2); }
	public PSOOpt(int size) { this(size, MAX_ITERATION, CONST_C1, CONST_C2); }
	public PSOOpt(int size, int iter) { this(size, iter, CONST_C1, CONST_C2); }
	public PSOOpt(int size, int iter, double c1, double c2) {
		this.swarmSize = size;
		this.maxIteration = iter;
		this.C1 = c1;
		this.C2 = c2;
		swarm = initSwarm();
	}
	
	public ArrayList<Particle> initSwarm() {
	ArrayList<Particle> pop = new ArrayList<Particle>();
		for (int i=0; i<swarmSize; i++) {
			Particle p = new Particle();
			p.location.x = Math.random()*3.0+1;
			p.location.y = Math.random()*2.0-1;
			p.velocity.x = Math.random()*2.0-1;
			p.velocity.y = Math.random()*2.0-1;
			pop.add(p);
		}
		return pop;
	}
	
	private ArrayList<Double> calculateAllFitness() {
		ArrayList<Double> result = new ArrayList<Double>();
		for (Particle p: swarm) result.add(p.getFitness());
		return result;
	}

	public void go() {
		int t=0;
		ArrayList<Particle.Data> lBestLoc = new ArrayList<Particle.Data>();
		ArrayList<Double> lBestFitness = new ArrayList<Double>();
		Particle.Data gBestLoc = new Particle.Data();
		double gBestFitness = Double.MAX_VALUE;
		while (t<maxIteration) {
			ArrayList<Double> f = calculateAllFitness();
			// find local best parameters
			if (t == 0) {
				for (Particle p: swarm) lBestLoc.add(p.location);
				for (Double d: f) lBestFitness.add(d);
			}
			else {
				for (int i=0; i<swarmSize; i++) {
					double d = (double)f.get(i);
					if (d < (double)lBestFitness.get(i)) {
						lBestFitness.set(i, d);
						lBestLoc.set(i, swarm.get(i).location);
					}
				}
			}
			// find global best parameters
			for (int i=0; i<swarmSize; i++) {
				double fit = (double)lBestFitness.get(i);
				if (fit < gBestFitness) {
					gBestFitness = fit;
					gBestLoc.x = lBestLoc.get(i).x;
					gBestLoc.y = lBestLoc.get(i).y;
				}
			}
			
			// update swarm particles
			double w = 1.0-t/(double)maxIteration;
			for (int i=0; i<swarmSize; i++) {
				Particle p = swarm.get(i);
				double r1 = Math.random();
				double r2 = Math.random();
				double newVelX = w*p.velocity.x+
				         C1*r1*(lBestLoc.get(i).x-p.location.x)+
				         C2*r2*(gBestLoc.x-p.location.x);
				double newVelY = w*p.velocity.y+
				         C1*r1*(lBestLoc.get(i).y-p.location.y)+
				         C2*r2*(gBestLoc.y-p.location.y);
				double newX = p.location.x+newVelX;
				double newY = p.location.y+newVelY;
				p.location.x = newX;
				p.location.y = newY;
				p.velocity.x = newVelX;
				p.velocity.y = newVelY;
			}
			t++;
		}
		
		System.out.println("Best x="+gBestLoc.x);
		System.out.println("Best y="+gBestLoc.y);
		System.out.println("Best fitness="+gBestFitness);
	}
}
