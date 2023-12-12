package org.example.GraphComponents;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Map;

public class GraphComponent {
    static final float ColorWidthPct = 0f;
    static final Color BaseColor = new Color(0x236466);
    static final Color AheadColor = new Color(0x55c07a);
    static final Color BehindColor = new Color(0xb52b29);
    static final Color MarkerColor = new Color(0x6bb5f4);


    private final String name;
    private int currentValue;
    private int baseValue;

    public GraphComponent(String name) {
        this.name = name;
    }

    public Image DrawBar(int width, int height, int highestValue) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) image.getGraphics();
        // Transparent background
        g.setBackground(new Color(0x00000000, true));

        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null)
            g.setRenderingHints(desktopHints);

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setFont(GraphWindow.dataFont);

        int baseValuePosition = (int)((float)baseValue/(float)highestValue * width);
        int currentValuePosition = (int)((float)currentValue/(float)highestValue * width);

        // Fill bar for current value
        g.setColor(BaseColor);
        g.fillRoundRect(0, 2, currentValuePosition, height-4, 16, 16);

        if (currentValue < baseValue) {
            // Good color!
            g.setColor(AheadColor);
            g.fillRoundRect(currentValuePosition, 2 + (int)(height*ColorWidthPct/2), baseValuePosition - currentValuePosition, height-4 - (int)(height*ColorWidthPct), 16, 16);
        }
        else if (currentValue != baseValue) {
            // Running overtime
            g.setColor(BehindColor);
            g.fillRoundRect(baseValuePosition, 2 + (int)(height*ColorWidthPct/2), currentValuePosition - baseValuePosition, height-4 - (int)(height*ColorWidthPct), 16, 16);
        }

        // Draw baseline marker
        g.setColor(MarkerColor);
        g.fillRoundRect(baseValuePosition - 2, 0, 5, height, 4, 4);

        g.setColor(GraphWindow.textColor);

        // Right-align name text
        //g.drawString(name, width - g.getFontMetrics().stringWidth(name) - 10, height/2);

        // Left-align name text
        Rectangle nameBounds = getStringBounds(g, name, 4, 0);
        Rectangle valueBounds = getStringBounds(g, "" + currentValue, 0, 0);

        // Use valueBounds to ensure text is level
        g.drawString(name, 4, height/2 + valueBounds.height/2);

        g.setColor(MarkerColor);

        // Value
        int xPos = (currentValuePosition - valueBounds.width > width) ? width - valueBounds.width - 4 : currentValuePosition + 4;
        // Don't draw text if would overlap with name.
        if ((nameBounds.x + nameBounds.width < xPos) && !(currentValue < baseValue && (xPos - valueBounds.width - 12) < nameBounds.width))
            g.drawString(currentValue + "", (currentValue < baseValue ? xPos - valueBounds.width - 8 : xPos), height/2 + valueBounds.height/2);

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
