package edu.saic.ref;


public class Particle {
	
	public static class Data { public double x, y; }
	
	public Data location;
	public Data velocity;
	private double fitness;
	
	public Particle() {
		location = new Data();
		velocity = new Data();
	}
	
	public double getFitness() {
		double x = location.x;
		double y = location.y;
		fitness = Math.pow(2.8125-x+x*y*y*y*y,2)+
				  Math.pow(2.25-x+x*y*y,2)+
				  Math.pow(1.5-x+x*y,2);
		return fitness;
	}
}
