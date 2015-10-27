package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import config.Params;
import struct.TreeNode;

public class TestDecisionTree {
	/**
	 * Use test data to test the correctness of the decision tree
	 * @param root: The root node of the decision tree
	 * @return The correctness value
	 */
	public static double testTree(TreeNode root) {
		double correctness = 0.0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(Params.DATA_DIRECTORY + "test"));
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
		    	if (node.label == Integer.parseInt(features[Params.FEATURE_NUM])){
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
}
