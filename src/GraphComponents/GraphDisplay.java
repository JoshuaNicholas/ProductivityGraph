package GraphComponents;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        components.add(component);

        component.SetBaseValue(2 + (int) (Math.random() * 18));
        component.SetCurrentValue(2 + (int) (Math.random() * 18));
    }

    public GraphComponent GetComponent(String name) {
        for (GraphComponent component : components)
            if (component.GetName().equals(name))
                return component;
        return null;
    }

    public GraphComponent GetFastest() {
        if (components.size() == 0)
            return null;

        GraphComponent current = components.get(0);
        for (GraphComponent comp : components)
            if (comp.GetCurrentValue() < current.GetCurrentValue())
                current = comp;

        return current;
    }

    public GraphComponent GetSlowest() {
        if (components.size() == 0)
            return null;

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

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int usableX = getWidth() - xMargin * 2;
        int usableY = getHeight() - yMargin;
        g.setFont(GraphWindow.markerFont);

        // Draw scale markers
        for (int i = 0; i < highestValue + 1; i++) {
            int xVal = (int) ((float) i / (highestValue + 1) * usableX) + xMargin;

            g.drawLine(xVal, 0, xVal, getHeight());
            g.drawString(i + "", xVal + 5, getHeight() - g.getFontMetrics().getHeight()/4);
        }

        DrawComponents(g);

        System.out.println();
    }

    private void DrawComponents(Graphics g) {
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
