import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class BuildTree {
	public static String DATA_DIRECTORY = "/Users/eryue/Documents/11-676/assignment2/";
	public static int FEATURE_NUM = 6;
	
	/**
	 * Use DFS to recursively generate the decision tree
	 * @param level: The level of the tree currently calculating on
	 * @param set: The features that have been generated in parent nodes
	 * @param sub: The left child has value 0, the right child has value 1
	 * @return: The TreeNode generated
	 */
	public static TreeNode genTree(int level, ArrayList<Integer> set, int sub) {
		TreeNode root = null;
		BufferedReader br = null;
		//Get the input filename
		String[] filenames = getFilename(level, set);
		String filename = filenames[sub];
		//If it is the leaf node, just generate the label
		if (set.size() == FEATURE_NUM){
			TreeNode leaf = genLabel(filename);
			return leaf;
		}
		try {
			br = new BufferedReader(new FileReader(DATA_DIRECTORY + filename));
			//Initialize the entropy list. Each feature has a list of probable value count.
			HashMap<Integer, ArrayList<Integer>> entropy= new HashMap<Integer, ArrayList<Integer>>();
			//Store the rest possible features that can be chosen from
			HashSet<Integer> restset = new HashSet<Integer>();
			for (int i = 0; i < FEATURE_NUM; i++) {
				restset.add(i);
			}
			for (int i = 0; i < set.size(); i++) {
				restset.remove(set.get(i));
			}
		    for (int key : restset) {
		    	ArrayList<Integer> list = new ArrayList<Integer>();
		    	for (int i = 0; i< 4; i++) {
		    		list.add(0);
		    	}
		    	entropy.put(key, list);
		    }
		    //Read lines from file and store the counts of different feature-label combination
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] features = line.split(",");
		        for (int i = 0; i < FEATURE_NUM; i++) {
		        	if (restset.contains(i)) {
		        		ArrayList<Integer> list = entropy.get(i);
		        		int x = Integer.parseInt(features[i]);
		        		int y = Integer.parseInt(features[6]);
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
		    String[] outputFiles = getFilename(level + 1, newset);
		    //Split the file into two files with different values for this feature
		    splitFiles(filename, outputFiles, minKey);
		    //Recursively call this function to generate the left child and right child
		    root.left = genTree(level + 1, newset, 0);
		    root.right = genTree(level + 1, newset, 1);
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
			br = new BufferedReader(new FileReader(DATA_DIRECTORY + filename));
		    String line;
		    int count = 0;
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	if (Integer.parseInt(features[FEATURE_NUM]) == 1) {
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
	
	/**
	 * Generate the filename for next level or get the filename of this level
	 * @param level: The current level or the level to generate file for
	 * @param set: The set of features that have been used
	 * @return The filename for left and right children
	 */
	public static String[] getFilename(int level, ArrayList<Integer> set) {
		if (level == 0) {
			String[] files = new String[1];
			files[0] = "train_0";
			return files;
		} else {
			String filename = "train_" + level;
			for (int i = 0; i < set.size(); i++) {
				filename += "_" + set.get(i);
			}
			String[] files = new String[2];
			files[0] = filename + "_1";
			files[1] = filename + "_0";
			return files;
		}
	}
	
	/**
	 * Split the original file into two files which are separated by value of a feature
	 * @param filename: The input filename
	 * @param outputFiles: The filenames of output files
	 * @param minKey: The feature that is split upon
	 */
	public static void splitFiles(String filename, String[] outputFiles, int minKey) {
		PrintWriter out0 = null, out1 = null;
		BufferedReader br = null;
		try {
			out0 = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIRECTORY + outputFiles[0])));
			out1 = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIRECTORY + outputFiles[1])));
			br = new BufferedReader(new FileReader(DATA_DIRECTORY + filename));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	if (Integer.parseInt(features[minKey]) == 0) {
		    		out0.write(line + "\n");
		    	} else {
		    		out1.write(line + "\n");
		    	}
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out0.close();
				out1.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Use test data to test the correctness of the decision tree
	 * @param root: The root node of the decision tree
	 * @return The correctness value
	 */
	public static double testTree(TreeNode root) {
		double correctness = 0.0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(DATA_DIRECTORY + "test"));
		    //Read lines from file
		    String line;
		    int correct = 0;
		    int total = 0;
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	TreeNode node = root;
		    	while (node.value != -1) {
		    		int index = node.value;
		    		if (Integer.parseInt(features[index]) == 0) {
		    			node = node.left;
		    		} else {
		    			node = node.right;
		    		}
		    	}
		    	if (node.label == Integer.parseInt(features[FEATURE_NUM])){
		    		correct++;
		    	}
		    	total++;
		    }
		    correctness = (float)correct / total;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return correctness;
	}
	
	public static void main(String[] args) {
		ArrayList<Integer> set = new ArrayList<Integer>();
		TreeNode root = genTree(0, set, 0);
		ArrayList<LinkedList<TreeNode>> result = listofDepth(root);
		for (int i = 0; i < FEATURE_NUM; i++) {
			LinkedList<TreeNode> list = result.get(i);
			for (int j = 0; j < list.size(); j++) {
				TreeNode node = list.get(j);
				System.out.print(node.value + " ");
			}
			System.out.println();
		}
		double accuracy = testTree(root);
		System.out.println("Accuracy: " + accuracy);
	}
	
	//The below two are used to print the decision tree, which are not main code for this assignment
	public static ArrayList<LinkedList<TreeNode>> listofDepth(TreeNode root) {
		ArrayList<LinkedList<TreeNode>> result = new ArrayList<LinkedList<TreeNode>>();
		LinkedList<TreeNode> list = new LinkedList<TreeNode>();
		list.add(root);
		while (!list.isEmpty()) {
			result.add(list);
			list = gen_list(list);
		}
		return result;
	}
	
	public static LinkedList<TreeNode> gen_list(LinkedList<TreeNode> list) {
		LinkedList<TreeNode> newlist = new LinkedList<TreeNode>();
		for (int i = 0; i < list.size(); i++) {
			TreeNode node = list.get(i);
			TreeNode lnode = node.left;
			TreeNode rnode = node.right;
			if (lnode != null) newlist.add(lnode);
			if (rnode != null) newlist.add(rnode);
		}
		return newlist;
	}

}
