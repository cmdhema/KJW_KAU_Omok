import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;

class Omok_Board extends Canvas {
	public static final int BLACK = 1, WHITE = -1;
	private int[][] map;
	private int size, cell;
	private String info = "게임 중지";
	private int color = BLACK;
	private boolean enable = false;
	private boolean running = false;
	private PrintWriter writer;
	private Graphics gboard, gbuff;
	private Image buff;

	Omok_Board(int s, int c) {
		this.size = s;
		this.cell = c;
		map = new int[size + 2][];
		for (int i = 0; i < map.length; i++)
			map[i] = new int[size + 2];
		setBackground(new Color(200, 200, 100));
		setSize(size * (cell + 1) + size, size * (cell + 1) + size);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (!enable)
					return;
				int x = (int) Math.round(me.getX() / (double) cell);
				int y = (int) Math.round(me.getY() / (double) cell);
				if (x == 0 || y == 0 || x == size + 1 || y == size + 1)
					return;
				if (map[x][y] == BLACK || map[x][y] == WHITE)
					return;
				writer.println("[STONE]" + x + " " + y);
				map[x][y] = color;
				if (check(new Point(x, y), color)) {
					info = "이겼습니다.";
					writer.println("[WIN]");
				} else
					info = "상대가 두기를 기다립니다.";
				repaint();
				enable = false;
			}
		});
	}

	public boolean isRunning() {
		return running;
	}

	public void startGame(String col) {
		running = true;
		if (col.equals("BLACK")) {
			enable = true;
			color = BLACK;
			info = "게임 시작... 두세요.";
		} else {
			enable = false;
			color = WHITE;
			info = "게임 시작... 기다리세요.";
		}
	}

	public void stopGame() {
		reset();
		writer.println("[STOPGAME]");
		enable = false;
		running = false;
	}

	public void putOpponent(int x, int y) {
		map[x][y] = -color;
		info = "상대가 두었습니다. 두세요.";
		repaint();
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (gbuff == null) {
			buff = createImage(getWidth(), getHeight());
			gbuff = buff.getGraphics();
		}
		drawBoard(g);
	}

	public void reset() {
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++)
				map[i][j] = 0;
		info = "게임 중지";
		repaint();
	}

	private void drawLine() {
		gbuff.setColor(Color.black);
		for (int i = 1; i <= size; i++) {
			gbuff.drawLine(cell, i * cell, cell * size, i * cell);
			gbuff.drawLine(i * cell, cell, i * cell, cell * size);
		}
	}

	private void drawBlack(int x, int y) {
		Graphics2D gbuff = (Graphics2D) this.gbuff;
		gbuff.setColor(Color.black);
		gbuff.fillOval(x * cell - cell / 2, y * cell - cell / 2, cell, cell);
		gbuff.setColor(Color.white);
		gbuff.drawOval(x * cell - cell / 2, y * cell - cell / 2, cell, cell);
	}

	private void drawWhite(int x, int y) {
		gbuff.setColor(Color.white);
		gbuff.fillOval(x * cell - cell / 2, y * cell - cell / 2, cell, cell);
		gbuff.setColor(Color.black);
		gbuff.drawOval(x * cell - cell / 2, y * cell - cell / 2, cell, cell);
	}

	private void drawStones() {
		for (int x = 1; x <= size; x++)
			for (int y = 1; y <= size; y++) {
				if (map[x][y] == BLACK)
					drawBlack(x, y);
				else if (map[x][y] == WHITE)
					drawWhite(x, y);
			}
	}

	synchronized private void drawBoard(Graphics g) {
		gbuff.clearRect(0, 0, getWidth(), getHeight());
		drawLine();
		drawStones();
		gbuff.setColor(Color.red);
		gbuff.drawString(info, 20, 15);
		g.drawImage(buff, 0, 0, this);
	}

	private boolean check(Point p, int col) {
		if (count(p, 1, 0, col) + count(p, -1, 0, col) == 4)
			return true;
		if (count(p, 0, 1, col) + count(p, 0, -1, col) == 4)
			return true;
		if (count(p, -1, -1, col) + count(p, 1, 1, col) == 4)
			return true;
		if (count(p, 1, -1, col) + count(p, -1, 1, col) == 4)
			return true;
		return false;
	}

	private int count(Point p, int dx, int dy, int col) {
		int i = 0;
		for (; map[p.x + (i + 1) * dx][p.y + (i + 1) * dy] == col; i++)
			;
		return i;
	}
}