package splaytrees;


/**Node
*
* @author Khalil Stemmler
* October 19th, 2014
* The Node class is used for linked Dynamic Structures. 
*/

public class Node {
	Node left;
	Node right;
	int count;
	String item;

	
	public Node(String item, Node left, Node right){
		this.left = left;
		this.right = right;
		this.item = item;
		count = 1;	//the count of a Node is set to 0 upon creation
	}

}
