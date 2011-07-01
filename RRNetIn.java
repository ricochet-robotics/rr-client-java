/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Server Monitor Connection
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

class Lexer {
    BufferedReader in;
    public boolean saw_eol = false;
    public boolean saw_eof = false;
    
    public Lexer(BufferedReader in) {
	this.in = in;
    }

    public String next()
      throws IOException {
	boolean in_token = false;
	boolean string_token = false;
	saw_eol = false;
	StringBuffer b = new StringBuffer();
	while (true) {
	    int ch = in.read();
	    if (string_token && ch == '"')
		break;
	    if (ch == -1) {
		saw_eof = true;
		return null;
	    }
	    if (string_token) {
		b.append((char)ch);
		continue;
	    }
	    if (ch == '\n') {
		saw_eol = true;
		if (in_token)
		    break;
		return null;
	    }
	    if (ch == ' ' || ch == '\t') {
		if (in_token)
		    break;
		continue;
	    }
	    if (!in_token && ch == '"') {
		in_token = true;
		string_token = true;
		continue;
	    }
	    in_token = true;
	    b.append((char)ch);
	}
	return b.toString();
    }
}

public class RRNetIn
  extends Thread {
    Lexer in;
    RRController ctl;
    
    public RRNetIn(Socket s, RRController ctl)
      throws IOException {
	this.ctl = ctl;
	InputStream si = s.getInputStream();
	InputStreamReader sr = new InputStreamReader(si);
	BufferedReader sb = new BufferedReader(sr);
	in = new Lexer(sb);
    }

    interface NoticeHandler {
	public boolean match(String[] notice) throws IOException;
    }

    class ShowHandler implements NoticeHandler {
	public boolean match(String[] notice)
	  throws IOException {
	    if (notice.length != 2 || !notice[0].equals("SHOW"))
		return false;
	    ctl.board.become(notice[1]);
	    ctl.boardPanel.repaint();
	    return true;
	}
    }

    class TurnHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 4 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("TURN"))
		return false;
	    ctl.netout.show();
	    return true;
	}
    }

    class ActiveHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 4 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("ACTIVE"))
		return false;
	    if (!notice[2].equals(ctl.board.playerName)) {
		ctl.board.setActive(false);
		return true;
	    }
	    ctl.board.setActive(true);
	    return true;
	}
    }

    class DoneHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 3 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("GAMESTATE") ||
		!notice[2].equals("DONE"))
		return false;
	    ctl.board.setActive(false);
	    return true;
	}
    }

    class ResetHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 2 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("RESET"))
		return false;
	    return true;
	}
    }

    class MessageHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 4 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("MESSAGE"))
		return false;
	    ctl.messages.message (notice[2], notice[3]);
	    return true;
	}
    }

    static  int lookup_color(String name) {
	for (int i = 0; i < RRSquare.colorname.length; i++)
	    if (RRSquare.colorname[i].equals(name))
		return i;
	return -1;
    }

    class PositionHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 5 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("POSITION"))
		return false;
	    RRBoardCoord coord =
		new RRBoardCoord(Integer.parseInt(notice[4]),
				 Integer.parseInt(notice[3]));
	    ctl.board.moveRobot(lookup_color(notice[2]), coord);
	    ctl.boardPanel.repaint();
	    return true;
	}
    }

    class TimerHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 3 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("TIMER"))
		return false;
	    ctl.messages.message (notice[2]);
	    ctl.messages.message (" seconds remaining\n");
	    return true;
	}
    }

    class UserHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 3 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("USER"))
		return false;
	    ctl.messages.message (notice[2]);
	    ctl.messages.message (" has connected to the server.\n");
	    return true;
	}
    }
    
    class JoinHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 3 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("JOIN"))
		return false;
	    ctl.messages.message (notice[2]);
	    ctl.messages.message (" has joined the game.\n");
	    return true;
	}
    }
    
    class PartHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length != 3 ||
		!notice[0].equals("NOTICE") ||
		!notice[1].equals("PART"))
		return false;
	    ctl.messages.message (notice[2]);
	    ctl.messages.message (" has left the game.\n");
	    return true;
	}
    }
    
    class DefaultHandler implements NoticeHandler {
	public boolean match(String[] notice) {
	    if (notice.length > 0)
		ctl.messages.message (notice[0]);
	    for (int i = 1; i < notice.length; i++) {
		ctl.messages.message (" ");
		ctl.messages.message (notice[i]);
	    }
	    ctl.messages.message ("\n");
	    return true;
	}
    }


    NoticeHandler handlers[] = {
	new ShowHandler(),
	new TurnHandler(),
	new PositionHandler(),
	new ActiveHandler(),
	new DoneHandler(),
	new ResetHandler(),
	new MessageHandler(),
	new TimerHandler(),
	new UserHandler (),
	new JoinHandler(),
	new PartHandler(),
	new DefaultHandler()
    };

    public void run() {
	try {
	    while(true) {
		Vector v = new Vector();
		while (true) {
		    String s = in.next();
		    if (s != null)
			v.add(s);
		    if (in.saw_eol || in.saw_eof)
			break;
		}
		String[] notice = new String[v.size()];
		for (int i = 0; i < notice.length; i++)
		    notice[i] = (String)v.elementAt(i);
		for (int i = 0; i < handlers.length; i++)
		    if (handlers[i].match(notice))
			break;
		if (in.saw_eof)
		    return;
	    }
	} catch (IOException e) {
	    System.err.println("I/O Exception in RRNetIn");
	    e.printStackTrace(System.err);
	}
    }
}
