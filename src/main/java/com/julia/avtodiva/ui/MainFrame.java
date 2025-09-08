package com.julia.avtodiva.ui;

import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.*;
import com.julia.avtodiva.ui.panel.RangeSelectionPanel;
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
                     InstructorsPanel instructorsPanel,
                     SearchSlotsPanel searchSlotsPanel) {
        setTitle("Автошкола");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(cardLayout);
        JScrollPane rangeScroll = createScrollPane(rangeSelectionPanel);
        JScrollPane freeScroll = createScrollPane(freeSlotsPanel);
        JScrollPane allScroll = createScrollPane(allSlotsPanel);
        JScrollPane bookedScroll = createScrollPane(bookedSlotsPanel);
        JScrollPane instructorScroll = createScrollPane(instructorsPanel);

        mainPanel.add(rangeScroll, PanelName.RANGE_SELECTION_PANEL.name());
        mainPanel.add(freeScroll, PanelName.FREE_SLOTS_PANEL.name());
        mainPanel.add(allScroll, PanelName.ALL_SLOTS_PANEL.name());
        mainPanel.add(bookedScroll, PanelName.BOOKED_SLOTS_PANEL.name());
        mainPanel.add(instructorScroll, PanelName.INSTRUCTOR_WEEKEND_PANEL.name());
        mainPanel.add(searchSlotsPanel, PanelName.SEARCH_SLOTS_PANEL.name());
        add(mainPanel);
        showPanel(PanelName.RANGE_SELECTION_PANEL.name());
    }

    private JScrollPane createScrollPane(JComponent comp) {
        JScrollPane scrollPane = new JScrollPane(comp);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);   // скорость колёсика
        scrollPane.getVerticalScrollBar().setBlockIncrement(50);  // PgUp/PgDn
        return scrollPane;
    }

    public void showPanel(String name) {
        cardLayout.show(mainPanel, name.toUpperCase());
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
