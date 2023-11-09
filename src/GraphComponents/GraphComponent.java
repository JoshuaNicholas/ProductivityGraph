package GraphComponents;

import javax.swing.*;

public class GraphComponent {
    private String name;
    private int currentValue;
    private int baseValue;

    public GraphComponent(String name) {
        this.name = name;
        textLabel.setFont(GraphWindow.dataFont);
        UpdateText();
    }

    public JLabel GetOutLabel() {
        return textLabel;
    }

    private JLabel textLabel = new JLabel("null", SwingConstants.CENTER);
    void UpdateText() {
        //String indicator = (currentValue >= baseValue ? "#00aa00'>-" : "#aa0000'>+") + Math.abs(Math.round((currentValue-baseValue)/7f));
        String indicator = (currentValue >= baseValue ? "#00aa00'>-" : "#aa0000'>+") + Math.abs(currentValue-baseValue);
        //textLabel.setText("<html><nobr>" + name + ": " + Math.round(currentValue/7f) + " <font color='" + indicator + "</font></nobr></html>");
        textLabel.setText("<html><nobr>" + name + ": " + currentValue + " <font color='" + indicator + "</font></nobr></html>");
    }

    public void SetCurrentValue(int value) {
        currentValue = value;
        UpdateText();
    }

    public void SetBaseValue(int value) {
        baseValue = value;
        UpdateText();
    }

    public int GetCurrentValue() {
        return currentValue;
    }

    public int GetBaseValue() {
        return baseValue;
    }

    public String GetName() { return name; }
}
