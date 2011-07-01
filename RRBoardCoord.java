/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Board Coordinate
 * Created on June 6, 2003
 *
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

public class RRBoardCoord {
    public int row, col;

    public RRBoardCoord(int row, int col) {
	this.row = row;
	this.col = col;
    }

    public RRBoardCoord(RRBoardCoord c) {
	this.row = c.row;
	this.col = c.col;
    }

    public boolean equals(RRBoardCoord b) {
	if (b.row != row)
	    return false;
	if (b.col != col)
	    return false;
	return true;
    }
}
