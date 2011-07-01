/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots GUI Client
 * Created on May 31, 2003
 *
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class RRClient extends JApplet {

    public static void main(String[] args)
      throws IOException {
	if (args.length != 4) {
	    System.err.println("usage: java RRClient <host> [join|watch] <user> <game>");
	    System.exit(1);
	}
    	Socket s = new Socket(args[0], 5252);
	final RRController ctl = new RRController();
	ctl.netout = new RRNetOut(s, ctl);
	ctl.board = new RRBoard(ctl, args[1]);
	ctl.images = new RRImages(Toolkit.getDefaultToolkit());
	ctl.boardPanel = new RRBoardPanel(ctl);
	ctl.netin = new RRNetIn(s, ctl);
        JFrame f = new JFrame("Ricochet Robots");
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        f.getContentPane().add(ctl.boardPanel, BorderLayout.CENTER);
	
	String[] intstrings = {"3", "4", "5", "6", "7", "8", "9", "10"};
	JComboBox bidBox = new JComboBox(intstrings);
	bidBox.setEditable(true);
	bidBox.setSelectedItem("");
	/* bidBox.setMaximumRowCount(3); */
	bidBox.setLightWeightPopupEnabled(false);
	bidBox.setMaximumSize(bidBox.getMinimumSize());
	class BidListener implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
		JComboBox b = (JComboBox) e.getSource();
		ctl.netout.bid(Integer.parseInt((String)(b.getSelectedItem())));
	    }
	};
	BidListener bid_listener = new BidListener();
	bidBox.addActionListener(bid_listener);

	JPanel bx = new JPanel();
	Border border = BorderFactory.createTitledBorder(null, "Bid");
	bx.setBorder(border);
	bx.add(bidBox);

	Box controlPanel = new Box(BoxLayout.Y_AXIS);

	Box buttonPanel = new Box(BoxLayout.Y_AXIS);

	/* reset turn button */
	class ResetListener implements ActionListener {
	    public void actionPerformed (ActionEvent e) {
		ctl.netout.reset ();
	    }
	}
	ResetListener reset_listener = new ResetListener();
	
	Button resetButton = new Button ("Reset");
	resetButton.addActionListener (reset_listener);
	
	buttonPanel.add(resetButton);
	
	/* next turn button */
	class NextListener implements ActionListener {
	    public void actionPerformed (ActionEvent e) {
		ctl.netout.turn ();
	    }
	}
	NextListener next_listener = new NextListener();
	
	Button nextButton = new Button ("Next");
	nextButton.addActionListener (next_listener);
	
	buttonPanel.add(nextButton);
	
	/* Zap timer button */
	class ZapListener implements ActionListener {
	    public void actionPerformed (ActionEvent e) {
		ctl.netout.nobid ();
	    }
	}
	ZapListener zap_listener = new ZapListener();
	
	Button zapButton = new Button ("Zap Timer");
	zapButton.addActionListener (zap_listener);
	
	buttonPanel.add(zapButton);
	
	/* Quit button */
	class QuitListener implements ActionListener {
	    public void actionPerformed (ActionEvent e) {
		ctl.netout.quit ();
	    }
	}
	QuitListener quit_listener = new QuitListener();
	
	Button quitButton = new Button ("Quit");
	quitButton.addActionListener (quit_listener);
	
	buttonPanel.add(quitButton);
	
	/* Message Display */
	ctl.messages = new RRMessages (10, 30);
	ctl.messages.setEditable (false);

	/* Messages live below the board */

	JPanel mx = new JPanel ();
	mx.setLayout (new BorderLayout());
	
	mx.add(ctl.messages, BorderLayout.CENTER);

	class MessageListener implements ActionListener {
	    public void actionPerformed (ActionEvent e) {
		TextField f = (TextField) e.getSource ();
		ctl.netout.message (f.getText ());
		f.setText ("");
	    }
	}
		
	MessageListener message_listener = new MessageListener ();
	
	TextField message = new TextField (30);
	message.setEditable (true);
	message.addActionListener (message_listener);

	mx.add (message, BorderLayout.SOUTH);
	
	f.getContentPane().add (mx, BorderLayout.SOUTH);
	
	JPanel buttonPad = new JPanel();
	buttonPad.setLayout(new BorderLayout());
	buttonPad.add(buttonPanel, BorderLayout.NORTH);

	controlPanel.add(bx);
	controlPanel.add(buttonPad);

	controlPanel.add(controlPanel.createGlue());

	f.getContentPane().add(controlPanel, BorderLayout.EAST);

        f.setSize(new Dimension(496, 556));
        f.setVisible(true);

	ctl.netin.start();
	ctl.netout.hello(args[2]);
	if (args[1].equals("watch"))
	     ctl.netout.watch(args[3]);
	else if (args[1].equals("join"))
	     ctl.netout.join(args[3]);
	ctl.netout.show();
    }

}
