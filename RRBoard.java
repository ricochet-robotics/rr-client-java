/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Board
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import java.io.*;
import java.util.*;

public class RRBoard {
    public static final int dim = 16;
    volatile RRSquare[][] squares = new RRSquare[dim][dim];
    volatile RRBoardCoord goal = null;
    RRController ctl;
    boolean active = false;
    public String playerName = null;

    public RRBoard(RRController ctl, String player_name) {
	this.ctl = ctl;
	this.playerName = player_name;
	for (int row = 0; row < dim; row++)
	    for (int col = 0; col < dim; col++)
		squares[row][col] = new RRSquare();
	for (int i = 0; i < dim; i++) {
	    squares[0][i].setWall(0);
	    squares[i][dim - 1].setWall(1);
	    squares[dim - 1][i].setWall(2);
	    squares[i][0].setWall(3);
	}
    }

    public RRBoard(BufferedReader in, RRController ctl)
      throws IOException {
	this(ctl, null);
	become(in);
    }


    public RRBoard(String board, RRController ctl)
      throws IOException {
	this(ctl, null);
	become(board);
    }


    static void expect(int c, int ch)
      throws IOException {
	if (c != ch)
	    throw new IOException("expected " + ch + ", got " + c);
    }

    static String next_line(BufferedReader in, int off)
      throws IOException {
	String s = in.readLine();
	int n = s.length();
	if (n != 4 * dim + off)
	    throw new IOException("wrong line width");
	return s;
    }

    void wall_line(String s, int i)
      throws IOException {
	int ci = 0;
	for (int j = 0; j < dim; j++) {
	    int c = s.charAt(ci++);
	    expect(c, ' ');
	    c = s.charAt(ci++);
	    if (c == '=') {
		if (i - 1 >= 0)
		    squares[i - 1][j].setWall(2);
		if (i < dim)
		    squares[i][j].setWall(0);
	    } else {
		expect(c, ' ');
	    }
	    int cx = s.charAt(ci++);
	    expect(cx, c);
	    cx = s.charAt(ci++);
	    expect(cx, c);
	}
	int c = s.charAt(ci++);
	expect(c, ' ');
    }

    void target_line(String s, int i)
      throws IOException {
	int ci = 0;
	for (int j = 0; j <= dim; j++) {
	    int c = s.charAt(ci++);
	    if (c == '|') {
		if (j < dim)
		    squares[i][j].setWall(3);
		if (j - 1 >= 0)
		    squares[i][j - 1].setWall(1);
	    } else {
		expect(c, ' ');
	    }
	    if (j == dim)
		break;
	    c = s.charAt(ci++);
	    switch(c) {
	    case 'r':
	    case 'R':
		squares[i][j].setRobot(RRSquare.RED);
		    break;
	    case 'y':
	    case 'Y':
		squares[i][j].setRobot(RRSquare.YELLOW);
		break;
	    case 'g':
	    case 'G':
		squares[i][j].setRobot(RRSquare.GREEN);
		break;
	    case 'b':
	    case 'B':
		squares[i][j].setRobot(RRSquare.BLUE);
		break;
	    default:
		expect(c, '.');
	    }
	    int c1 = s.charAt(ci++);
	    int c2 = s.charAt(ci++);
	    int color = -1;
	    boolean goal_target = false;
	    switch (c1) {
	    case 'R':
		goal_target = true;
	    case 'r':
		color = RRSquare.RED;
		break;
	    case 'Y':
		goal_target = true;
	    case 'y':
		color = RRSquare.YELLOW;
		break;
	    case 'G':
		goal_target = true;
	    case 'g':
		color = RRSquare.GREEN;
		break;
	    case 'B':
		goal_target = true;
	    case 'b':
		color = RRSquare.BLUE;
		break;
	    case 'W':
		goal_target = true;
	    case 'w':
		color = RRSquare.WHIRLPOOL;
		break;
	    default:
		expect(c1, '.');
	    }
	    int shape = -1;
	    boolean goal_shape = false;
	    switch(c2) {
	    case 'C':
		goal_shape = true;
	    case 'c':
		shape = RRImages.CIRCLE;
		break;
	    case 'S':
		goal_shape = true;
	    case 's':
		shape = RRImages.SQUARE;
		break;
	    case 'T':
		goal_shape = true;
	    case 't':
		shape = RRImages.TRIANGLE;
		break;
	    case 'O':
		goal_shape = true;
	    case 'o':
		shape = RRImages.STAR;
		break;
	    case 'W':
		goal_shape = true;
	    case 'w':
		if (color != RRSquare.WHIRLPOOL)
		    throw new IOException("target whirlpool mismatch");
		shape = 0;
		break;
	    default:
		expect(c2, '.');
		if (color != -1)
		    throw new IOException("target blank mismatch");
	    }
	    if (goal_shape != goal_target)
		throw new IOException("target priority mismatch");
	    if (color >= 0) {
		int symbol = 4 * color + shape;
		squares[i][j].setTarget(symbol, goal_target);
		if (goal_target)
		    goal = new RRBoardCoord(i, j);
	    }
	}
    }


