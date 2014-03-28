/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 *
 * @author bsearle
 */
public class CreateTree {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    public static void main(String[] args) {
        // TODO code application logic here
        new CreateTree().start("BROWSER", "heathro5");
    }

    public void start(String item, String server) {
        conn = JavaConnect.ConnectDB();
        System.out.println("<tree>\n"
                + " <declarations>\n"
                + " <attributeDecl name=\"name\" type=\"String\"/>\n"
                + " </declarations>\n");
        getBranch(item, server);
        System.out.println("</tree>");
    }

    public void removeData() {
        try {
            //String sql = "SELECT * INTO tree FROM connections";
            String sql = "DELETE FROM tree";
            pst = conn.prepareStatement(sql);
            pst.execute();
            System.out.println("Data Removed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void addData() {
        try {
            //String sql = "SELECT * INTO tree FROM connections";
            String sql = "INSERT INTO tree SELECT * FROM connections";
            pst = conn.prepareStatement(sql);
            pst.execute();
            System.out.println("Data Added");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void removeItem(String item, String server) {
        try {
            //String sql = "SELECT * INTO tree FROM connections";
            String sql = "DELETE FROM tree WHERE primaryItem = '" + item + "' server = '" + server + "' FROM connections";
            pst = conn.prepareStatement(sql);
            pst.execute();
            System.out.println(item + " Removed from DB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void getBranch(String item, String server) {
        if (countLeaves(item, server)) {
            try {
                List<String> branches = new ArrayList<String>();
                String sql = "SELECT * FROM connections WHERE primaryItem = '" + item + "' AND server = '" + server + "' ";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                while (rs.next()) {
                    String itemName = rs.getString("secondaryItem");  //  get the name of mimic
                    if (itemName != null) {
                        branches.add(itemName);
                    }
                }
                System.out.println("<branch>\n"
                        + "<attribute name=\"name\" value=\"" + item + "\"/>");
                for (String b : branches) {
                    getBranch(b, server);
                    // System.out.println(s);
                }
                System.out.println("</branch>");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        } else {
            System.out.println("<leaf>\n"
                    + "<attribute name=\"name\" value=\"" + item + "\"/>");
            System.out.println("</leaf>");
        }
    }

    public boolean countLeaves(String item, String server) {
        boolean ans = false;
        int count = 0;
        try {
            List<String> branches = new ArrayList<String>();
            String sql = "SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + item + "' AND server = '" + server + "' ";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next() && count == 0) {
                count = rs.getInt("count");  //  create a string for each of the names in the database
            }
            if (count > 0) {
                ans = true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        //System.out.println("Number of Leaves: " + ans);
        return ans;
    }
}
