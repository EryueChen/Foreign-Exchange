
public class TreeNode {
	int value;
	TreeNode left = null;
	TreeNode right = null;
	int label = -1;
	public TreeNode(int value) { 
		this.value = value;
	}
	public void setLabel(int label) {
		this.label = label;
	}
}
