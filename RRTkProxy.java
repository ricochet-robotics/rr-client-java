/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Toolkit Proxy
 * Created on May 31, 2003
 *
 */

/**
 * @author Bart Massey <bart@cs.pdx.edu>
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class RRTkProxy {
    JApplet applet = null;
    Toolkit tk = null;
    
    RRTkProxy(JApplet applet) { this.applet = applet; }
    RRTkProxy(Toolkit tk) { this.tk = tk; }

    Image getImage(String name) {
	Image result;
        if (tk != null)
            result = tk.getImage(name);
        else if (applet != null)
            result = applet.getImage(applet.getCodeBase(), name);
	else
            throw new Error("No image access");
	if (result == null)
	    throw new Error("Can't load image " + name);
	return result;
    }

    Image createImage(ImageProducer src) {
	Image result;
        if (tk != null)
            result = tk.createImage(src);
        else if (applet != null)
            result = applet.createImage(src);
	else
            throw new Error("No image creation");
	if (result == null)
	    throw new Error("Can't create image");
	return result;
    }
}

