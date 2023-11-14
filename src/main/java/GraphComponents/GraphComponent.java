package GraphComponents;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

public class GraphComponent {
    private String name;
    private int currentValue;
    private int baseValue;

    public GraphComponent(String name) {
        this.name = name;
    }

    //Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
    Color color = new Color(29, 64, 107);
    Color compColor = new Color(0xFFFFFF - color.getRGB());

    public Image DrawBar(int width, int height, int highestValue) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setFont(GraphWindow.dataFont);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int baseValuePosition = (int)((float)baseValue/(float)highestValue * width);
        int currentValuePosition = (int)((float)currentValue/(float)highestValue * width);

        // Fill bar for current value
        g.setColor(color);
        g.fillRect(0, 2, currentValuePosition, height-4);

        if (currentValue < baseValue) {
            // Good color!
            g.setColor(Color.green);
            g.fillRect(currentValuePosition, 2, baseValuePosition - currentValuePosition, height-4);
        }
        else if (currentValue != baseValue) {
            // Running overtime
            g.setColor(Color.red);
            g.fillRect(baseValuePosition, 2, currentValuePosition - baseValuePosition, height-4);
        }

        // Draw baseline marker
        g.setColor(Color.black);
        g.fillRect(baseValuePosition - 2, 0, 5, height);

        //g.setColor(new Color(238,238,238));
        //Rectangle nameBounds = getStringBounds(g, name, width - g.getFontMetrics().stringWidth(name) - 10, height/2);
        //g.fillRect(nameBounds.x, nameBounds.y, nameBounds.width, nameBounds.height);

        g.setColor(Color.white);

        // Right-align name text
        //g.drawString(name, width - g.getFontMetrics().stringWidth(name) - 10, height/2);

        // Left-align name text
        g.drawString(name, 4, height/2 + getStringBounds(g, name, 0, 0).height/2);

        // Value
        g.drawString(currentValue + "", currentValuePosition, height/2 + getStringBounds(g, "" + currentValue, 0, 0).height/2);

        return image;
    }

    public void SetCurrentValue(int value) {
        currentValue = value;
    }

    public void SetBaseValue(int value) {
        baseValue = value;
    }

    public int GetCurrentValue() {
        return currentValue;
    }

    public int GetBaseValue() {
        return baseValue;
    }

    public String GetName() { return name; }

    // GetStringBounds from @ranieribt on StackOverflow
    private Rectangle getStringBounds(Graphics2D g2, String str, float x, float y)
    {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        return gv.getPixelBounds(null, x, y);
    }
}
