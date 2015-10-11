package decisiontree;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import config.Params;
import struct.TreeNode;
import utils.FileUtils;

public class BuildTree {
	
	/**
	 * Use DFS to recursively generate the decision tree
	 * @param level: The level of the tree currently calculating on
	 * @param set: The features that have been generated in parent nodes
	 * @param sub: The left child has value 0, the right child has value 1
	 * @return: The TreeNode generated
	 */
	public static TreeNode genTree(int level, ArrayList<Integer> set, ArrayList<Integer> all, int sub) {
		TreeNode root = null;
		BufferedReader br = null;
		//Get the input filename
		String[] filenames = FileUtils.getFilename(level, set);
		String filename = filenames[sub];
		//If it is the leaf node, just generate the label
		if (set.size() == Params.FEATURE_NUM){
			TreeNode leaf = genLabel(filename);
			return leaf;
		}
		try {
			br = new BufferedReader(new FileReader(Params.DATA_DIRECTORY + filename));
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
		    //Read lines from file and store the counts of different feature-label combination
		    String line;
		    while ((line = br.readLine()) != null) {
		    	Random random = new Random();
				int r = random.nextInt(3);
				if (r % 3 == 0) continue;
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
		    //System.out.println(minKey);
		    //Generate a node with the feature of largest entropy
		    root = new TreeNode(minKey);
		    ArrayList<Integer> newset = new ArrayList<Integer>();
		    newset.addAll(set);
		    newset.add(minKey);
		    //Generate the file names of output files
		    String[] outputFiles = FileUtils.getFilename(level + 1, newset);
		    //Split the file into two files with different values for this feature
		    FileUtils.splitFiles(filename, outputFiles, minKey);
		    //Recursively call this function to generate the left child and right child
		    root.left = genTree(level + 1, newset, all, 0);
		    root.right = genTree(level + 1, newset, all, 1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return root;
	}
	
	/**
	 * Generate the label for each leaf node
	 * @param filename: The filename corresponding to the file storing the data for that leaf node
	 * @return Generate a TreeNode with only label value as leaf node
	 */
	public static TreeNode genLabel(String filename) {
		BufferedReader br = null;
		TreeNode node = new TreeNode(-1);
		try {
			//Traverse the file, if it has more 1 than 0 as label, return 1. Otherwise, return 0.
			br = new BufferedReader(new FileReader(Params.DATA_DIRECTORY + filename));
		    String line;
		    int count = 0;
		    while ((line = br.readLine()) != null) {
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
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return node;
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
