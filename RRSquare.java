/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Square
 * Created on May 31, 2003
 *
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

public class RRSquare {
    public static final int NO_TARGET = -1;
    public static final int RED = 0;
    public static final int YELLOW = 1;
    public static final int GREEN = 2;
    public static final int BLUE = 3;
    public static final int WHIRLPOOL = 4;
    public static final int NO_ROBOT = -1;

    public static final String[] colorname = {
	"red", "yellow", "green", "blue", "whirlpool"
    };

    /* see above */
    int target = NO_TARGET;
    /* R, Y, G, B */
    int robot = NO_ROBOT;
    /* N, E, S, W */
    boolean[] wall = {false, false, false, false};
    /* Is this the goal square? */
    boolean primary_target = false;
    
    public void setTarget(int t) {
        target = t;
    }

    public void setTarget(int t, boolean primary) {
        target = t;
	primary_target = primary;
    }

    public int getTarget() {
        return target;
    }

    public boolean isPrimaryTarget() {
        return primary_target;
    }

    public int getRobot() {
        return robot;
    }

    public void setRobot(int r) {
        robot = r;
    }
    
    public void clearRobot() {
        robot = NO_ROBOT;
    }
    
    public boolean getWall(int w) {
        return wall[w];
    }

    /* XXX can't remove walls for now */
    public void setWall(int w) {
        wall[w] = true;
    }
}
