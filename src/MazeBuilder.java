import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// A class that is used to build maze of a given width and height
class MazeBuilder {

  ArrayList<ArrayList<MazeTile>> buildMaze(int width, int height) {

    ArrayList<ArrayList<MazeTile>> grid = this.buildGrid(width, height);

    ArrayList<Edge> sortedEdges = this.buildEdges(grid);

    HashMap<MazeTile, MazeTile> connections = this.buildInitialHash(grid);

    KruskalTreeBuilder treeBuilder = new KruskalTreeBuilder(connections, sortedEdges);

    ArrayList<Edge> tree = treeBuilder.createTree();

    // For each edge in the tree, connects the two MazeTiles the edge represents
    for (Edge edge : tree) {
      edge.connect();
    }
    
    grid.get(0).get(0).initializeDistanceToStart();
    grid.get(width - 1).get(height - 1).initializeDistanceToEnd();

    return grid;
  }

  // builds a 2d arraylist of Maze tiles with size based on the given width and height
  ArrayList<ArrayList<MazeTile>> buildGrid(int width, int height) {
    ArrayList<ArrayList<MazeTile>> grid = new ArrayList<ArrayList<MazeTile>>();

    for (int x = 0; x < width; x += 1) {
      ArrayList<MazeTile> column = new ArrayList<MazeTile>();

      for (int y = 0; y < height; y += 1) {
        MazeTile tile = new MazeTile(x, y);
        column.add(tile);
      }

      grid.add(column);
    }

    return grid;

  }

  // Creates an edge between all adjacent mazeTiles with a random weight between 0
  // and 100.
  // Returns all edges as a arraylist of edges that is sorted by weight
  // (ascending)
  ArrayList<Edge> buildEdges(ArrayList<ArrayList<MazeTile>> grid) {
    ArrayList<Edge> edges = new ArrayList<Edge>();

    int width = grid.size();

    int height;
    if (width == 0) {
      height = 0;
    }
    else {
      height = grid.get(0).size();
    }

    for (int x = 0; x < width; x += 1) {

      for (int y = 0; y < height; y += 1) {

        if (y != height - 1) {
          edges
              .add(new Edge(grid.get(x).get(y), grid.get(x).get(y + 1), new Random().nextInt(100)));
        }

        if (x != width - 1) {
          edges
              .add(new Edge(grid.get(x).get(y), grid.get(x + 1).get(y), new Random().nextInt(100)));
        }
      }
    }

    edges.sort(new BiggerWeight());

    return edges;
  }

  // builds a hashtable with each tile mapped to itself
  HashMap<MazeTile, MazeTile> buildInitialHash(ArrayList<ArrayList<MazeTile>> tiles) {
    HashMap<MazeTile, MazeTile> hash = new HashMap<MazeTile, MazeTile>();

    for (ArrayList<MazeTile> column : tiles) {

      for (MazeTile tile : column) {
        hash.put(tile, tile);
      }
    }
    return hash;
  }

}

// Represents a tile in a maze
class MazeTile {
  private int x;
  private int y;
  private ArrayList<MazeTile> connections;
  private int distanceToEnd;
  private int distanceToStart;

  MazeTile(int x, int y) {
    this.x = x;
    this.y = y;
    this.connections = new ArrayList<MazeTile>();
  }

  // Connects this mazetile to the given one
  void addConnection(MazeTile tile) {
    this.connections.add(tile);
  }
  
  // checks if this mazetile is connected to one at the given positon
  boolean isConnected(int x, int y) {
    
    // checks if a tile in connections is at x, y
    for (MazeTile tile : this.connections) {
      if (tile.isAt(x, y)) {
        return true;
      }
    }
    return false;
  }
  
  // To be called on end point of the maze, initializes the distances of all points to the end
  void initializeDistanceToEnd() {
    this.distanceToEnd = 0;
    
    // initialized the distance to end of each tile this is connected to
    for (MazeTile tile : this.connections) {
      tile.initializeDistanceToEnd(0, this);
    }
  }
  
  // Given the distance of the mazetile that called this method from the end
  // and that maze tile, initialized the distanceToEnd field for all maze
  // tiles on this path
  void initializeDistanceToEnd(int distanceSoFar, MazeTile from) {
    this.distanceToEnd = distanceSoFar + 1;
    
 // initialized the distance to end of each tile this is connected to
    for (MazeTile tile : this.connections) {
      if (tile != from) {
        tile.initializeDistanceToEnd(this.distanceToEnd, this);
      }
    }
  }  
  
  // To be called on start point of the maze, initializes the distances of all points to the start
  void initializeDistanceToStart() {
    this.distanceToStart = 0;

    // initialized the distance to start of each tile this is connected to
    for (MazeTile tile : this.connections) {
      tile.initializeDistanceToStart(0, this);
    }
  }

  // Given the distance of the mazetile that called this method from the start
  // and that maze tile, initialized the distanceToStart field for all maze
  // tiles on this path
  void initializeDistanceToStart(int distanceSoFar, MazeTile from) {
    this.distanceToStart = distanceSoFar + 1;

    // initialized the distance to start of each tile this is connected to
    // except the tile that called this one
    for (MazeTile tile : this.connections) {
      if (tile != from) {
        tile.initializeDistanceToStart(this.distanceToStart, this);
      }
    }
  }
  
  
  // returns whether this tile is at the given position
  boolean isAt(int x, int y) {
    return this.x == x && this.y == y;
  }

