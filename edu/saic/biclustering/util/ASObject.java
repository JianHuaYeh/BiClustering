package edu.saic.biclustering.util;

public class ASObject implements Comparable<Object> {
    public int label;
    public double score;
    public double score2;

    public ASObject(int l, double s, double s2) {
        this.label = l;
        this.score = s; // 1.0/dim
        this.score2 = s2; // fitness
    }

    public int compareTo(Object other) {
        ASObject obj = (ASObject)other;
        if (this.score < obj.score) return -1;
        else if (this.score > obj.score) return 1;
       	else {
       		if (this.score2 < obj.score2) return -1;
       		else if (this.score2 > obj.score2) return 1;
       		return 0;
       	}
    }
    
    /*public int compareTo(Object other) {
        ASObject obj = (ASObject)other;
        if (this.score < obj.score) return -1;
        else if (this.score > obj.score) return 1;
       	else return 0;
    }*/
}
