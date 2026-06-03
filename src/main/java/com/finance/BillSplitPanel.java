package com.finance;

import javax.swing.*;
import java.awt.*;

public class BillSplitPanel extends JPanel {
    private final JTextField totalBillField = new JTextField(10);
    private final JTextField peopleField = new JTextField(10);
    private final JLabel splitResultLabel = new JLabel("Each Person Pays: $0.00");

    public BillSplitPanel() {
        setBorder(BorderFactory.createTitledBorder("Group Bill Splitter"));
        setLayout(new GridBagLayout());
        GridBagConstraints bsGbc = new GridBagConstraints();
        bsGbc.insets = new Insets(6, 6, 6, 6);
        bsGbc.fill = GridBagConstraints.HORIZONTAL;

        bsGbc.gridx = 0; bsGbc.gridy = 0; add(new JLabel("Total Bill ($):"), bsGbc);
        bsGbc.gridx = 1; add(totalBillField, bsGbc);

        bsGbc.gridx = 0; bsGbc.gridy = 1; add(new JLabel("Number of People:"), bsGbc);
        bsGbc.gridx = 1; add(peopleField, bsGbc);

        JButton calcBtn = new JButton("Calculate Split");
        bsGbc.gridx = 0; bsGbc.gridy = 2; bsGbc.gridwidth = 2;
        add(calcBtn, bsGbc);

        splitResultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        splitResultLabel.setForeground(new Color(39, 174, 96));
        bsGbc.gridy = 3;
        add(splitResultLabel, bsGbc);

        bsGbc.gridy = 4; bsGbc.weighty = 1.0;
        add(new JPanel(), bsGbc);

        calcBtn.addActionListener(e -> {
            try {
                double total = Double.parseDouble(totalBillField.getText().trim());
                int people = Integer.parseInt(peopleField.getText().trim());
                if (people <= 0) throw new IllegalArgumentException();
                splitResultLabel.setText(String.format("Each Person Pays: $%.2f", total / people));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid positive numbers for bill computations.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}