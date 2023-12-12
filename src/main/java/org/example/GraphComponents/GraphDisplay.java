package org.example.GraphComponents;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphDisplay extends JPanel {
    List<GraphComponent> components = new ArrayList<>();

    final float xMarginPct = 0.02f, yMarginPct = 0.02f;
    int xMargin = 0, yMargin = 0;

    public GraphDisplay() {
        super();
    }

    public void AddComponent(String name) {
        AddComponent(new GraphComponent(name));
    }

    public void AddComponent(GraphComponent component) {
        if (component == null)
            return;
        components.add(component);

        //component.SetBaseValue(2 + (int) (Math.random() * 18));
        //component.SetCurrentValue(2 + (int) (Math.random() * 18));
    }

    public GraphComponent GetComponent(String name) {
        for (GraphComponent component : components)
            if (component.GetName().equals(name))
                return component;
        return null;
    }

    public GraphComponent GetFastest() {
        if (components.size() == 0)
            return new GraphComponent("ERROR_NoSheetItems");

        GraphComponent current = components.get(0);
        for (GraphComponent comp : components)
            if (comp.GetCurrentValue() < current.GetCurrentValue())
                current = comp;

        return current;
    }

    public GraphComponent GetSlowest() {
        if (components.size() == 0)
            return new GraphComponent("ERROR_NoSheetItems");

        GraphComponent current = components.get(0);
        for (GraphComponent comp : components)
            if (comp.GetCurrentValue() > current.GetCurrentValue())
                current = comp;

        return current;
    }

    public void Update() {
        xMargin = (int)(getWidth() * xMarginPct);
        yMargin = (int)(getHeight() * yMarginPct);
        repaint();
    }

    private int highestValue = 0;

    public void SetHighestValue(int value) {
        highestValue = value;
    }

    public int GetHighestValue() {
        highestValue = 0;
        for (GraphComponent component : components) {
            if (component.GetBaseValue() > highestValue)
                highestValue = component.GetBaseValue();
            if (component.GetCurrentValue() > highestValue)
                highestValue = component.GetCurrentValue();
        }

        return highestValue;
    }

    public void paintComponent(Graphics _g) {
        Graphics2D g = (Graphics2D) _g;

        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g.setRenderingHints(desktopHints);
        }

        super.paintComponent(g);

        int usableX = getWidth() - xMargin * 2;
        //int usableY = getHeight() - yMargin;
        g.setFont(GraphWindow.markerFont);
        g.setColor(GraphComponent.MarkerColor);

        // Draw scale markers
        for (int i = 0; i < highestValue + 1; i++) {
            int xVal = (int) ((float) i / (highestValue + 1) * usableX) + xMargin;

            g.drawLine(xVal, 0, xVal, getHeight());
            if (highestValue > 20 && i % 2 == 0)
                g.drawString(i + "", xVal + 5, getHeight() - g.getFontMetrics().getHeight()/4);
        }

        DrawComponents(g);

        System.out.println();
    }

    private void DrawComponents(Graphics _g) {
        Graphics2D g = (Graphics2D) _g;

        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g.setRenderingHints(desktopHints);
        }

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int usableX = getWidth() - xMargin * 2;
        int usableY = getHeight() - yMargin;

        int yHeight = 0;
        if (components.size() > 0)
            yHeight = usableY/components.size() - yMargin/components.size();

        // Draw components to self
        for (int i = 0; i < components.size(); i++) {
            GraphComponent component = components.get(i);
            Image img = component.DrawBar(usableX, yHeight - yMargin, highestValue + 1);

            int yPos = (int)((float)i * yHeight);

            g.drawImage(img, xMargin, yMargin + yPos, null);
        }
    }
}
