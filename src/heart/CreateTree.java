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
        new CreateTree().start();
    }

    public void start() {
        conn = JavaConnect.ConnectDB();
        System.out.println("<tree>\n"
                + " <declarations>\n"
                + " <attributeDecl name=\"name\" type=\"String\"/>\n"
                + " </declarations>");
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"HEART\"/>");
        //  tree for heathrow server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathrow\"/>");
        getBranch("BROWSER", "heathrow");
        getBranch("os", "heathrow");
        System.out.println("</branch>");
        //  tree for heathro2 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro2\"/>");
        getBranch("BROWSER", "heathro2");
        getBranch("os", "heathro2");
        System.out.println("</branch>");
        //  tree for heathro3 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro3\"/>");
        getBranch("BROWSER", "heathro3");
        getBranch("os", "heathro3");
        System.out.println("</branch>");
        //  tree for heathro4 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro4\"/>");
        getBranch("BROWSER", "heathro4");
        getBranch("os", "heathro4");
        System.out.println("</branch>");
        //  tree for heathro5 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro5\"/>");
        getBranch("BROWSER", "heathro5");
        getBranch("os", "heathro5");
        System.out.println("</branch>");
        System.out.println("</branch>");
        System.out.println("</tree>");
    }

    public void getBranch(String item, String server) {
        if (isBranch(item, server)) {
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

    public boolean isBranch(String item, String server) {
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
