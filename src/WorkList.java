import java.util.ArrayDeque;


// represents a worklist containing mazetiles
interface IWorkList {
  
  // Adds a tile to the worklist
  void add(MazeTile tile);
  
  // Removes a tile from the worklist and return it
  MazeTile remove();
  
  // Determines whether this WorkList is empty
  boolean isEmpty();
  
  // Returns the number of elements in this worklist
  int size();
}

// Represents a stack of MazeTiles (LIFO)
class MazeStack implements IWorkList {
  
  ArrayDeque<MazeTile> deque;

  MazeStack() {
    this.deque = new ArrayDeque<MazeTile>();
  }
  
  // Adds the given tile to the front of the deque
  public void add(MazeTile tile) {
    this.deque.addFirst(tile);
  }
  
  // Removes and returns the first element of the deque
  public MazeTile remove() {
    return this.deque.removeFirst();
  }
  
  // returns whether this stack is empty
  public boolean isEmpty() {
    return this.deque.isEmpty();
  }
  
  // returns the size of this stack
  public int size() {
    return this.deque.size();
  }

}

//Represents a Queue of MazeTiles (FIFO)
class MazeQueue implements IWorkList {
  
  ArrayDeque<MazeTile> deque;

  MazeQueue() {
    this.deque = new ArrayDeque<MazeTile>();
  }
  
  // Adds the given tile to the front of the deque
  public void add(MazeTile tile) {
    this.deque.addFirst(tile);
  }
  
  // Removes and returns the last element of the deque
  public MazeTile remove() {
    return this.deque.removeLast();
  }
  
  // returns whether this queue is empty
  public boolean isEmpty() {
    return this.deque.isEmpty();
  }
  
  // returns the size of this queue
  public int size() {
    return this.deque.size();
  }
}