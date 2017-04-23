package edu.saic.biclustering.util;

import java.util.ArrayList;

public class Permutation {
	//private static ArrayList<ArrayList<Integer>> result;
	
	public static void permutation(String str) {
	    permutation("", str); 
	}

	private static void permutation(String prefix, String str) {
	    int n = str.length();
	    if (n == 0) {
	    	System.out.println(prefix);
	    }
	    else {
	        for (int i = 0; i < n; i++)
	            permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n));
	    }
	}
	
	//public static ArrayList<ArrayList<Integer>> permutation(ArrayList<Integer> list) {
	public static void permutation(ArrayList<Integer> list) {
		//result = new ArrayList<ArrayList<Integer>>();
	    permutation(new ArrayList<Integer>(), list);
	    //return result;
	}
	
	public static void doOutput(ArrayList<Integer> prefix) {
		for (int i=0; i<prefix.size()-1; i++) {
			System.out.print(prefix.get(i));
			System.out.print(",");
		}
		System.out.println(prefix.get(prefix.size()-1));
	}

	private static void permutation(ArrayList<Integer> prefix, ArrayList<Integer> list) {
	    int n = list.size();
	    if (n == 0) {
	    	//System.out.println(prefix);
	    	//result.add(prefix);
	    	doOutput(prefix);
	    }
	    else {
	        for (int i=0; i<n; i++) {
	            //permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n));
	        	ArrayList<Integer> prefix2 = (ArrayList<Integer>)prefix.clone();
	        	prefix2.add(list.get(i));
	        	ArrayList<Integer> sublist = new ArrayList<Integer>();
	        	sublist.addAll(list.subList(0, i));
	        	sublist.addAll(list.subList(i+1, n));
	        	permutation(prefix2, sublist);
	        }
	    }
	}
	
	//public static ArrayList<ArrayList<Integer>> generatePermutation(int len) {
	public static void generatePermutation(int len) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i=0; i<len; i++) {
			list.add(i);
		}
		permutation(list);
		//return permutation(list);
	}
	
	public static void main(String[] args) {
		//ArrayList<ArrayList<Integer>> r = generatePermutation(3);
		//generatePermutation(4);
		//System.out.println("Total "+r.size()+" permutations.");
		try {
			int i = Integer.parseInt(args[0]);
			generatePermutation(i);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
