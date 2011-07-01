/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Output Server Connection
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import java.io.*;
import java.net.*;

public class RRNetOut {
    PrintStream out;
    RRController ctl;
    
    /* XXX Doesn't belong here */
    public String[] dirname = {
	"north", "east", "south", "west"
    };

    void uniprint(String s) {
	boolean isodd = false;
	for (int i = 0; i < s.length(); i++) {
	    int c = s.charAt(i);
	    if (c <= ' ' || c >= 0x7f || c == '"') {
		isodd = true;
		break;
	    }
	}
	if (isodd) {
	    out.print('"');
	    for (int i = 0; i < s.length(); i++) {
		if (s.charAt(i) == '"')
		    out.print('\\');
		out.print(s.charAt(i));
	    }
	    out.print('"');
	    return;
	}
	out.print(s);
    }

    public RRNetOut(Socket s, RRController ctl)
      throws IOException, UnsupportedEncodingException {
	this.ctl = ctl;
	OutputStream so = s.getOutputStream();
	BufferedOutputStream sb = new BufferedOutputStream(so);
	out = new PrintStream(sb, true, "UTF-8");
    }

    public void hello(String name) {
	out.print("helo ");
	uniprint(name);
	out.println();
    }

    public void newGame(String game) {
        out.print("new ");
	uniprint(game);
	out.println();
    }

    public void join(String game) {
        out.print("join ");
	uniprint(game);
	out.println();
    }

    public void show() {
	out.println("show");
    }

    public void watch(String game) {
	out.print("watch ");
	uniprint(game);
	out.println();
    }

    public void bid(int b) {
	out.print("bid ");
	out.print(b);
	out.println();
    }

    public void move(int color, int dir) {
	out.println("move " +
		    RRSquare.colorname[color] + " " +
		    dirname[dir]);
    }

    public void undo() {
	out.println("undo");
    }

    public void reset() {
	out.println("reset");
    }

    public void revoke () {
	out.println("revoke");
    }

    public void abandon () {
	out.println ("abandon");
    }

    public void nobid () {
	out.println ("nobid");
    }

    public void turn () {
	out.println ("turn");
    }

    public void pass () {
	out.println ("pass");
    }

    public void part () {
	out.println ("part");
    }

    public void quit () {
	out.println ("quit");
    }
    
    public void message (String text) {
	out.print ("message ");
	uniprint (text);
	out.println();
    }
}
