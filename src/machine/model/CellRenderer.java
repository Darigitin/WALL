/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machine.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.table.DefaultTableCellRenderer;
import machine.view.MachinePanel;

/**
 *
 * @author Ryan Ball
 */
public class CellRenderer extends DefaultTableCellRenderer {
    
    private final Font font = new Font("SansSerif", Font.PLAIN, 14);
    private final MachinePanel machine;
    
//    public CellRenderer() {
//        
//        super.setHorizontalAlignment(CENTER);
//        super.setHorizontalTextPosition(CENTER);
//        super.setVerticalAlignment(CENTER);
//        super.setOpaque(true);
//    }
    
    public CellRenderer(MachinePanel machine) {
        this.machine = machine;
        super.setHorizontalAlignment(CENTER);
        super.setHorizontalTextPosition(CENTER);
        super.setVerticalAlignment(CENTER);
        super.setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component l = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        
        if (hasFocus && isSelected) {
            setBackground(new Color(255, 160, 160));
        }
        else {
            setBackground(Color.white);
        }
        
        
        if (machine.getVisualStackFlag().equals("RSP")) {
            System.out.println("I am here motherfucker");
            setBackground(Color.red);
        }
        else if (machine.getVisualStackFlag().equals("RBP")) {
            setBackground(Color.red);
        }
        
        setForeground(Color.black);
        setFont(font);
        setText((String) value);
        setBorder(BorderFactory.createLineBorder(Color.black));
        return this;
    }
    
}
