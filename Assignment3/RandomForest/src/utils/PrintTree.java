package utils;

import java.util.ArrayList;
import java.util.LinkedList;

import config.Params;
import struct.TreeNode;

public class PrintTree {
	//The below two are used to print the decision tree, which are not main code for this assignment
	public static void printTree(TreeNode root) {
		ArrayList<LinkedList<TreeNode>> result = listofDepth(root);
		for (int i = 0; i < Params.FEATURE_NUM + 1; i++) {
			LinkedList<TreeNode> list = result.get(i);
			for (int j = 0; j < list.size(); j++) {
				TreeNode node = list.get(j);
				int value = 0;
				if (i >= Params.FEATURE_NUM) value = node.label;
				else value = node.value;
				System.out.print(value + " ");
			}
			System.out.println();
		}
	}
	
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
