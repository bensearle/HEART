/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author bsearle
 */
public class Interface extends javax.swing.JFrame {
Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    /**
     * Creates new form Interface
     */
    public Interface() {
        initComponents();
         conn = JavaConnect.ConnectDB();  //  get the connection url
         labelSelected.setText("BROWSER");
         updateTablePrimary();
         updateTableSecondary();
         updateTableSelected();
    }

    
     /**
     * method populates the table with data from the SQL Server
     */
    public void updateTablePrimary(){
        String selected = labelSelected.getText();
        try {
            String sql = "SELECT primaryItem AS [Primary to " + selected + "] FROM connections WHERE secondaryItem = '" + selected + "';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            tablePrimary.setModel(DbUtils.resultSetToTableModel(rs));
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    /**
     * method populates the table with data from the SQL Server
     */
    public void updateTableSecondary(){
        String selected = labelSelected.getText();
        try {
            String sql = "SELECT secondaryItem AS [Secondary to " + selected + "] FROM connections WHERE primaryItem = '" + selected + "';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            tableSecondary.setModel(DbUtils.resultSetToTableModel(rs));
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    /**
     * method populates the table with data from the SQL Server
     */
    public void updateTableSelected(){
        String selected = labelSelected.getText();
        try {
            String sql = "SELECT loc AS [Lines of Code], totalCon AS [Total Connections] FROM allMimics WHERE mimicName = '" + selected + "';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            tableSelected.setModel(DbUtils.resultSetToTableModel(rs));
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, e);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        tablePrimary = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableSecondary = new javax.swing.JTable();
        labelSelected = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableSelected = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1600, 900));
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tablePrimary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablePrimary.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablePrimaryMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tablePrimary);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        tableSecondary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableSecondary.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSecondaryMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableSecondary);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        getContentPane().add(jScrollPane2, gridBagConstraints);

        labelSelected.setText("SelectedItem");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(labelSelected, gridBagConstraints);

        tableSelected.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tableSelected);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        getContentPane().add(jScrollPane4, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tablePrimaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablePrimaryMouseClicked
        int row = tablePrimary.getSelectedRow();  //  define row as the selected row of the table
        String pr = tablePrimary.getModel().getValueAt(row, 0).toString();  //  get seleted project from table
        labelSelected.setText(pr);  //  set selected item to selected project
        updateTablePrimary();
        updateTableSecondary();
        updateTableSelected();
    }//GEN-LAST:event_tablePrimaryMouseClicked

    private void tableSecondaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSecondaryMouseClicked
        int row = tableSecondary.getSelectedRow();  //  define row as the selected row of the table
        String pr = tableSecondary.getModel().getValueAt(row, 0).toString();  //  get seleted project from table
        labelSelected.setText(pr);  //  set selected item to selected project
        updateTablePrimary();
        updateTableSecondary();
        updateTableSelected();
    }//GEN-LAST:event_tableSecondaryMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel labelSelected;
    private javax.swing.JTable tablePrimary;
    private javax.swing.JTable tableSecondary;
    private javax.swing.JTable tableSelected;
    // End of variables declaration//GEN-END:variables
}
