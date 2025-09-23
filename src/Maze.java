import java.awt.Color;
import java.util.*;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;

// represents a maze
class Maze extends World {
  final int width;
  final int height;
  private int blockSize;
  private ArrayList<ArrayList<MazeTile>> grid;
  private Collection<MazeTile> path;
  private ArrayList<MazeTile> seen;
  private MazeTile currentPosition;
  private boolean searchInProgress;
  private IWorkList workList;
  private HashMap<MazeTile, MazeTile> cameFrom;
  private boolean searchComplete;
  private boolean showSeen;
  
  // either "none" "to start" or "to end"
  private String gradientMode;

  Maze(int width, int height) {
    this.blockSize = 20;
    this.width = width;
    this.height = height;
    this.grid = new MazeBuilder().buildMaze(width, height);
    this.currentPosition = this.grid.get(0).get(0);
    this.path = new ArrayList<MazeTile>();
    this.seen = new ArrayList<MazeTile>();
    this.searchInProgress = false;
    this.searchComplete = false;
    this.showSeen = true;
    this.gradientMode = "normal";
    
  }

  Maze(int width, int height, int blockSize) {
    this.blockSize = blockSize;
    this.width = width;
    this.height = height;
    this.grid = new MazeBuilder().buildMaze(width, height);
    this.currentPosition = this.grid.get(0).get(0);
    this.path = new ArrayList<MazeTile>();
    this.seen = new ArrayList<MazeTile>();
    this.searchInProgress = false;
    this.searchComplete = false;
    this.showSeen = true;
    this.gradientMode = "normal";
  }

  // Resets maze to state before search
  void clear() {
    this.path = new ArrayList<MazeTile>();
    this.seen = new ArrayList<MazeTile>();
    this.currentPosition = this.grid.get(0).get(0);
    this.searchInProgress = false;
    this.workList = null;
    this.cameFrom = null;
    this.searchComplete = false;
    this.gradientMode = "normal";
  }

  // Generates a new maze
  void newMaze() {
    this.grid = new MazeBuilder().buildMaze(width, height);
    this.clear();
  }

  // draws the maze and paths based on the gradient mode
  public WorldScene makeScene() {

    WorldScene scene = new WorldScene(this.width * this.blockSize, this.height * this.blockSize);
    int maxToStart = this.maxDistanceFromStart();
    int maxToEnd = this.maxDistanceFromEnd();
    
    for (ArrayList<MazeTile> column : grid) {
      for (MazeTile tile : column) {
        Color color;

        // Priority: Start/End > Current Location > Path > Tries > Gradient Mode > Default
        if (tile.isAt(0, 0)) {
          color = Color.GREEN;
        }
        else if (tile.isAt(width - 1, height - 1)) {
          color = new Color(150, 0, 255);
        }
        else if (tile.equals(this.currentPosition)) {
          color = Color.RED;
        }
        else if (this.path.contains(tile)) {
          color = Color.BLUE;
        }
        else if (this.seen.contains(tile) && this.showSeen) {
          color = Color.CYAN;
        }
        else if (this.gradientMode.equals("to start")) {
          color = tile.toStartColor(maxToStart);
        } 
        else if (this.gradientMode.equals("to end")) {
          color = tile.toEndColor(maxToEnd);
        }
        else {
          color = Color.WHITE;
        }
        tile.draw(scene, this.blockSize, color);
      }
    }

    scene.placeImageXY(
        new TextImage("Wrong moves: " + (this.seen.size() - this.path.size()), Color.BLACK), width * this.blockSize - 60,
        this.blockSize / 2);

    return scene;
  }
  
  // returns the maximum distance from the end tile
  int maxDistanceFromEnd() {
    int max = 0;
    
    // iterates through 2d arraylist and 
    // set max as the maximum of max and the tiles distance to end
    for (ArrayList<MazeTile> column : this.grid) {
      for (MazeTile tile : column) {
        max = tile.biggerFromEnd(max);
      }
      
    }
    return max;
  }
  
  // returns the maximum distance from the start tile
  int maxDistanceFromStart() {
    int max = 0;
    
    // iterates through 2d arraylist and 
    // set max as the maximum of max and the tiles distance to start
    for (ArrayList<MazeTile> column : this.grid) {
      for (MazeTile tile : column) {
        max = tile.biggerFromStart(max);
      }
      
    }
    return max;
  }
  

