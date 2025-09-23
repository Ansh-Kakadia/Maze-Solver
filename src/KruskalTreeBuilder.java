import java.util.ArrayList;
import java.util.HashMap;

// A Class that determines which blocks are connected in the maze using Kruskal's algorithm
class KruskalTreeBuilder {
  HashMap<MazeTile, MazeTile> hash;
  ArrayList<Edge> edges;
  
  // takes in a hashmap of tiles, and a Arraylist of edges, sorted by weight ascending
  KruskalTreeBuilder(HashMap<MazeTile, MazeTile> hash, ArrayList<Edge> edges) {
    this.hash = hash;
    this.edges = edges;
  }
  
  // Creates the tree, or list of edges
  ArrayList<Edge> createTree() {
    
    ArrayList<Edge> tree = new ArrayList<Edge>();
    int numTiles = hash.size();
    
    // iterates through the edges and adds a edge to the tree if 
    // it's tiles are not already connected
    // when adding, updates the hashmap
    for (Edge edge : this.edges) {
      if (!edge.isConnected(hash)) {
        edge.join(hash);
        tree.add(edge);
      }
      
      if (tree.size() >= numTiles - 1) {
        return tree;
      }
    }
    return tree;
  }
  
}
