package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import config.Params;
import struct.TreeNode;
import utils.TreeUtils;

public class Test {
	/* Build forest from the output of mapreduce */
	public static ArrayList<TreeNode> buildForest() {
		ArrayList<TreeNode> forest = new ArrayList<TreeNode>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader("/trees"));
			while ((line = br.readLine()) != null) {
				TreeNode root = TreeUtils.deserialize(line);
				forest.add(root);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return forest;
	}
	
	
	public static void main(String[] args) {
		ArrayList<TreeNode> forest = buildForest();
		double correctness = 0.0;
		//Read the test file and gather the result of each tree and compare with actual result
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/test"));
		    String line;
		    int correct = 0;
		    int total = 0;
		    int[] corrects = new int[forest.size()];
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	int votes = 0;
		    	int label = 0;
		    	for (int i = 0; i < forest.size(); i++) {
		    		TreeNode node = forest.get(i);
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
		    for (int i = 0; i < forest.size(); i++) {
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
		System.out.println("Acurracy of forest: " + correctness + "\n");
	}

}