    synchronized public void become(BufferedReader in)
      throws IOException {
	goal = null;
	for (int i = 0; i < dim; i++) {
	    String s = next_line(in, 1);
	    wall_line(s, i);
	    s = next_line(in, 1);
	    target_line(s, i);
	}
	String s = next_line(in, 2);
	wall_line(s, dim);
    }

    synchronized public void become(String board)
      throws IOException {
	goal = null;
	StringTokenizer st = new StringTokenizer(board, "\n");
	String s;
	for (int i = 0; i < dim; i++) {
	    s = st.nextToken();
	    wall_line(s, i);
	    s = st.nextToken();
	    target_line(s, i);
	}
	s = st.nextToken();
	wall_line(s, dim);
    }


    synchronized public RRSquare getSquare(int row, int col) {
        return squares[row][col];
    }

    synchronized public void moveRobot(RRBoardCoord start, RRBoardCoord end) {
	/* anything to move? */
	RRSquare ss = squares[start.row][start.col];
	int robot = ss.getRobot();
	if (robot == ss.NO_ROBOT)
	    return;
	if (start.equals(end))
	    return;
	/* figure out where it's going */
	int dr = 0;
	int dc = 0;
	int dir = -1;
	RRBoardCoord cur = new RRBoardCoord(start);
	if (cur.row == end.row) {
	    if (cur.col < end.col) {
		dir = 1;
		dc = 1;
	    } else {
		dir = 3;
		dc = -1;
	    }
	} else if (cur.col == end.col) {
	    if (cur.row < end.row) {
		dir = 2;
		dr = 1;
	    } else {
		dir = 0;
		dr = -1;
	    }
	} else {
	    return;
	}
	while (!cur.equals(end)) {
	    RRSquare scur = squares[cur.row][cur.col];
	    if (scur.getRobot() != scur.NO_ROBOT) {
		cur.row -= dr;
		cur.col -= dc;
		break;
	    }
	    if (scur.getWall(dir))
		break;
	    cur.row += dr;
	    cur.col += dc;
	}
	/* deal with it */
	if (cur.equals(start))
	    return;
	ctl.netout.move(robot, dir);
    }

    synchronized public void moveRobot(int color, RRBoardCoord end) {
	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		if (squares[i][j].getRobot() == color) {
		    RRBoardCoord start = new RRBoardCoord(i, j);
		    RRSquare ss = squares[start.row][start.col];
		    ss.clearRobot();
		    break;
		}
	    }
	}
	RRSquare se = squares[end.row][end.col];
	se.setRobot(color);
    }

    public void setActive(boolean active) {
	this.active = active;
    }

    public boolean getActive() {
	return active;
    }
}
