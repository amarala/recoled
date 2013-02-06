package cowrat.recoled.client;

/*
 * @(#)LineBorder.java	1.21 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Component;
import javax.swing.border.* ;

/**
 * A class which implements a line border of arbitrary thickness
 * and of a single color.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version 1.21 12/03/01
 * @author David Kloba
 */
public class IncompleteLineBorder extends AbstractBorder
{
    protected Color lineColor;

    protected boolean excludeTop ;
    protected boolean excludeBottom ;
    protected boolean excludeLeft ;
    protected boolean excludeRight ;

    /** 
     * Creates a line border with the specified color and a 
     * thickness = 1.
     * @param color the color for the border
     */
    public IncompleteLineBorder(Color color) {
        lineColor = color ;
	excludeTop = false ;
	excludeBottom = false ;
	excludeLeft = false ;
	excludeRight = false ;
    }

    public void setExcludeTop(boolean b) { excludeTop = b ; }
    public void setExcludeBottom(boolean b) { excludeBottom = b ; }
    public void setExcludeLeft(boolean b) { excludeLeft = b ; }
    public void setExcludeRight(boolean b) { excludeRight = b ; }

    /**
     * Paints the border for the specified component with the 
     * specified position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int i;

        g.setColor(lineColor);
	if(!excludeTop) g.drawLine(x, y, x + width, y) ;
	if(!excludeBottom) g.drawLine(x, y + height, x + width, y+ height) ;
	if(!excludeLeft) g.drawLine(x, y, x, y+height) ;
	if(!excludeRight) g.drawLine(x + width - 1, y, x + width - 1, y + height) ;
        
        g.setColor(oldColor);
    }

    /**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    public Insets getBorderInsets(Component c)       {
        return new Insets(1,1,1,1);
    }

    /** 
     * Reinitialize the insets parameter with this Border's current Insets. 
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 1;
        return insets;
    }

    /**
     * Returns the color of the border.
     */
    public Color getLineColor()     {
        return lineColor;
    }

    /**
     * Returns the thickness of the border.
     */
    public int getThickness()       {
        return 1;
    }

    /**
     * Returns whether or not the border is opaque.
     */
    public boolean isBorderOpaque() { 
        return !true; 
    }

}
