package decisiontree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import config.Params;
import struct.TreeNode;

public class BuildTree {
	
	/**
	 * Use DFS to recursively generate the decision tree
	 * @param level: The level of the tree currently calculating on
	 * @param set: The features that have been generated in parent nodes
	 * @param sub: The left child has value 0, the right child has value 1
	 * @return: The TreeNode generated
	 */
	public static TreeNode genTree(int level, ArrayList<Integer> set, ArrayList<Integer> all, String data) {
		TreeNode root = null;
		//If it is the leaf node, just generate the label
		if (set.size() == Params.FEATURE_NUM){
			TreeNode leaf = genLabel(data);
			return leaf;
		}
		//Initialize the entropy list. Each feature has a list of probable value count.
		HashMap<Integer, ArrayList<Integer>> entropy= new HashMap<Integer, ArrayList<Integer>>();
		//Store the rest possible features that can be chosen from
		HashSet<Integer> restset = new HashSet<Integer>();
		for (int i = 0; i < all.size(); i++) {
			restset.add(all.get(i));
		}
		for (int i = 0; i < set.size(); i++) {
			restset.remove(set.get(i));
		}

		for (int key : restset) {
	    	ArrayList<Integer> list = new ArrayList<Integer>();
	    	for (int i = 0; i < 4; i++) {
	    		list.add(0);
		    }
		   	entropy.put(key, list);
		}
		for (String line : data.split("\n")) {
			String[] features = line.split(",");
		    for (int i = 0; i < all.size(); i++) {
		        if (restset.contains(all.get(i))) {
		       		ArrayList<Integer> list = entropy.get(all.get(i));
		       		int x = Integer.parseInt(features[all.get(i)]);
		       		int y = Integer.parseInt(features[Params.FEATURE_TOTAL]);
		       		if (x == 0 && y == 0) {
		       			int count = list.get(0);
		       			list.set(0, count + 1);
		       		} else if (x == 1 && y == 0) {
		       			int count = list.get(1);
	        			list.set(1, count + 1);
	        		} else if (x == 0 && y == 1) {
	        			int count = list.get(2);
		        		list.set(2, count + 1);
		        	} else if (x == 1 && y == 1) {
		        		int count = list.get(3);
		       			list.set(3, count + 1);
		       		}
		       	}   
		    }
		}
		//Iterate entropy list and calculate H(Y|X)
	    Iterator it = entropy.entrySet().iterator();
	    HashMap<Integer, Double> result = new HashMap<Integer, Double>();
	    while (it.hasNext()) {
	    	Map.Entry pair = (Map.Entry)it.next();
		    ArrayList<Integer> array = (ArrayList<Integer>) pair.getValue();
		    double IG = calculateIG(array);
		    result.put((Integer) pair.getKey(), IG);
	    }
		    //Find the smallest H(Y|X), which is also the largest entropy
		it = result.entrySet().iterator();
		int minKey = 0;
		double minValue = Double.MAX_VALUE;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
		   	if ((Double)pair.getValue() < minValue) {
		   		minValue = (Double) pair.getValue();
		   		minKey = (Integer) pair.getKey();
		   	}
	    }
	   	//Generate a node with the feature of largest entropy
	    root = new TreeNode(minKey);
	    ArrayList<Integer> newset = new ArrayList<Integer>();
	    newset.addAll(set);
	    newset.add(minKey);
	    
	    //Split the file into two files with different values for this feature
	    String[] newdata = splitdata(data, minKey);
	    //Recursively call this function to generate the left child and right child
	    root.left = genTree(level + 1, newset, all, newdata[0]);
	    root.right = genTree(level + 1, newset, all, newdata[1]);

		return root;
	}
	
	/**
	 * Generate the label for each leaf node
	 * @param filename: The filename corresponding to the file storing the data for that leaf node
	 * @return Generate a TreeNode with only label value as leaf node
	 */
	public static TreeNode genLabel(String data) {
		TreeNode node = new TreeNode(-1);
		int count = 0;
		for (String line : data.split("\n")) {
		    String[] features = line.split(",");
		   	if (Integer.parseInt(features[Params.FEATURE_TOTAL]) == 1) {
		   		count++;
		   	} else {
		   		count--;
	    	}
	    }
	    if (count > 0) {
		    node.setLabel(1);
		} else {
	    	node.setLabel(0);
	    }
		return node;
	}
	
	public static String[] splitdata(String data, int minKey) {
		String[] result = new String[2];
	    for (String line : data.split("\n")) {
	    	String[] features = line.split(",");
	    	if (Integer.parseInt(features[minKey]) == 0) {
	    		result[0] += line + "\n";
	    	} else {
	    		result[1] += line + "\n";
	    	}
	    }
	    return result;
	}
	
	/**
	 * Calculate the IG for each feature
	 * @param array: The array of the counts of possible combination of feature and label
	 * @return The H(Y|X) value of a feature
	 */
	public static double calculateIG(ArrayList<Integer> array) {
		double sum = array.get(0) + array.get(1) + array.get(2) + array.get(3);
		//P(x = 0)
        double Px0 = (double) (array.get(0) + array.get(2)) / sum;
        //P(x = 1)
        double Px1 = (double) (array.get(1) + array.get(3)) / sum;
        //P(y = 1 | x = 0)
        double Py1x0 = (double) array.get(2) / (array.get(0) + array.get(2));
        //P(y = 0 | x = 0)
        double Py0x0 = 1 - Py1x0;
        //P(y = 1 | x = 1)
        double Py1x1 = (double) array.get(3) / (array.get(1) + array.get(3));
        //P(y = 0 | x = 1)
        double Py0x1 = 1 - Py1x1;
        double IG = Px0 * (-Py1x0 * Math.log(Py1x0) - Py0x0 * Math.log(Py0x0)) + Px1 * (-Py1x1 * Math.log(Py1x1) - Py0x1 * Math.log(Py0x1));
        return IG;
	}
}
