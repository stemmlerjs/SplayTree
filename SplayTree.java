package splaytrees;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;

import BasicIO.ASCIIDataFile;

/**SplayTree
* @author Khalil Stemmler
* November 6th, 2014
* This class is a top down splay tree implementation. With the use of a Queue object to keep track of the last 2/3 items visited, splay 
* operations are performed on the BST data set in a top down fashion as is natural with a Queue's first in first out implementation. In this
* implementation, the splay tree's validity is tested with the insertion of a data set and the removal of items starting from r to t (upper and lowercase)
*/

public class SplayTree {
	//INSTANCE VARIABLES
	Node tree;
	ASCIIDataFile in;
	Queue<Node> q = new LinkedList<Node>(); // the Queue will be used to keep track of the last 2/3 items to perform splay operations on.
	char [] del;							// this char array will be populated with the first letter to denote items to delete
	Queue<String> toDelete; 				// this queue will be populated with items to delete within the tree
	
	//CONSTRUCTOR
	public SplayTree(){

		//Initialize Tree and get input
		tree = null;
		in = new ASCIIDataFile();
		
		//Add elements to the tree
		while(!in.isEOF()){
			String text = parseText(in.readString()); 
			if(in.isEOF()) break;			
			StringTokenizer token = new StringTokenizer(text);
			while(token.hasMoreTokens()){
				// Insert Elements into the Tree
				tree = insert((String)token.nextToken());	  
			}
		}	

		// ============================================= //
		// ============= TEST HARNESS ================== //
		// ============================================= //

		System.out.println("BEFORE: IN ORDER TRAVERSAL OUTPUT");
		inOrderTraversal(tree);
		System.out.println("");
		
		//Delete Every Item starting with d-n and D-N
		del = new char[]{'r', 'R', 's', 'S', 't', 'T'};
		
		//Obtain a list of items to delete
		toDelete = new LinkedList<String>();
		obtainDeleteables(tree);
		
		//Delete all the items added in the Deletion Queue toDelete
		while(!toDelete.isEmpty()){
			delete(toDelete.remove());
		}
		
		System.out.println("");
		
		System.out.println("AFTER: IN ORDER TRAVERSAL OUTPUT");
		inOrderTraversal(tree);
		System.out.println("");
		
	}
	
	/** This method is used to remove an item from the splay tree.
     ** @param String 	the item for the Node to delete
     ** @param Node 	the root Node
     ** @return Node 	the current Node
     **/
	
	private void delete(String x) {
		find(x); //firstly, we splay the target Node to the root
		Node root = tree;
		if(root.item.compareTo(x) == 0){ 			//The item exists

			if(root.count > 1){ //this is the case that the item has multiple occurrences, just decrease the count. Dont delete.
				root.count--; 
			} else { //there is only 1 occurrence of the item; therefore delete it
				//Delete procedure
				Node successor = getSuccessor(root); //we will promote the successor
				if(successor == null){ //Means there is no successor, we just delete the leaf Node, then we promote the predecessor
					Node predecessor = getPredecessor(root);
					root = predecessor;
				} else { //we found the successor, now we have rearrange the sublist to the right of the root.
					//System.out.println("The successor is " + successor.item + " and it has " + successor.count + " occurrences");
					Node subList = root.right;
					subList = findIn(successor.item,subList); //we wish to find the successor in the sublist
					//We Copy everything EXCEPT the left pointer
					root.item = subList.item;
					root.right = subList.right;
					root.count = subList.count;
				}
				//System.out.println("the new root is " + root.item);
				Node t = root.right;
			}
		}
		this.tree = root;
	}
	
	/** This method brings the target node containing the string x item to the very top of the list.
     ** @param String 		the target element to bring to the top of the list
     **/

