/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import net.proteanit.sql.DbUtils;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tree;
import prefuse.data.io.TreeMLReader;
import prefuse.util.FontLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;

/**
 *
 * @author bsearle
 */
public class View extends javax.swing.JFrame {
    private static String treeNodes;

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    /**
     * Creates new form View
     */
    public View() {
        initComponents();
        conn = JavaConnect.ConnectDB();  //  get the connection url
        populateCombo();
        getObjects();
        getMimics();
        countMimics();
        countObjects();
    }

    public void populateCombo() {
        //  populate servers
        selectServer.removeAllItems();
        selectServer.addItem("All");
        selectServer.addItem("Heathrow");
        selectServer.addItem("Heathro2");
        selectServer.addItem("Heathro3");
        selectServer.addItem("Heathro4");
        selectServer.addItem("Heathro5");

        //  populate object order by options
        orderObjects.removeAllItems();
        orderObjects.addItem("Server");
        orderObjects.addItem("Mimic");
        orderObjects.addItem("Object");
        orderObjects.addItem("Times Referenced");

        //  populate mimic order by options
        orderMimics.removeAllItems();
        orderMimics.addItem("Server");
        orderMimics.addItem("Mimic");
    }

    /**
     * method populates the table with data from the SQL Server
     */
    public void getObjects() {
        String where = "";  //  String for WHERE condition of SQL statement
        String orderBy = "";  //  String for ORDER BY condition of SQL statement

        //  if the server combobox is poulated and not equal to index 0 (ALL), then set WHERE condition
        if (selectServer.getItemCount() > 0 && selectServer.getSelectedIndex() > 0) {
            String server = selectServer.getSelectedItem().toString();  //  convert the selected server to a String
            where = "WHERE server = '" + server + "'";  //  set the WHERE condition for what the server should equal
        }

        //  if the orderBy combobox is poulated, then set ORDER BY condition
        if (orderObjects.getItemCount() > 0) {
            String order = orderObjects.getSelectedItem().toString();  //  convert the selected server to a String
            //  set SQL ORDER BY command based on selected item
            switch (order) {
                case "Server":
                    //  SQL - define Heathrow to be ordered first, before Heathro2 etc
                    orderBy = " ORDER BY CASE WHEN server = 'Heathrow' THEN NULL ELSE server END ASC";
                    break;
                case "Mimic":
                    orderBy = " ORDER BY mimic ASC";
                    break;
                case "Object":
                    orderBy = " ORDER BY object ASC";
                    break;
                case "Times Referenced":
                    orderBy = " ORDER BY used DESC";
                    break;
                default:
                    break;
            }
        }

        try {
            String sql = "SELECT server AS [Server], mimic AS [Mimic], object AS [Object], hasDBPoint AS [DB Use], hasShape AS [Shape], used AS [Times Referenced] FROM heartObjects " + where + orderBy + ";";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            tableObjects.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * method populates the table with data from the SQL Server
     */
    public void getMimics() {
        String where = "";  //  String for WHERE condition of SQL statement
        String orderBy = "";  //  String for ORDER BY condition of SQL statement

        //  if the server combobox is poulated and not equal to index 0 (ALL), then set WHERE condition
        if (selectServer.getItemCount() > 0 && selectServer.getSelectedIndex() > 0) {
            String server = selectServer.getSelectedItem().toString();  //  convert the selected server to a String
            where = "WHERE server = '" + server + "'";  //  set the WHERE condition for what the server should equal
        }

        //  if the orderBy combobox is poulated, then set ORDER BY condition
        if (orderMimics.getItemCount() > 0) {
            String order = orderMimics.getSelectedItem().toString();  //  convert the selected server to a String
            //  set SQL ORDER BY command based on selected item
            switch (order) {
                case "Server":
                    //  SQL - define Heathrow to be ordered first, before Heathro2 etc
                    orderBy = " ORDER BY CASE WHEN server = 'Heathrow' THEN NULL ELSE server END ASC";
                    break;
                case "Mimic":
                    orderBy = " ORDER BY mimicName ASC";
                    break;
                default:
                    break;
            }
        }

        try {
            String sql = "SELECT server AS [Server], mimicName AS [Mimic] FROM heartMimics " + where + orderBy + ";";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            tableMimics.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * method count the number of mimics and set text
     */
    public void countMimics() {
        try {
            String sql = "SELECT COUNT (*) FROM heartMimics;";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int i = rs.getInt(1);
                countMimics.setText("Total Mimics: " + i);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    /**
     * method count the number of objects and set text
     */
    public void countObjects() {
        try {
            String sql = "SELECT COUNT (*) FROM heartObjects;";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int i = rs.getInt(1);
                countObjects.setText("Total Objects: " + i);
            }
        } catch (Exception e) {
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

        selectServer = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableObjects = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        orderObjects = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableMimics = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        orderMimics = new javax.swing.JComboBox();
        countMimics = new javax.swing.JLabel();
        countObjects = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1366, 768));
        setMinimumSize(new java.awt.Dimension(1366, 768));

        selectServer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectServerActionPerformed(evt);
            }
        });

        jLabel1.setText("Select Server");

        tableObjects.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tableObjects);

        jLabel2.setText("Order By");

        orderObjects.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        orderObjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderObjectsActionPerformed(evt);
            }
        });

        tableMimics.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tableMimics);

        jLabel3.setText("Order By");

        orderMimics.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        orderMimics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderMimicsActionPerformed(evt);
            }
        });

        countMimics.setText("Total Mimics: ");

        countObjects.setText("Total Objects: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(selectServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(orderObjects, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(countObjects))
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderMimics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(countMimics)))
                .addContainerGap(838, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(orderObjects, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countObjects))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(orderMimics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countMimics))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectServerActionPerformed
        getObjects();
        getMimics();
    }//GEN-LAST:event_selectServerActionPerformed

    private void orderObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderObjectsActionPerformed
        getObjects();
    }//GEN-LAST:event_orderObjectsActionPerformed

    private void orderMimicsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderMimicsActionPerformed
        getMimics();
    }//GEN-LAST:event_orderMimicsActionPerformed

    
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
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new View().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel countMimics;
    private javax.swing.JLabel countObjects;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox orderMimics;
    private javax.swing.JComboBox orderObjects;
    private javax.swing.JComboBox selectServer;
    private javax.swing.JTable tableMimics;
    private javax.swing.JTable tableObjects;
    // End of variables declaration//GEN-END:variables

}


