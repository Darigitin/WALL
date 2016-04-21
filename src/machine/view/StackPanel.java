/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machine.view;

import javax.swing.BoxLayout;

/**
 *
 * @author Ryan Ball
 */
public class StackPanel extends javax.swing.JPanel {

    private int numOfRecords = 0;
    
    /**
     * Creates new form StackPanel
     */
    public StackPanel() {
        initComponents();
        
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(new ActivationRecord());
    }
    
    /**
     * Adds a record to the bottom of the StackPanel
     * 
     * @param ar 
     */
    public void addRecord(ActivationRecord ar) {
        ((ActivationRecord)this.getComponent(numOfRecords))
                .setTextAreaVisible(false);
        this.add(ar);
        numOfRecords++;
    }
    
    /**
     * Removes the bottom record from the StackPanel
     */
    public void removeRecord() {
        this.remove(numOfRecords--);
        ((ActivationRecord)this.getComponent(numOfRecords))
                .setTextAreaVisible(true);
    }
    
    /**
     * Removes all entries on the StackPanel
     */
    public void resetRecords() {
        while (numOfRecords != 0) {
            this.removeRecord();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