	private void find(String x) {
		Node t = tree;
		if(tree != null){
			//If there is already a root Node
			Node leafNode = null; //used to keep a reference to the leaf Node to add the last item to
			A: while(t != null){
				//System.out.println("Looking at " + t.item);
				q.add(t);
				leafNode = t;
				if(x.compareTo(t.item) < 0){
					t = t.left;
				} else if(x.compareTo(t.item) > 0){
					t = t.right;
				} else { //found OR not found
					break A;
				}
				
				//if 3 moves down, we will splay (t must still be at the same pointer that it was at before.
				if(q.size() == 3){
					t = splay();
					q.clear();
					//System.out.println("After splay: " + t.item);
				}
			}
			this.tree = splay(); //splay again, this will fix the two Nodes case
		}
		q.clear(); //reset the queue
	}
	
	/** This method brings the target node containing the string x item to the very top of the list. findIn() differs from find() because it allows us to specify
	 ** an entry point to begin looking for the target Node.
     ** @param String 		the target element to bring to the top of the list
     ** @param Node 		the specified entry point to begin searching
     **/
	
	private Node findIn(String x, Node root) {
		Node t = root;
		if(root != null){
			Node leafNode = null; // keep a reference to the leaf Node to add the last item to
			A: while(t != null){
				q.add(t);
				leafNode = t;
				if(x.compareTo(t.item) < 0){
					t = t.left;
				} else if(x.compareTo(t.item) > 0){
					t = t.right;
				} else { //found
					break A;
				}
				
				//if 3 moves down, we will splay (t must still be at the same pointer that it was at before.
				if(q.size() == 3){
					t = splay();
					q.clear();
				}
			}
			root = splay(); //splay again, this will fix the two Nodes case
		}
		q.clear();
		
		return root;
	}
	
	/** This method creates a new Node in the splay tree. It performs splay() operations on its way down the tree to find the entry point.
     ** @param String 		element to add to the splay tree
     **/

	public Node insert(String x){
		Stack<Node> s = new Stack<Node>();
		Boolean duplicate = false;
		Node t = tree;
		Node toLeaf = null;

		//No root Node exists
		if(t == null){
			//System.out.println("Added " + x);
			t = new Node(x, null, null);
			return t;
		} else {
			//If there is already a root Node
			Node leafNode = null; //used to keep a reference to the leaf Node to add the last item to
			A: while(t != null){
				q.add(t);
				s.push(t);
				leafNode = t;
				if(x.compareTo(t.item) < 0){
					t = t.left;
				} else if(x.compareTo(t.item) > 0){
					t = t.right;
				} else {
					duplicate = true;
					break A;
				}
				
				//if 3 moves down, we will splay (t must still be at the same pointer that it was at before.
				if(q.size() == 3){
					t = splay();
					q.clear();
				}
			}

			if(duplicate){
				leafNode.count = leafNode.count + 1;
			} else {
				//Add the leaf Node onto the tree, update the queue
					toLeaf = new Node(x, null, null);
				if(x.compareTo(leafNode.item) < 0){
					leafNode.left = toLeaf;
				} else if(x.compareTo(leafNode.item) > 0){
					leafNode.right = toLeaf;
				}
				updateQueue();
				q.add(leafNode);
				q.add(toLeaf); s.push(toLeaf);
			}
				
			//splay again, this will fix the two Nodes case
			toLeaf = splay(); 
		}

		q.clear();
		return toLeaf;
}
	
	/** This method removes the items from the public Queue object so that the new updated Nodes can be added
     **/
	
	private void updateQueue(){
		Queue<Node> newQueue = new LinkedList<Node>();
		int queueSize = q.size();
		for(int i = 1; i < queueSize; i++){
			newQueue.add(q.remove());
		}
		q = newQueue;
	}
	