  // responds to user input according to UserGuide.txt
  public void onKeyEvent(String key) {

    if (key.equals("d")) {
      this.clear();
      this.startSearch(new MazeStack());
    }
    else if (key.equals("b")) {
      this.clear();
      this.startSearch(new MazeQueue());
    }
    else if (key.equals("c")) {
      this.clear();
    }
    else if (key.equals("n")) {
      this.newMaze();
    }
    else if (key.equals("s")) {
      this.showSeen = !this.showSeen;
    }
    else if (key.equals("1")) {
      this.gradientMode = "to start";
    }
    else if (key.equals("9")) {
      this.gradientMode = "to end";
    }
    else if (key.equals("k")) {
      this.gradientMode = "normal";
    }
    else if (!this.searchComplete && !this.searchInProgress
        && (key.equals("right") || key.equals("left") || key.equals("down") || key.equals("up"))) {

      MazeTile nextPosition = this.currentPosition;

      int currentX = -1;
      int currentY = -1;

      // goes through the 2d arraylist and finds the x y coordinates of this tile
      for (int x = 0; x < this.grid.size(); x++) {
        for (int y = 0; y < this.grid.get(x).size(); y++) {
          if (this.grid.get(x).get(y) == this.currentPosition) {
            currentX = x;
            currentY = y;
            break;
          }
        }
        if (currentX != -1) {
          break;
        }
      }

      if (key.equals("up") && currentY > 0) {
        if (this.currentPosition.isConnected(currentX, currentY - 1)) {
          nextPosition = this.grid.get(currentX).get(currentY - 1);
        }
      }
      else if (key.equals("down") && currentY < this.height - 1) {
        if (this.currentPosition.isConnected(currentX, currentY + 1)) {
          nextPosition = this.grid.get(currentX).get(currentY + 1);
        }
      }
      else if (key.equals("left") && currentX > 0) {
        if (this.currentPosition.isConnected(currentX - 1, currentY)) {
          nextPosition = this.grid.get(currentX - 1).get(currentY);
        }
      }
      else if (key.equals("right") && currentX < this.width - 1) {
        if (this.currentPosition.isConnected(currentX + 1, currentY)) {
          nextPosition = this.grid.get(currentX + 1).get(currentY);
        }
      }

      if (!nextPosition.equals(this.currentPosition)) {
        if (path.contains(nextPosition)) {
          this.path.remove(this.currentPosition);
        }
        else {
          this.path.add(nextPosition);
        }
        if (!this.seen.contains(nextPosition)) {
          this.seen.add(nextPosition);
        }
        this.currentPosition = nextPosition;
      }
      if (this.currentPosition.isAt(this.width - 1, this.height - 1)) {
        this.searchComplete = true;
      }
    }

  }

  // performs a search step if needed
  public void onTick() {
    if (this.searchInProgress && !this.searchComplete) {
      this.performSearchStep();
    }
  }

  // given the current tile and a hash map representing where tiles came from,
  // returns a list of tiles in the path
  ArrayList<MazeTile> findPath(MazeTile tile, HashMap<MazeTile, MazeTile> cameFrom) {
    ArrayList<MazeTile> path = new ArrayList<MazeTile>();
    MazeTile current = tile;

    while (!current.isAt(0, 0)) {
      path.add(current);

      if (!cameFrom.containsKey(current)) {
        throw new IllegalArgumentException("Could not trace maze path");
      }

      current = cameFrom.get(current);
    }

    path.add(current);
    return path;
  }

  // iterates the next step of the serach
  void performSearchStep() {
    if (this.workList.isEmpty()) {
      this.searchComplete = true;
      return;
    }

    MazeTile tile = this.workList.remove();
    this.seen.add(tile);

    if (tile.isAt(this.width - 1, this.height - 1)) {
      this.path = this.findPath(tile, this.cameFrom);
      this.searchComplete = true;
      return;
    }
    tile.addUnseenConnectionsTo(this.workList, this.seen, this.cameFrom);
  }

  // begins the serach
  void startSearch(IWorkList workList) {
    if (this.grid.size() == 0 || this.grid.get(0).size() == 0) {
      throw new IllegalArgumentException("This maze has no tiles");
    }

    this.seen.clear();
    this.path.clear();
    this.searchInProgress = true;
    this.workList = workList;
    this.cameFrom = new HashMap<MazeTile, MazeTile>();
    this.searchComplete = false;
    this.workList.add(this.grid.get(0).get(0));
  }

  // starts the animation at the given frequency
  void start(double frequency) {
    this.bigBang(width * this.blockSize, height * this.blockSize, frequency);
  }
}

class ExamplesBigBang {

  ArrayList<Edge> unsortedEdges;

  Maze maze = new Maze(3, 3);

  void init() {
    this.unsortedEdges = new ArrayList<Edge>();
    this.unsortedEdges.add(new Edge(null, null, 10));
    this.unsortedEdges.add(new Edge(null, null, 0));
    this.unsortedEdges.add(new Edge(null, null, 100));
    this.unsortedEdges.add(new Edge(null, null, 7));
  }

  void testEdgeSort(Tester t) {
    this.init();
    this.unsortedEdges.sort(new BiggerWeight());

    t.checkExpect(this.unsortedEdges, new ArrayList<Edge>(Arrays.asList(new Edge(null, null, 0),
        new Edge(null, null, 7), new Edge(null, null, 10), new Edge(null, null, 100))));
  }

  void testBigbang(Tester t) {
    Maze world = new Maze(20, 10);
    world.start(Double.MIN_VALUE);
  }
  

}