  // given a worldscene, a blocksize, and a color, draws this tile on the scene
  void draw(WorldScene scene, int blockSize, Color color) {
    int centerX = x * blockSize + blockSize / 2;
    int centerY = y * blockSize + blockSize / 2;
    

    // Draw the tile
    scene.placeImageXY(new RectangleImage(blockSize, blockSize, OutlineMode.SOLID, color),
        centerX, centerY);

    // Draw left wall
    if (!this.isConnected(this.x - 1, this.y) && this.x != 0) {
      scene.placeImageXY(new LineImage(new Posn(0, blockSize), Color.BLACK),
          x * blockSize, centerY);
    }

    // Draw top wall
    if (!this.isConnected(this.x, this.y - 1) && this.y != 0) {
      scene.placeImageXY(new LineImage(new Posn(blockSize, 0), Color.BLACK),
          centerX, y * blockSize);
    }

    // Draw right wall
    if (!this.isConnected(this.x + 1, this.y)) {
      scene.placeImageXY(new LineImage(new Posn(0, blockSize), Color.BLACK),
          (x + 1) * blockSize, centerY);
    }

    // Draw bottom wall
    if (!this.isConnected(this.x, this.y + 1)) {
      scene.placeImageXY(new LineImage(new Posn(blockSize, 0), Color.BLACK),
          centerX, (y + 1) * blockSize);
    }
  }
  
  // adds unseen neighbor tiles to the worklist and updates the hashmap
  void addUnseenConnectionsTo(IWorkList workList, ArrayList<MazeTile> seen,
      HashMap<MazeTile, MazeTile> cameFrom) {
    
    // adds unseen neighbor tiles to the worklist and updates the hashmap
    for (MazeTile tile : this.connections) {
      
      if (!seen.contains(tile)) {
        workList.add(tile);
        cameFrom.put(tile, this);
      }
    }
  }
  
  // returns the maximum of the given integer and the distance from the tile to the end
  int biggerFromEnd(int max) {
    return Math.max(this.distanceToEnd, max);
  }
  
  // returns the maximum of the given integer and the distance from the tile to the start
  int biggerFromStart(int max) {
    return Math.max(this.distanceToStart, max);
  }

  // given the maximum distance to the start tile,
  // returns the gradient color that represents the distance of this tile to the start
  Color toStartColor(int maxToStart) {
    double ratio = (double) this.distanceToStart / maxToStart;
    int red = (int)(255 * ratio);
    int green = (int)(255 * (1 - ratio));
    int blue = 0;
    return new Color(red, green, blue);
  }
  
  // given the maximum distance to the end tile,
  // returns the gradient color that represents the distance of this tile to the end
  Color toEndColor(int maxToEnd) {    
    double ratio = (double) this.distanceToEnd / maxToEnd;
    int red = (int)(255 * ratio);
    int green = (int)(255 * (1 - ratio));
    int blue = 0;
    return new Color(red, green, blue);
  }
  
}

// represents a weighted edge between two tiles
// (is used temporarily to construct a MST)
class Edge {

  private MazeTile tile1;
  private MazeTile tile2;
  private int weight;

  Edge(MazeTile tile1, MazeTile tile2, int weight) {
    this.tile1 = tile1;
    this.tile2 = tile2;
    this.weight = weight;
  }

  // connects the two tiles of this edge
  void connect() {
    this.tile1.addConnection(this.tile2);
    this.tile2.addConnection(this.tile1);
  }

  // given a hashmap of tile connections, 
  // checks if the two tiles on this edge are already connected
  boolean isConnected(HashMap<MazeTile, MazeTile> hash) {
    MazeTile ref1 = this.findRef(this.tile1, hash);
    MazeTile ref2 = this.findRef(this.tile2, hash);
    return ref1 == ref2;
  }

  // given a tile and a hashmap of tile connections,
  // returns the parent tile of the group the tile is in
  MazeTile findRef(MazeTile tile, HashMap<MazeTile, MazeTile> hash) {
    if (!hash.containsKey(tile)) {
      throw new IllegalArgumentException("Hash does not contain given MazeTile");
    }

    if (tile == hash.get(tile)) {
      return tile;
    }

    return this.findRef(hash.get(tile), hash);
  }

  // joins the two tiles this edge is connected to in the given hash map of tile connections
  void join(HashMap<MazeTile, MazeTile> hash) {
    MazeTile rep1 = this.findRef(this.tile1, hash);
    MazeTile rep2 = this.findRef(this.tile2, hash);
    hash.put(rep1, rep2);
  }

  // compares the weight of this edge to another
  // if this has a heigher weight, returns a positive number
  // if this has a lower weight, returns a negative number
  // otherwise returns 0
  int compareWeight(Edge other) {
    return this.weight - other.weight;
  }

}

// Represents a comparator that compares two edges based on weight
class BiggerWeight implements Comparator<Edge> {

  // compares the two given edges based on weight
  // if edge1 has a heigher weight, returns a positive number
  // if edge1 has a lower weight, returns a negative number
  // otherwise returns 0
  public int compare(Edge edge1, Edge edge2) {
    return edge1.compareWeight(edge2);
  }
 
}