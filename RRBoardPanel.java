/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Board Panel
 * Created on May 31, 2003
 *
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

class RobotMover extends MouseInputAdapter {
    static final int IDLE = 0;
    static final int DRAGGING = 1;
    int state = IDLE;
    RRController ctl;
    public RRBoardCoord start = null;
    public RRBoardCoord stop = null;
    public RRBoardCoord cur = null;

    public RobotMover(RRController ctl) {
	super();
	this.ctl = ctl;
    }

    public void mousePressed(MouseEvent e) {
	if (e.getButton() != e.BUTTON1)
	    return;
	start = ctl.boardPanel.boardCoord(e.getPoint());
	stop = null;
	cur = start;
	state = DRAGGING;
    }

    public void mouseReleased(MouseEvent e) {
	if (state != DRAGGING)
	    return;
	if (e.getButton() != e.BUTTON1)
	    return;
	stop = ctl.boardPanel.boardCoord(e.getPoint());
	cur = stop;
	state = IDLE;
	if (!start.equals(stop))
	    ctl.board.moveRobot(start, stop);
	ctl.boardPanel.clearHilite();
    }

    public void mouseDragged(MouseEvent e) {
	if (state != DRAGGING)
	    return;
	cur = ctl.boardPanel.boardCoord(e.getPoint());
	ctl.boardPanel.setHilite(cur);
    }
}

public class RRBoardPanel extends JPanel {
    static final double wallwidth = 0.1;

    RRController ctl;
    RobotMover mover;

    int dim;
    double dcol, drow;

    RRBoardCoord hilited = null;

    static int floor(double d) {
	return (int)Math.floor(d);
    }

    void update_dims() {
	dim = ctl.board.dim;
        Dimension d = getSize();
	dcol = d.width / (double)dim;
	drow = d.height / (double)dim;
    }

    public RRBoardPanel(RRController ctl) {
        this.ctl = ctl;
	mover = new RobotMover(ctl);
	this.addMouseMotionListener(mover);
	this.addMouseListener(mover);
    }

    public void paintComponent(Graphics g) {
	update_dims();

	int idcol = floor(dcol + 1);
	int idrow = floor(drow + 1);

	/* draw floor */
	Image blank_img = ctl.images.getBlankImage();
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
		int xdcol = floor(col * dcol);
		int ydrow = floor(row * drow);
		Image blank = blank_img;
		RRSquare s = ctl.board.getSquare(row, col);
		if (s.isPrimaryTarget()) {
		    int target = s.getTarget();
		    blank = ctl.images.getColoredBlankImage(target / 4);
		}
		g.drawImage(blank, xdcol, ydrow, idcol, idrow, this);
	    }
	}
        /* draw targets */
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                RRSquare s = ctl.board.getSquare(row, col);
		if (s.getTarget() == RRSquare.NO_TARGET)
		    continue;
		boolean primary = s.isPrimaryTarget();
		Image img = ctl.images.getTargetImage(s.getTarget(), primary);
		int xdcol = floor(col * dcol);
		int ydrow = floor(row * drow);
		g.drawImage(img, xdcol, ydrow, idcol, idrow, this);
            }
        }
        /* draw walls */
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                RRSquare s = ctl.board.getSquare(row, col);
                for (int w = 0; w < 4; w++) {
                    if (!s.getWall(w))
                        continue;
                    double wdcol = wallwidth * dcol ;
                    double wdrow = wallwidth * drow ;
                    double wc = col * dcol - wdcol;
                    double wr = row * drow - wdrow;
		    double ww = 2 * wdcol + 1;
		    double wh = 2 * wdrow + 1;
                    if (w == 1 || w == 3)
			wh += drow - 1;
		    else
			ww += dcol - 1;
		    if (w == 1)
			wc += dcol;
		    if (w == 2)
			wr += drow;
		    int iwc = floor(wc);
		    int iwr = floor(wr);
		    int iww = floor(ww);
		    int iwh = floor(wh);
                    g.setColor(Color.BLACK);
                    g.fillRect(iwc, iwr, iww, iwh);
                }
            }
        }
        /* draw robots */
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                RRSquare s = ctl.board.getSquare(row, col);
                int r = s.getRobot();
                if (r == RRSquare.NO_ROBOT)
                    continue;
                Image img = ctl.images.getRobotImage(r);
		int col_dcol = floor(col * dcol);
		int row_drow = floor(row * drow);
                g.drawImage(img, col_dcol, row_drow, idcol, idrow, this);
            }
        }
	/* draw hilite */
	if (hilited != null) {
	    int x = floor(hilited.col * dcol);
	    int y = floor(hilited.row * drow);
	    int w = floor(dcol);
	    int h = floor(drow);
	    g.setColor(Color.MAGENTA);
	    g.drawRect(x, y, w, h);
	}
    }

    public RRBoardCoord boardCoord(Point p) {
	update_dims();
	return new RRBoardCoord(floor(p.y / drow), floor(p.x / dcol));
    }

    public void setHilite(RRBoardCoord c) {
	if (hilited == null || !hilited.equals(c))
	    repaint();
	hilited = c;
    }

    public void clearHilite() {
	hilited = null;
	repaint();
    }
}
