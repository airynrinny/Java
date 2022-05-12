/**Nur Hanna Binti Yusof
 * 2017380
 * CSCI1301 SEC01
 * Individual Assignment
 * Reference : http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */

package testing;
 
import java.util.Collections;
import java.util.Arrays;
 
public class mazzee {
	
	public static void main(String[] args) {
		int length=10;
		int height=10;
		assignmaze maze=new assignmaze(length, height);
		maze.display();
	}

	private final int[][] maze;
	private final int length;
	private final int height;
 
	public assignmaze(int length, int height) {
		this.length=length;
		this.height=height;
		maze=new int[this.length][this.height];
		mazeMaze(0, 0);
	}
	
	private enum level {
		UP(1, 0, -1), 
		DOWN(2, 0, 1), 
		RIGHT(4, 1, 0), 
		LEFT(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private level opposite;
 
		static {
			UP.opposite=DOWN;
			DOWN.opposite=UP;
			RIGHT.opposite=LEFT;
			LEFT.opposite=RIGHT;
		}
 
		private level(int bit, int dx, int dy) {
			this.bit=bit;
			this.dx=dx;
			this.dy=dy;
		}
	}
	
	private static boolean between(int side, int up) {
		if ((side>=0) && (side<up))
			return true;
		else
			return false;
	}
 
	private void mazeMaze(int x, int y) {
		level[] dirs=level.values();
		for (level direction : dirs) {
			int nx=x+direction.dx;
			int ny=y+direction.dy;
			
			if (between(nx, length)&&between(ny, height)&&(maze[nx][ny]==0)) {
				maze[x][y] |= direction.bit;
				maze[nx][ny] |= direction.opposite.bit;
				mazeMaze(nx, ny);
			}
		}
	}
	
	public void display() {
		for (int i=0; i<height; i++) {
			for (int j=0; j<length; j++) {
				if((maze[j][i]&1)==0)
					System.out.print("+---");
				else
					System.out.print("+   ");
			}
			System.out.println("+");
			for (int j=0; j<length; j++) {
				if((maze[j][i]&8)==0)
					System.out.print("|   ");
				else
					System.out.print("    ");
			}
			System.out.println("|");
		}
		
		for (int j=0; j<length; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}
 
}