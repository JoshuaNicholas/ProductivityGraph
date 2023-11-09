package GraphComponents;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphDisplay extends JPanel {
    List<GraphComponent> components = new ArrayList<>();
    GridBagConstraints c = new GridBagConstraints();

    final float xMarginPct = 0.02f, yMarginPct = 0.02f;
    int xMargin = 0, yMargin = 0;

    public GraphDisplay() {
        super(new GridBagLayout());

        c.weightx = 1.0f;
        c.weighty = 1.0f;
        c.fill = GridBagConstraints.BOTH;

        xMargin = (int) (xMarginPct * getWidth());
        yMargin = (int) (yMarginPct * getHeight());
        c.insets = new Insets(yMargin, xMargin, yMargin, xMargin);
    }

    public void AddComponent(String name) {
        AddComponent(new GraphComponent(name));
    }

    public void AddComponent(GraphComponent component) {
        components.add(component);
        needsRefresh = true;

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

    boolean needsRefresh = false;
    public void Update() {
        if (needsRefresh) {
            components.sort(Comparator.comparingInt(GraphComponent::GetCurrentValue));
            addAllComponents();
        }
    }

    private void addAllComponents() {
        clearFrame();

        int maxTableWidth = getWidth() / GetMaxWidth() - 3;
        int maxTableHeight = 0;
        int i = 0;

        for (int y = 0; maxTableHeight == 0 || y < maxTableHeight; y++) {
            c.gridy = y;
            for (int x = 0; maxTableWidth == 0 || x < maxTableWidth; x++) {
                c.gridx = x;

                add(components.get(i).GetOutLabel(), c);

                i++;
                if (i >= components.size())
                    return;
            }
        }

        revalidate();
        needsRefresh = false;
    }

    private int GetMaxWidth() {
        int highestWidth = 1;

        for (GraphComponent comp : components)
            if (comp.GetOutLabel().getWidth() > highestWidth)
                highestWidth = (int) comp.GetOutLabel().getPreferredSize().getWidth();

        return highestWidth;
    }

    void clearFrame() {
        removeAll();
        //repaint();
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
}
