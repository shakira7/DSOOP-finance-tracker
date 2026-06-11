package com.finance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

import javax.swing.JPanel;

// expense distribution pie chart
public class PieChartPanel extends JPanel {
    private Map<String, Double> categoryTotals;

    public PieChartPanel() {
        setPreferredSize(new Dimension(380, 200));
        setBackground(Color.WHITE);
    }

    public void updateData(Map<String, Double> totals) {
        this.categoryTotals = totals;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (categoryTotals == null || categoryTotals.isEmpty()) {
            g2.drawString("No data matching current filters.", 20, 30);
            return;
        }

        double totalSpend = 0;
        for (double val : categoryTotals.values()) totalSpend += val;

        if (totalSpend == 0) {
            g2.drawString("Total filtered spending is $0.00", 20, 30);
            return;
        }

        String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
        Color[] colors = {
            new Color(46, 204, 113), new Color(231, 76, 60), 
            new Color(52, 152, 219), new Color(241, 196, 15), 
            new Color(149, 165, 166)
        };

        int startAngle = 0;
        int x = 15, y = 30, width = 140, height = 140;

        for (int i = 0; i < categories.length; i++) {
            double value = categoryTotals.getOrDefault(categories[i], 0.0);
            if (value > 0) {
                int arcAngle = (int) Math.round((value / totalSpend) * 360.0);
                g2.setColor(colors[i]);
                g2.fillArc(x, y, width, height, startAngle, arcAngle);
                startAngle += arcAngle;
            }
        }

        int legendX = 175, legendY = 40;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i < categories.length; i++) {
            g2.setColor(colors[i]);
            g2.fillRect(legendX, legendY + (i * 24), 12, 12);
            g2.setColor(Color.BLACK);
            double value = categoryTotals.getOrDefault(categories[i], 0.0);
            g2.drawString(String.format("%s ($%.2f)", categories[i], value), legendX + 20, legendY + 10 + (i * 24));
        }
    }
}