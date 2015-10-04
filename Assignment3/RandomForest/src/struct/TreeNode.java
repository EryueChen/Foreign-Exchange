package struct;

public class TreeNode {
	public int value;
	public TreeNode left = null;
	public TreeNode right = null;
	public int label = -1;
	
	public TreeNode(int value) { 
		this.value = value;
	}
	public void setLabel(int label) {
		this.label = label;
	}
}
