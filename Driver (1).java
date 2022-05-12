package groupproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Random;

public class Driver {
	public static void main(String[] args) {
		// configure the width and height
		int w = Maze.DEFAULT_WIDTH;
		int h = Maze.DEFAULT_HEIGHT;
		long seed = 0L; 
		boolean useSeed = false;
		boolean animate = false;
		float delay = 0.04f;	

		// ignore arguments that we don't understand
		for ( int i=0; i < args.length; ++i ) {
			String arg = args[i];

			// parse the "parameter" arguments
			if ( arg.length() > 2 ) {
				String s = arg.substring(0,1);
				String t = arg.substring(1,2);
				if ( s.equals("-")) { 
					if ( t.equals("w") ) {
						w = Integer.parseInt(arg.substring(2));
					}
					else if ( t.equals("h") ) {
						h = Integer.parseInt(arg.substring(2));
					}
					else if ( t.equals("s") ) {
						useSeed = true; 
						seed = Long.parseLong(arg.substring(2));
					}
					else if ( t.equals("d") ) {
						delay = Float.parseFloat(arg.substring(2));
					}
				}
			}

			// parse the "no parameter" arguments
			else if ( arg.length() > 1) {
				String s = arg.substring(0,1);
				String t = arg.substring(1,2);
				if ( s.equals("-") ) { 
					if ( t.equals("a") ) {
						animate = true; 
					}
				}
			}
		}

		// render the maze
		if ( useSeed ) {
			new Kruskal(w,h,animate,delay,seed).draw();
		} else {
			new Kruskal(w,h,animate,delay).draw();
		}
	}
}

class Maze {
	// Define class variables 
	public static final int N = 1;
	public static final int S = 2;
	public static final int E = 4;
	public static final int W = 8;

	public static final int DEFAULT_WIDTH = 12;
	public static final int DEFAULT_HEIGHT = 12;

	protected Random _random = null;
	protected Long _seed = null; 
	protected int _w = 0;
	protected int _h = 0;
	protected int[][] _grid = null;

	// Define class methods
	public static int DX(int direction) {
		switch ( direction ) {
		case Maze.E:
			return +1;
		case Maze.W:
			return -1;
		case Maze.N:
		case Maze.S:
			return 0;
		}
		// error condition, but should never reach here
		return -1;
	}

	public static int DY(int direction) {
		switch ( direction ) {
		case Maze.E:
		case Maze.W:
			return 0;
		case Maze.N:
			return -1;
		case Maze.S:
			return 1;
		}
		// error condition, but should never reach here
		return -1;
	}

	public static int OPPOSITE(int direction) {
		switch ( direction ) {
		case Maze.E:
			return Maze.W;
		case Maze.W:
			return Maze.E;
		case Maze.N:
			return Maze.S;
		case Maze.S:
			return Maze.N;
		}
		// error condition, but should never reach here
		return -1;
	}

	public Maze() {
		this(DEFAULT_WIDTH,DEFAULT_HEIGHT);
	}
	public Maze(int w,int h) {
		initialize(w,h);
		_random = new Random();
	}
	public Maze(int w,int h,long seed) {
		initialize(w,h);
		_random = new Random(seed);
		_seed = new Long(seed);
	}
	private void initialize(int w,int h) {
		_w = w; _h = h; 
		_grid = new int[h][w];
		for ( int j=0; j < h; ++j ) {
			for ( int i=0; i < w; ++i ) {
				_grid[j][i] = 0;
			}
		}
	}

	public void draw() {
		// draw the "top" line
		System.out.print(" ");
		for ( int i=0; i < (_w*2 - 1); ++i ) {
			System.out.print("_");
		}
		System.out.println("");

		// draw each row
		for ( int j=0; j < _h; ++j ) {
			System.out.print("|");
			for ( int i=0; i < _w; ++i ) {
				// render "bottom" using the "S" switch
				System.out.print((_grid[j][i] & Maze.S) != 0 ? " " : "_");

				// render "side" using "E" switch
				if ( (_grid[j][i] & Maze.E) != 0 ) {
					System.out.print(((_grid[j][i] | _grid[j][i+1]) & Maze.S) != 0 ? " " : "_" );
				} else {
					System.out.print("|");
				}
			}
			System.out.println("");
		}

		// output maze metadata
		outputMetadata();
	}

	protected void outputMetadata() {
		String meta = " " + _w + " " + _h;
		if ( _seed != null ) {
			meta += " " + _seed;
		} else { 
			meta += " random";
		}
		System.out.println(meta);	
	}
}
class Kruskal extends Maze {
		// Define instance variables
		private boolean _animate = false;
		private float _delay = 0.0f;
		
		private List<List<Tree>> _sets;
		private Stack<Edge> _edges;
		
		public Kruskal() {
			super();
			initialize();
		}
		public Kruskal(int w,int h) {
			super(w,h);
			initialize();
		}
		public Kruskal(int w,int h,long seed) {
			super(w,h,seed);
			initialize(); 
		}

