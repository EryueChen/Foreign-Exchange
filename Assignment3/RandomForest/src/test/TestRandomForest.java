package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import config.Params;
import decisiontree.BuildTree;
import struct.TreeNode;
import utils.PrintTree;
import utils.RandomUtils;

public class TestRandomForest {
	/**
	 * Test the random forest.
	 * @return The accuracy of the random forest
	 */
	public static double testRandomForest() {
		//Build all the trees in the forest
		ArrayList<TreeNode> roots = new ArrayList<TreeNode>();
		for (int i = 0; i < Params.TREE_NUM; i++) {
			ArrayList<Integer> feature_set = RandomUtils.randomSet();
			ArrayList<Integer> set = new ArrayList<Integer>();
			for (int j = 0; j < Params.FEATURE_NUM; j++) {
				System.out.print(feature_set.get(j) + " ");
			}
			System.out.println();
			TreeNode root = BuildTree.genTree(0, set, feature_set, 0);
			roots.add(root);
			PrintTree.printTree(root);
		}
		double correctness = 0.0;
		//Read the test file and gather the result of each tree and compare with actual result
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(Params.DATA_DIRECTORY + "test"));
		    String line;
		    int correct = 0;
		    int total = 0;
		    int[] corrects = new int[Params.TREE_NUM];
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	int votes = 0;
		    	int label = 0;
		    	for (int i = 0; i < Params.TREE_NUM; i++) {
		    		TreeNode node = roots.get(i);
		    		while (node.value != -1) {
		    			int index = node.value;
		    			if (Integer.parseInt(features[index]) == 0) {
		    				node = node.left;
		    			} else {
		    				node = node.right;
		    			}
		    		}
		    		if (node.label == 1) votes++;
		    		else votes--;
		    		if (node.label == Integer.parseInt(features[Params.FEATURE_TOTAL])) corrects[i]++;
		    	}
		    	if (votes > 0) label = 1;
		    	if (label == Integer.parseInt(features[Params.FEATURE_TOTAL])){
	    			correct++;
	    		}
		    	total++;
		    }
		    for (int i = 0; i < Params.TREE_NUM; i++) {
		    	System.out.println("Acurracy of Tree " + i + ": " + (float)corrects[i] / total);
		    }
		    correctness = (float) correct / total;
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
