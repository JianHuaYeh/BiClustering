/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.saic.ref;

import java.io.*;
import java.util.*;
/**
 *
 * @author jhyeh
 */
public class Optimization {

    private String[][] people = {{"Seymour", "BOS"},
                                 {"Franny", "DAL"},
                                 {"Zooey", "CAK"},
                                 {"Walt", "MIA"},
                                 {"Buddy", "ORD"},
                                 {"Les", "OMA"}};
    private HashMap flights;
    private String destination = "LGA";

    public class ASObject implements Comparable {
        public int label;
        public double score;

        public ASObject(int l, double s) {
            this.label = l;
            this.score = s;
        }

        public int compareTo(Object other) {
            ASObject obj = (ASObject)other;
            if (this.score < obj.score) return -1;
            else if (this.score > obj.score) return 1;
            return 0;
        }
    }

    public static void main(String[] args) {
        Optimization opt = new Optimization(args[0]);
        opt.go();
    }

    public Optimization(String str) {
        this.loadData(str);
    }

    private void loadData(String fname) {
        this.flights = new HashMap();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fname));
            String line = "";
            while ((line=br.readLine()) != null) {
                // sample data: "LGA,OMA,6:19,8:13,239"
                StringTokenizer st = new StringTokenizer(line, ",");
                String origin="", dest="", depart="", arrive="", price="";
                if (st.hasMoreTokens()) origin = st.nextToken().trim();
                if (st.hasMoreTokens()) dest = st.nextToken().trim();
                if (st.hasMoreTokens()) depart = st.nextToken().trim();
                if (st.hasMoreTokens()) arrive = st.nextToken().trim();
                if (st.hasMoreTokens()) price = st.nextToken().trim();
                //String[] key = {origin, dest};
                String key = origin+"-"+dest;
                String[] val = {depart, arrive, price};
                Object obj = this.flights.get(key);
                ArrayList al = null;
                if (obj == null) al = new ArrayList();
                else al = (ArrayList)obj;
                al.add(val);
                //System.out.println(origin+" "+dest+" "+price);
                this.flights.put(key, al);
            }
            br.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // sample solution = [1,4,3,2,7,3,6,3,2,4,5,3]
    public void printSchedule(int[] r) {
        for (int i=0; i<r.length/2; i++) {
            String name = people[i][0];
            String origin = people[i][1];
            //String[] outk = {origin, this.destination};
            String outk = origin+"-"+this.destination;
            //String[] retk = {this.destination, origin};
            String retk = this.destination+"-"+origin;
            ArrayList outal = (ArrayList)this.flights.get(outk);
            if (outal == null) {
                System.out.println("outal null");
                continue;
            }
            String[] out = (String[])outal.get(r[2*i]);
            ArrayList retal = (ArrayList)this.flights.get(retk);
            if (retal == null) {
                System.out.println("retal null");
                continue;
            }
            String[] ret = (String[])retal.get(r[2*i+1]);
            System.out.println(name+"\t"+origin+"\t"+out[0]+"-"+out[1]+"\t$"
                    +out[2]+"\t"+ret[0]+"-"+ret[1]+"\t$"+ret[2]);
        }
    }

    private int getMinutes(String times) {
        // format hh:mm
        StringTokenizer st = new StringTokenizer(times, ":");
        if (st.hasMoreTokens()) {
            int hh = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                int mm = Integer.parseInt(st.nextToken());
                return hh*60+mm;
            }
        }
        return 0;
    }

    public int scheduleCost(int[] sol) {
        int totalprice = 0;
        int latestarrival = 0;
        int earliestdep = 24*60;

        for (int i=0; i<sol.length/2; i++) {
            String origin = people[i][1];
            String outk = origin+"-"+this.destination;
            String retk = this.destination+"-"+origin;
            ArrayList outal = (ArrayList)this.flights.get(outk);
            if (outal == null) continue;
            String[] out = (String[])outal.get(sol[2*i]);
            ArrayList retal = (ArrayList)this.flights.get(retk);
            if (retal == null) continue;
            String[] ret = (String[])retal.get(sol[2*i+1]);

            int outi = Integer.parseInt(out[2]);
            int reti = Integer.parseInt(ret[2]);
            totalprice += (outi+reti);

            int t1 = this.getMinutes(out[1]);
            if (t1 > latestarrival) latestarrival = t1;
            int t2 = this.getMinutes(ret[0]);
            if (t2 < earliestdep) earliestdep = t2;
        }

        int totalwait = 0;
        for (int i=0; i<sol.length/2; i++) {
            String origin = people[i][1];
            String outk = origin+"-"+this.destination;
            String retk = this.destination+"-"+origin;
            ArrayList outal = (ArrayList)this.flights.get(outk);
            if (outal == null) continue;
            String[] out = (String[])outal.get(sol[2*i]);
            ArrayList retal = (ArrayList)this.flights.get(retk);
            if (retal == null) continue;
            String[] ret = (String[])retal.get(sol[2*i+1]);
            int t1 = this.getMinutes(out[1]);
            int t2 = this.getMinutes(ret[0]);

            totalwait += (latestarrival-t1)+(t2-earliestdep);
        }

        if (latestarrival > earliestdep) totalprice += 50;

        return totalprice+totalwait;
    }

    private int[] copy(int[] sol) {
        return sol.clone();
    }

    public int[] hillClimbing() {
        int[] sol = new int[this.people.length*2];
        for (int i=0; i<sol.length/2; i++) {
            sol[i] = (int)(Math.random()*10);
            sol[i+1] = (int)(Math.random()*10);
        }

        while (true) {
            ArrayList neighbors = new ArrayList();
            for (int i=0; i<sol.length/2; i++) {
                int[] copy1 = copy(sol);
                if (sol[i]<9) { copy1[i]++; neighbors.add(copy1); }
                int[] copy2 = copy(sol);
                if (sol[i+1]>0) { copy2[i+1]--; neighbors.add(copy2); }
            }

            int current = scheduleCost(sol);
            int best = current;
            for (Iterator it=neighbors.iterator(); it.hasNext(); ) {
                int[] candidate = (int[])it.next();
                int cost = scheduleCost(candidate);
                if (cost < best) {
                    best = cost;
                    sol = candidate;
                }
            }
            if (best == current) {
                break;
            }
        }
        return sol;
    }

    public void randomRestartHillClimbing() {
        int best = 999999;
        int[] bestsol = null;
        int iteration = 0;
        int bestiter = 0;
        while (true) {
            int[] sol = hillClimbing();
            //this.printSchedule(sol);
            int cost = this.scheduleCost(sol);
            iteration++;
            //System.out.println("Lowest cost = "+cost);
            if (cost < best) {
                best = cost;
                bestsol = sol;
                bestiter = iteration;
                System.out.println("Iteration "+iteration+
                        ", best cost = "+best);
            }
            if (iteration-bestiter >= 4000) {
                System.out.println("Maybe stablized, stop at iteration "+
                        iteration);
                System.out.println("Current best solution:");
                this.printSchedule(bestsol);
                System.out.println("Cost = "+best);
                break;
            }
        }
    }

    public int[] annealingOptimize() {
        double T=10000.0;
        double cool=0.95;
        int step=1;

        int[] vec = new int[this.people.length*2];
        for (int i=0; i<vec.length/2; i++) {
            vec[i] = (int)(Math.random()*10);
            vec[i+1] = (int)(Math.random()*10);
        }

        while (T>0.1) {
            int pos = (int)(Math.random()*10);
            //int dir = (int)(Math.random()*3)-1;  // -1, 0, 1
            int dir = (Math.random()>0.5)? 1: -1;

            int[] vecb = copy(vec);
            vecb[pos] += dir;
            if (vecb[pos]>9) vecb[pos] = 9;
            else if (vecb[pos]<0) vecb[pos] = 0;

            int ea = scheduleCost(vec);
            int eb = scheduleCost(vecb);
            double p = Math.pow(Math.E, (-eb-ea)/T);

            if (eb<ea || Math.random()<p) {
                //if (eb > ea) {
                //    System.out.println("Jump out, cost="+eb+">"+ea+" at T="+T);
                //}
                vec = vecb;
            }

            T *= cool;
        }
        return vec;
    }

    public void randomRestartSimulatedAnnealing() {
        int best = 999999;
        int[] bestsol = null;
        int iteration = 0;
        int bestiter = 0;
        while (true) {
            int[] sol = annealingOptimize();
            //this.printSchedule(sol);
            int cost = this.scheduleCost(sol);
            iteration++;
            //System.out.println("Lowest cost = "+cost);
            if (cost < best) {
                best = cost;
                bestsol = sol;
                bestiter = iteration;
                System.out.println("Iteration "+iteration+
                        ", best cost = "+best);
            }
            if (iteration-bestiter >= 4000) {
                System.out.println("Maybe stablized, stop at iteration "+
                        iteration);
                System.out.println("Current best solution:");
                this.printSchedule(bestsol);
                System.out.println("Cost = "+best);
                break;
            }
        }
    }

    public int[] mutate(int[] r) {
        int step = 1;
        int[] vec = copy(r);
        int i = (int)(Math.random()*vec.length);
        if (Math.random()<0.5 && vec[i]>0)
            vec[i] -= step;
        else if (vec[i] < 9)
            vec[i] += step;
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

    public int[] geneticOptimize() {
        int popsize = 50;
        double elite = 0.2;
        int maxiter = 50;
        double mutprob = 0.2;

        ArrayList pop = new ArrayList();
        for (int i=0; i<popsize; i++) {
            int[] sol = new int[this.people.length*2];
            for (int j=0; j<sol.length/2; j++) {
                sol[j] = (int)(Math.random()*10);
                sol[j+1] = (int)(Math.random()*10);
            }
            pop.add(sol);
        }

        int topelite=(int)(elite*popsize);

        int count=0;
        while (count++ < maxiter) {
            ASObject[] array = new ASObject[pop.size()];
            for (int i=0; i<pop.size(); i++) {
                int[] r = (int[])pop.get(i);
                int cost = this.scheduleCost(r);
                array[i] = new ASObject(i, cost);
            }
            Arrays.sort(array);
            ArrayList pop2 = new ArrayList();
            for (int i=0; i<topelite; i++) {
                pop2.add(pop.get(array[i].label));
            }
            pop = pop2;

            while (pop.size() < popsize) {
                if (Math.random() < mutprob) {
                    int c = (int)(Math.random()*topelite);
                    pop.add(mutate((int[])pop.get(c)));
                }
                else {
                    int c1 = (int)(Math.random()*topelite);
                    int c2 = (int)(Math.random()*topelite);
                    pop.add(crossover((int[])pop.get(c1), (int[])pop.get(c2)));
                }
            }
            //System.out.println("Current best cost = "+
            //        ((ASObject)array[0]).score+" in generation "+count);
        }

        return (int[])pop.get(0);
    }

    public void randomRestartGeneticOptimization() {
        int best = 999999;
        int[] bestsol = null;
        int iteration = 0;
        int bestiter = 0;
        while (true) {
            int[] sol = this.geneticOptimize();
            //this.printSchedule(sol);
            int cost = this.scheduleCost(sol);
            iteration++;
            //System.out.println("Lowest cost = "+cost);
            if (cost < best) {
                best = cost;
                bestsol = sol;
                bestiter = iteration;
                System.out.println("Iteration "+iteration+
                        ", best cost = "+best);
            }
            if (iteration-bestiter >= 1000) {
                System.out.println("Maybe stablized, stop at iteration "+
                        iteration);
                System.out.println("Current best solution:");
                this.printSchedule(bestsol);
                System.out.println("Cost = "+best);
                break;
            }
        }
    }

    public void go() {
        //int[] r = {1,4,3,2,7,3,6,3,2,4,5,3};
        //this.printSchedule(r);
        //int cost = this.scheduleCost(r);
        //System.out.println("Cost = "+cost);
        //this.randomRestartHillClimbing();

        /*int[] r = annealingOptimize();
        this.printSchedule(r);
        int cost = this.scheduleCost(r);
        System.out.println("Cost = "+cost);*/
        //this.randomRestartSimulatedAnnealing();

        /*int[] r = this.geneticOptimize();
        this.printSchedule(r);
        int cost = this.scheduleCost(r);
        System.out.println("Cost = "+cost);*/
        this.randomRestartGeneticOptimization();
    }

}