		public Kruskal(int w,int h,boolean animate,float delay) {
			super(w,h);
			_animate = animate;
			_delay = delay;
			initialize();
		}

		public Kruskal(int w,int h,boolean animate,float delay,long seed) {
			super(w,h,seed);
			_animate = animate;
			_delay = delay;
			initialize();
		}
		
		private void initialize() {
			
			_sets = new ArrayList<List<Tree>>();
			for ( int y=0; y < _h; ++y ) {
				List<Tree> tmp = new ArrayList<Tree>();
				for ( int x=0; x < _w; ++x ) {
					tmp.add(new Tree());
				}
				_sets.add(tmp);
			}
			
			
			_edges = new Stack<Edge>();
			for ( int y=0; y < _h; ++y ) {
				for (int x=0; x < _w; ++x ) {
					if ( y > 0 ) 	{ _edges.add(new Edge(x,y,Maze.N)); }
					if ( x > 0 ) 	{ _edges.add(new Edge(x,y,Maze.W)); }
				}
			}
			shuffle(_edges);
			
			if ( !_animate ) {
				carvePassages();
			}
		}
		
		
		public void draw() {
			// Clear the screen
			//System.out.print((char)27 + "[2J");
			
			if ( !_animate ) {
				// Move to the upper left and defer to superclass.
				//System.out.print((char)27 + "[H");
				super.draw();
			} else {
				// Carve the passages and animate as we go 
				carvePassages();
			}
		}
		
		
		public void display() {
			// Draw the "top row" of the maze
			System.out.print((char)27 + "[H");
			System.out.print(" ");
			for ( int i=0; i < (_w*2) - 1; ++i ) {
				System.out.print("_");
			}
			System.out.println("");
			
			// Step through the grid/maze, cell-by-cell
			for ( int y=0; y < _grid.length; ++y ) {
				System.out.print("|");
				for ( int x=0; x < _grid[0].length; ++x ) {
					// Start coloring, if unconnected
					if ( _grid[y][x] == 0 ) 	{ System.out.print((char)27 + "[47m"); }
					
					System.out.print( ((_grid[y][x] & Maze.S) != 0) ? " " : "_" );
					if ( (_grid[y][x] & Maze.E) != 0 ) {
						System.out.print( (((_grid[y][x] | _grid[y][x+1]) & Maze.S) != 0) ? " " : "_" );
					} else {
						System.out.print("|");
					}
					
					// Stop coloring, if unconnected
					if ( _grid[y][x] == 0 ) 	{ System.out.print((char)27 + "[m"); }
				}
				System.out.println("");
			}
		}
		
		private void carvePassages() {
			while ( _edges.size() > 0 ) {
				// Select the next edge, and decide which direction we are going in.
				Edge tmp = _edges.pop();
				int x = tmp.getX();
				int y = tmp.getY();
				int direction = tmp.getDirection();
				int dx = x + Maze.DX(direction), dy = y + Maze.DY(direction);
				
				// Pluck out the corresponding sets
				Tree set1 = (_sets.get(y)).get(x);
				Tree set2 = (_sets.get(dy)).get(dx);
				
				if ( !set1.connected(set2) ) {
					// If we are animating, display the maze and pause
					if ( _animate ) {
						display();
						try {
							 Thread.sleep((long)(_delay * 1000));
						} catch ( Exception ex ) {
							ex.printStackTrace();
						}
					}
					
					// Connect the two sets and "knock down" the wall between them.
					set1.connect(set2);
					_grid[y][x] |= direction;
					_grid[dy][dx] |= Maze.OPPOSITE(direction);
				}
			}
			
			if ( _animate ) {
				// Display the final iteration
				display();
				
				// Output maze metadata
				outputMetadata(); 
			}
		}
		

		private void shuffle(List<Edge> args) {
			for ( int i=0; i < args.size(); ++i ) {
				int pos = _random.nextInt(args.size());
				Edge tmp1 = args.get(i);
				Edge tmp2 = args.get(pos);
				args.set(i,tmp2);
				args.set(pos,tmp1);
			}
		}
	}

	class Tree {
		
		private Tree _parent = null;
		
		public Tree() {
			
		}
		
		public Tree root() {
			return _parent != null ? _parent.root() : this;
		}
		
		public boolean connected(Tree tree) {
			return this.root() == tree.root();
		}
		
		public void connect(Tree tree) {
			tree.root().setParent(this);
		}

		public void setParent(Tree parent) {
			this._parent = parent;
		}
	}

	class Edge {
		private int _x;
		private int _y;
		private int _direction;
		
		public Edge(int x, int y, int direction) {
			_x = x; 
			_y = y;
			_direction = direction;
		}
		
		public int getX() { return _x; }
		public int getY() { return _y; }
		public int getDirection() { return _direction; }
}
