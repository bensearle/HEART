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
 * This class builds as complete tree in the format required for TreeView.java
 * This class prints out the tree to the output. It needs to be copied in to a .xml file
 * @author bsearle
 */
public class CreateTree {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    public static void main(String[] args) {
        // call the method to start the tree building process
        new CreateTree().start();
    }

    public void start() {
        conn = JavaConnect.ConnectDB();  //  get the SQL DB URL
        
        //  print the start of the tree and the declarations
        System.out.println("<tree>\n"
                + " <declarations>\n"
                + " <attributeDecl name=\"name\" type=\"String\"/>\n"
                + " </declarations>");
        //  create the top branch - HEART
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"HEART\"/>");
        //  create a tree for each of servers - the top of these trees are branches to HEART
        //  tree for heathrow server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathrow\"/>");
        getBranch("BROWSER", "heathrow");  //  create the tree for mimics - top of tree will be a branch of the server
        getBranch("os", "heathrow");  //  create the tree for outstations - top of tree will be a branch of the server
        System.out.println("</branch>");
        //  tree for heathro2 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro2\"/>");
        getBranch("BROWSER", "heathro2");  //  create the tree for mimics - top of tree will be a branch of the server
        getBranch("os", "heathro2");  //  create the tree for outstations - top of tree will be a branch of the server
        System.out.println("</branch>");
        //  tree for heathro3 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro3\"/>");
        getBranch("BROWSER", "heathro3");  //  create the tree for mimics - top of tree will be a branch of the server
        getBranch("os", "heathro3");  //  create the tree for outstations - top of tree will be a branch of the server
        System.out.println("</branch>");
        //  tree for heathro4 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro4\"/>");
        getBranch("BROWSER", "heathro4");  //  create the tree for mimics - top of tree will be a branch of the server
        getBranch("os", "heathro4");  //  create the tree for outstations - top of tree will be a branch of the server
        System.out.println("</branch>");
        //  tree for heathro5 server
        System.out.println("<branch>\n"
                + "<attribute name=\"name\" value=\"heathro5\"/>");
        getBranch("BROWSER", "heathro5");  //  create the tree for mimics - top of tree will be a branch of the server
        getBranch("os", "heathro5");  //  create the tree for outstations - top of tree will be a branch of the server
        System.out.println("</branch>");
        //  close the top branch - HEART
        System.out.println("</branch>");
        //  print the end of the tree
        System.out.println("</tree>");
    }

    /*
     * Method to create a tree for a node
     * Method uses recursion to create the tree of each branch from the node
     */
    public void getBranch(String item, String server) {
        //  call the isBranch method to see if the node is a branch
        if (isBranch(item, server)) {
            try {
                List<String> branches = new ArrayList<String>();  //  create a list of branches (or leaves) that this node has
                //  SQL query to return all items where the item parsed in is the primary item
                String sql = "SELECT * FROM connections WHERE primaryItem = '" + item + "' AND server = '" + server + "' ";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                //  go through the list
                while (rs.next()) {
                    String itemName = rs.getString("secondaryItem");  //  get the name secondary item
                    //  add the item to the list of branches if it is not null
                    if (itemName != null) {
                        branches.add(itemName);
                    }
                }
                //  print the start of the primary node
                System.out.println("<branch>\n"
                        + "<attribute name=\"name\" value=\"" + item + "\"/>");
                //  for every branch, create call the method to get the branches (or leaves) of that branch
                for (String b : branches) {
                    getBranch(b, server);
                    // System.out.println(s);
                }
                //  print the start of the secondary node
                System.out.println("</branch>");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
        //  if the node is not a branch then it is a leaf
        else {
            //  print the enitre leaf
            System.out.println("<leaf>\n"
                    + "<attribute name=\"name\" value=\"" + item + "\"/>");
            System.out.println("</leaf>");
        }
    }

    /*
     * This method checks to see if a node is a branch or a leaf
     * Returns true if node is a branch
     */
    public boolean isBranch(String item, String server) {
        boolean ans = false;  //  value to return, initialised as false
        int count = 0;  //  set the count for the number of outgoing branches from the node
        try {
            //  execute SQL string to count the number of times the item is a primary item
            String sql = "SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + item + "' AND server = '" + server + "' ";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            //  go through the list of counts until the count goes above 1
            while (rs.next() && count == 0) {
                count = rs.getInt("count");  //  create a string for each of the names in the database
            }
            //  if the count is above 1 (item is a branch) then set the return value to true
            if (count > 0) {
                ans = true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        //System.out.println("Number of Leaves: " + ans);
        //  return boolean value - true if branch, false if leaf
        return ans;
    }
}
