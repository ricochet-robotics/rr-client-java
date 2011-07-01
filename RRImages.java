/*
 * Copyright © 2003, 2011 Bart Massey
 * [This program is licensed under the "MIT License"]
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

/*
 * Ricochet Robots Image Cache
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

class DimFilter extends RGBImageFilter {
    double fraction;

    public DimFilter(double fraction) {
	// The filter's operation does not depend on the
	// pixel's location, so IndexColorModels can be
	// filtered directly.
	this.fraction = fraction;
	canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
	int color = rgb & 0x00ffffff;
	int alpha = (rgb >> 24) & 0xff;
	alpha = (int)Math.floor(alpha * fraction);
	return (alpha << 24) | color;
    }
}

class RecolorFilter extends RGBImageFilter {
    double[] fraction = new double[3];

    public RecolorFilter(double red, double green, double blue) {
	// The filter's operation does not depend on the
	// pixel's location, so IndexColorModels can be
	// filtered directly.
	canFilterIndexColorModel = true;
	fraction[0] = red;
	fraction[1] = green;
	fraction[2] = blue;
    }

    public int filterRGB(int x, int y, int rgb) {
	for (int i = 0; i < 3; i++) {
	    int color = (rgb >>> ((2 - i) * 8)) & 0xff;
	    color = (int)Math.floor(color * fraction[i]);
	    if (color > 0xff)
		color = 0xff;
	    rgb &= ~(0xff << ((2 - i) * 8));
	    rgb |= color << ((2 - i) * 8);
	}
	return rgb;
    }
}

public class RRImages {
    public static final int CIRCLE = 0;
    public static final int SQUARE = 1;
    public static final int TRIANGLE = 2;
    public static final int STAR = 3;

    Image blank_image;
    Image[] target_images = new Image[17];
    Image[] dim_target_images = new Image[17];
    Image[] robot_images = new Image[4];
    Image[] colored_blank_images = new Image[5];

    RRTkProxy tkproxy;

    int[] colors = {
      RRSquare.RED,
      RRSquare.YELLOW,
      RRSquare.GREEN,
      RRSquare.BLUE
    };
    String[] colornames = {"red", "yellow", "green", "blue"};
    String[] shapenames = {"circle", "square", "triangle", "star"};

    private RRImages(RRTkProxy tkproxy) {
        this.tkproxy = tkproxy;
	/* get the floor tile */
        blank_image = tkproxy.getImage("images/blank.png");
	/* get the target symbols */
        for (int i = 0; i < 16; i++)
            target_images[i] =
                tkproxy.getImage("images/target-" +
                           colornames[colors[i / 4]] + "-" +
                           shapenames[i % 4] + ".png");
        target_images[16] = tkproxy.getImage("images/target-whirlpool.png");
	/* make dimmed versions of the target symbols */
	ImageFilter dim_filter = new DimFilter(0.15);
	for (int i = 0; i < 17; i++) {
	    ImageProducer img = target_images[i].getSource();
	    FilteredImageSource src =
		new FilteredImageSource(img, dim_filter);
	    dim_target_images[i] = tkproxy.createImage(src);
	}
	/* get the robot symbols */
        for (int i = 0; i < 4; i++)
            robot_images[i] = tkproxy.getImage("images/robot-" +
                                         colornames[colors[i]] + ".png");
	/* make the special floor overlays */
	double big = 1.7;
	double med = 1.5;
	double small = 0.7;
	double[][] fa = {
	    {big,small,small},
	    {med,med,small},
	    {small,big,small},
	    {small,small,big},
	    {small,small,small}};
	ImageProducer s = blank_image.getSource();
	for (int i = 0; i < 5; i++) {
	    double[] fai = fa[i];
	    RecolorFilter f = new RecolorFilter(fai[0], fai[1], fai[2]);
	    FilteredImageSource src = new FilteredImageSource(s, f);
	    colored_blank_images[i] = tkproxy.createImage(src);
	}
    }

    public RRImages(Toolkit tk) {
        this(new RRTkProxy(tk));
    }

    public RRImages(JApplet applet) {
        this(new RRTkProxy(applet));
    }

    public Image getBlankImage() {
	return blank_image;
    }

    public Image getTargetImage(int index, boolean primary) {
	if (primary)
	    return target_images[index];
	return dim_target_images[index];
    }

    public Image getRobotImage(int index) {
        return robot_images[index];
    }

    public Image getColoredBlankImage(int index) {
	return colored_blank_images[index];
    }
}
