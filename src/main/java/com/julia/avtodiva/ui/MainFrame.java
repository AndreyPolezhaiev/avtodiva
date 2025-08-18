package com.julia.avtodiva.ui;

import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.AllSlotsPanel;
import com.julia.avtodiva.ui.panel.data.BookedSlotsPanel;
import com.julia.avtodiva.ui.panel.data.FreeSlotsPanel;
import com.julia.avtodiva.ui.panel.RangeSelectionPanel;
import com.julia.avtodiva.ui.panel.data.InstructorsPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel;

    @Autowired
    public MainFrame(RangeSelectionPanel rangeSelectionPanel,
                     FreeSlotsPanel freeSlotsPanel,
                     AllSlotsPanel allSlotsPanel,
                     BookedSlotsPanel bookedSlotsPanel,
                     InstructorsPanel instructorsPanel) {
        setTitle("Автошкола");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(cardLayout);
        mainPanel.add(rangeSelectionPanel, PanelName.RANGE_SELECTION_PANEL.name());
        mainPanel.add(freeSlotsPanel, PanelName.FREE_SLOTS_PANEL.name());
        mainPanel.add(allSlotsPanel, PanelName.ALL_SLOTS_PANEL.name());
        mainPanel.add(bookedSlotsPanel, PanelName.BOOKED_SLOTS_PANEL.name());
        mainPanel.add(instructorsPanel, PanelName.INSTRUCTOR_WEEKEND_PANEL.name());
        add(mainPanel);
        showPanel(PanelName.RANGE_SELECTION_PANEL.name());
    }

    public void showPanel(String name) {
        cardLayout.show(mainPanel, name.toUpperCase());
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
