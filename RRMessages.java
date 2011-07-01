/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Messages
 * Created on January 7, 2003
 *
 */

/**
 * @author Keith Packard <keithp@keithp.com>
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class RRMessages extends TextArea {
    public RRMessages (int rows, int cols) {
	super ("", rows, cols, TextArea.SCROLLBARS_BOTH);
    }
    
    public void message (String user, String message) {
	this.append (user + ": " + message + "\n");
    }
    public void message (String message) {
	this.append (message);
    }
}