   /** This splay method performs the splay operation that is required when 3 immediate nodes have been added into the queue OR when 2 Nodes are left in the queue.
	 * There are 6 different splay operations that can occur based on the layout of the 2 or 3 nodes that have been removed from the queue. The Nodes grandparent,
	 * parent, and child denote the removed items from the queue. The hierarchy of their relationship is from grandparent to parent to child; the logic structure to
	 * choose which rotation to do is based on this.
    ** @see getSuccessor
    ** @see getHeight
    ** @see simpleLeft
    ** @see singleRight
    ** @see simpleRotateLeftToRight
    ** @see simpleRotateRightToLeft
    ** @see doubleRotateLeftToRight
    ** @see doubleRotateRightToLeft
    */
	
	private Node splay() {
		Node grandParent = null;
		Node parent = null;
		Node child = null;
		
		//Remove all of the elements from the Queue
		if(!q.isEmpty()) grandParent = q.remove();
		if(!q.isEmpty()) parent = q.remove();
		if(!q.isEmpty()) child = q.remove();
		
		//Operations
		if((grandParent != null) && (parent != null) && (child == null)){ //when q == 2
			if(grandParent.left == parent){ //the tree is left heavy
				//System.out.println("SimpleRight");
				grandParent = simpleRight(grandParent);
			} else {						//the tree is right heavy
				//System.out.println("SimpleLeft");
				grandParent = simpleLeft(grandParent);
			}
		} else if ((grandParent != null) && (parent != null) && (child != null)){ //when q == 3
			if(grandParent.left == parent){
				if(parent.left == child){
					//System.out.println("SimpleLeftToRight");
					grandParent = simpleRotateLeftToRight(grandParent);
				} else { //parent.right == child
					grandParent = doubleRotateRightToLeft(grandParent);
				}
			} else { //grandparent.right == parent
				if(parent.left == child){
					grandParent = doubleRotateLeftToRight(grandParent);
				} else { //parent.right == child
				//	System.out.println("SimpleRightToLeft");
					grandParent = simpleRotateRightToLeft(grandParent);
				}
			}
		}
		//System.out.println("New root is " + grandParent.item);
		return grandParent;
	}
	
	/** This is a simpleRotateRightToLeft rotation, this occurs when we have 3 Nodes in a full diagonal right heavy relationship. 
	 * 	After rotations, the child Node is returned as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/
	
	private Node simpleRotateRightToLeft(Node grandparent) {
		Node parent = grandparent.right;
		Node child = parent.right;
		
		parent.right = child.left;
		child.left = parent;
		grandparent.right = parent.left;
		parent.left = grandparent;
		return child;
	}
	
	/** This is a simpleRotateLefttoRight rotation, this occurs when we have 3 Nodes in a full diagonal left heavy relationship. 
	 * 	After rotatons, the child is returned as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/

	private Node simpleRotateLeftToRight(Node grandparent){
		Node parent = grandparent.left;
		Node child = parent.left;
		
		parent.left = child.right;
		child.right = parent;
		grandparent.left = parent.right;
		parent.right = grandparent;
		return child;
	}
	
	/** This is a doubleRotateRightToLeft rotation, this occurs when we have 3 Nodes and the parentNode is right heavy.
	 ** After rotations, the child is returned as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/
	
	private Node doubleRotateRightToLeft(Node grandparent) {
		Node parent = grandparent.left;
		Node child = parent.right;

		grandparent.left = child.right;
		child.right = grandparent;
		parent.right = child.left;
		child.left = parent;

		return child;
	}
	
	/** This is a doubleRotateLefttoRight rotation, this occurs when we have 3 Nodes and the parentNode is right heavy.
	 ** After rotations, the child is returned as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/
	
	private Node doubleRotateLeftToRight(Node grandparent) {
		Node parent = grandparent.right;
		Node child = parent.left;

		grandparent.right = child.left;
		child.left = grandparent;
		parent.left = child.right;
		child.right = parent;

		return child;
	}
	
	/** This is a simpleRight rotation, this occurs when we have two Nodes. It simply shifts the leftNode above the right Node. Returns the lowerNode as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/
	
	private Node simpleRight(Node parent) {
		Node upperNode = parent;
		Node lowerNode = parent.left;
		Node payload;
		if(lowerNode.right == null){
			payload = null;
		} else {
			payload = lowerNode.right;
		}
				
		upperNode.left = payload;
		lowerNode.right = upperNode;
		return lowerNode;
	}

	/** This is a simpleLeft rotation, this occurs when we have two Nodes. It simply shifts the leftNode below the right Node. Returns the lowerNode as the root.
	 ** @param Node		the current Node
	 ** @return Node	the updated Root Node
     **/
	
