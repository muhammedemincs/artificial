public class PlayerSkeleton {
	private static final int INF = 99999999;
	private GhostState g;
	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		// The reason for two sets of total scores is to keep track of endgame
		// moves. totalScore will keep track of moves that does not end in game
		// over while totalBadScore will keep track of the best move among failing
		// moves. If all moves are failing, totalBadScore will be used to judge.
		int totalScore = -INF;
		int bestMove = 0;
		int totalBadScore = -INF;
		int bestBadMove = 0;
		for(int i=0; i<legalMoves.length; i++){
			int score = 0;
			g.reset(s, legalMoves[i]);
			//ignore this move if game over
			score += -1*g.getTotalHeight(); //Minimize total height
			score += -1*g.getMaxHeight(); //Minimize max height
			if(score >= totalScore && !g.hasLost()){
				totalScore = score;
				bestMove = i;
			}else if(score >= totalBadScore && g.hasLost()){
				totalBadScore = score;
				bestBadMove = i;
			}
		}
		if(totalScore == -INF){
			return bestBadMove;
		}else{
			return bestMove;
		}
	}

	public PlayerSkeleton(){
		g = new GhostState();
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared()
			+ " rows.");
	}

	private class GhostState{
		private int[][] field;
		private int[] top;
		private int nextPiece;
		private int turn;
		private int cleared;
		private boolean lost;
		//Copied from State.java. Required for makeMove
		private int[][] pWidth = { { 2 }, { 1, 4 }, { 2, 3, 2, 3 },
			{ 2, 3, 2, 3 }, { 2, 3, 2, 3 }, { 3, 2 }, { 3, 2 } };
		private int[][] pHeight = { { 2 }, { 4, 1 }, { 3, 2, 3, 2 },
			{ 3, 2, 3, 2 }, { 3, 2, 3, 2 }, { 2, 3 }, { 2, 3 } };
		private int[][][] pBottom = { { { 0, 0 } },
			{ { 0 }, { 0, 0, 0, 0 } },
			{ { 0, 0 }, { 0, 1, 1 }, { 2, 0 }, { 0, 0, 0 } },
			{ { 0, 0 }, { 0, 0, 0 }, { 0, 2 }, { 1, 1, 0 } },
			{ { 0, 1 }, { 1, 0, 1 }, { 1, 0 }, { 0, 0, 0 } },
			{ { 0, 0, 1 }, { 1, 0 } }, { { 1, 0, 0 }, { 0, 1 } } };
		private int[][][] pTop = { { { 2, 2 } }, { { 4 }, { 1, 1, 1, 1 } },
			{ { 3, 1 }, { 2, 2, 2 }, { 3, 3 }, { 1, 1, 2 } },
			{ { 1, 3 }, { 2, 1, 1 }, { 3, 3 }, { 2, 2, 2 } },
			{ { 3, 2 }, { 2, 2, 2 }, { 2, 3 }, { 1, 2, 1 } },
			{ { 1, 2, 2 }, { 3, 2 } }, { { 2, 2, 1 }, { 2, 3 } } };

		public GhostState(){
			lost = false;
			field = new int[State.ROWS][State.COLS];
			top = new int[State.COLS];
		}
		public void reset(State template, int[] move){
			lost = template.hasLost();
			int[][] oField = template.getField();
			for(int i=0;i<oField.length;i++){
				System.arraycopy(oField[i] ,0,field[i], 0, oField[i].length);
			}
			System.arraycopy(template.getTop(), 0, top, 0, template.getTop().length);
			turn = template.getTurnNumber();
			cleared = 0;
			nextPiece = template.getNextPiece();
			makeMove(move[State.ORIENT],move[State.SLOT]);
		}

		//Returns the total height of all the columns
		public int getTotalHeight(){
			int result = 0;
			for(int i=0;i<top.length;i++){
				result += top[i];
			}
			return result;
		}

		//Returns the maximum column height out of all columns
		public int getMaxHeight(){
			int result = -1;
			for(int i=0;i<top.length;i++){
				result = Math.max(result, top[i]);
			}
			return result;
		}
		//This function tells if the last moved resulted in Game Over
		public boolean hasLost(){
			return lost;
		}
		//This function is identical to the one in State for all practical purposes
		public boolean makeMove(int orient, int slot){
			int height = top[slot] - pBottom[nextPiece][orient][0];
			for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
				height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
			}
			if (height + pHeight[nextPiece][orient] >= State.ROWS) {
				lost = true;
				return false;
			}
			for (int i = 0; i < pWidth[nextPiece][orient]; i++) {
				for (int h = height + pBottom[nextPiece][orient][i]; h < height
					+ pTop[nextPiece][orient][i]; h++) {
					field[h][i + slot] = turn;
				}
			}
			for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
				top[slot + c] = height + pTop[nextPiece][orient][c];
			}
			int rowsCleared = 0;
			for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
				boolean full = true;
				for (int c = 0; c < State.COLS; c++) {
					if (field[r][c] == 0) {
						full = false;
						break;
					}
				}
				if (full) {
					rowsCleared++;
					cleared++;
					for (int c = 0; c < State.COLS; c++) {

						for (int i = r; i < top[c]; i++) {
							field[i][c] = field[i + 1][c];
						}
						top[c]--;
						while (top[c] >= 1 && field[top[c] - 1][c] == 0)
							top[c]--;
					}
				}
			}
			lost = false;
			return true;
		}
	}
}