	private Node simpleLeft(Node parent) {
		Node upperNode = parent;
		Node lowerNode = parent.right;
		Node payload;
		
		//Low Node Payload
		if(lowerNode.left == null){
			payload = null;
		} else {
			payload = lowerNode.left;
		}
			
		upperNode.right = payload;
		lowerNode.left = upperNode;
		return lowerNode;
	}
	
	/** This method is used to obtain the items that need to be deleted within the tree. The items that are to be deleted are allocated within a queue for deletion at a 
	 ** later point. Traversal to locate Nodes follows the LVR In Order algorithm structure.
     ** @param Node 	the root Node
     **/
	
	public void obtainDeleteables(Node currentNode){
		if(currentNode.left != null){
			obtainDeleteables(currentNode.left);
		}   
		Search: for(int i = 0; i < del.length; i++){
			if(currentNode.item.charAt(0) == del[i]){
			//	System.out.println("Adding to delete queue: " + currentNode.item);
				toDelete.add(currentNode.item);
				break Search;
			}
		}	
		if(currentNode.right != null){
			obtainDeleteables(currentNode.right);
		}
	}
	
	/** This method is used to print out all the elements of a queue. The elements within the original queue are maintained.
     ** @param Queue 	the queue to print out
     **/
	
	public void printQueue(Queue<Node> q){
		while(!q.isEmpty()){
			System.out.println(q.remove().item);
		}
	}
	
	/** Only used for the deletion of Nodes
	 ** @param Node		the current Node
	 ** @return Node	the successor of the currentNode
     **/
	
	public Node getSuccessor(Node currentNode){
		if(currentNode.right != null){ //means a successor will be promoted
			currentNode = currentNode.right;
		} else { //there is no successor
			return null;
		}
		while(currentNode.left != null){
			currentNode = currentNode.left;
		}
		return currentNode;
	}
	
	/** Only used for the deletion of Nodes
	 ** @param Node		the current Node
	 ** @return Node	the predecessor of the currentNode
     **/
	
	public Node getPredecessor(Node currentNode){
		if(currentNode.left != null){ //means a predecessor will be promoted
			currentNode = currentNode.left;
		} else { //there is no predecessor
			return null;
		}
		while(currentNode.right != null){
			currentNode = currentNode.right;
		}
		return currentNode;
	}
	
	/** This method performs a standard BST LVR In Order Traversal (SOT) using recursion.
	 ** @param Node		the currentNode
     **/
	
	public void inOrderTraversal(Node currentNode){
		if(currentNode.left != null){
			inOrderTraversal(currentNode.left);
		}   System.out.println(currentNode.item + " " + currentNode.count); 
		if(currentNode.right != null){
			inOrderTraversal(currentNode.right);
		}
	}

	/** This method removes unwanted characters from the String data through the use of the String .replace() method and returns a new String value with
	 ** "good" parsed String data.
	 ** @param String 		the data to be parsed of unwanted characters
	 ** @return String 		the newly parsed String data
     ** @see LinkStack
     **/
	
	public String parseText(String data){
		String newData = data.replace('-', ' ').replace(';', ' ').replace('{',' ').replace('}',' ').replace('/',' ')
				.replace(',', ' ').replace(')', ' ').replace('(', ' ').replace('.', ' ').replace('\'',' ').replace('*',' ');
		return newData;
	}
	
	public static void main(String args[]){
		new SplayTree();
	}
	

}
