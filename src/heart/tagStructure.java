/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

import com.mysql.jdbc.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author bsearle
 */
public class tagStructure {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    Set<String> mimicObjects = null;

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    String filepath;  //  the file path where the mimics are stored
    
    Statement statement;
    String tMimic; // topMimic name
    String tServer; // topMimic server
    int tMimicCount;  // count of topMimic on server
    String[] topMimics;
    String[] serverMimics; // mimics for a server
    String[] mimicList;
    int mimicListLength;
    int mimicListDone;
    int numberBegin; // counter for 'begin' (start of object)
    int numberEnd; // counter for 'end' (end of object)

    // for object counts
    String objectName;
    String objectShape;
    String objectDBPoint;

    List loadedObjects = new ArrayList();
    Map<String, String> loadedObjects_Mimics;

    //HashSet objectSet; // set of objects that a mimic can use.
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        //  call the method to start analysing the HEART data
        System.out.println("********** START **********");
        new tagStructure().doOrder();
        //new tagStructure().testing();
        System.out.println("********** FINISH **********");
    }

    public void doOrder() throws SQLException {
        conn = JavaConnect.ConnectDB();
        statement = conn.createStatement();
        mimicList = new String[100000];
        mimicListLength = 0; // initialize value
        mimicListDone = 0; // initialize value
        objectName = ""; // initialize value
        objectShape = "n"; // initialize value
        objectDBPoint = "n"; // initialize value
        loadedObjects_Mimics = new HashMap<String, String>();
        /*
         * search BROWSER mimics to find top level mimics
         */

        tMimicCount = 0;
        getTopMimics("BROWSER", "heathrow");
        System.out.println("heathrow BROWSER mimics:" + tMimicCount);
        tMimicCount = 0;
        getTopMimics("BROWSER", "heathro2");
        System.out.println("heathro2 BROWSER mimics:" + tMimicCount);
        tMimicCount = 0;
        getTopMimics("BROWSER", "heathro3");
        System.out.println("heathro3 BROWSER mimics:" + tMimicCount);
        tMimicCount = 0;
        getTopMimics("BROWSER", "heathro4");
        System.out.println("heathro4 BROWSER mimics:" + tMimicCount);
        tMimicCount = 0;
        getTopMimics("BROWSER", "heathro5");
        System.out.println("heathro5 BROWSER mimics:" + tMimicCount);

        String batchSQL = "DELETE FROM tagStructure WHERE tag NOT LIKE 'heathro%';"; // delete non heathrow sources
        statement.addBatch(batchSQL);
        statement.executeBatch();

        selectTopMimics(); // populate topMimics with SQL data
        // scan through all of the top layer mimics
        for (String topMimic : topMimics) {
            //System.out.println(topMimic);
            String[] tagSplit = topMimic.split("\\."); // split tag into server and mimics
            String cServer = tagSplit[0]; // server is the first part of the tag
            String cMimic = tagSplit[tagSplit.length - 1]; // the current mimic is the last part of the tag
            //System.out.println(cServer +" "+ cMimic);
            getMimicStructure(cMimic, cServer, topMimic);
            //System.out.println("executing ...");
            statement.executeBatch();
            //System.out.println("... done");
            numberBegin = 0; // reset value
            numberEnd = 0; // reset value
        }

        // scan through all the found mimics
        while (mimicListLength > mimicListDone) {
            String mimic = mimicList[mimicListDone];
            String[] tagSplit = mimic.split("\\."); // split tag into server and mimics
            String cServer = tagSplit[0]; // server is the first part of the tag
            String cMimic = tagSplit[tagSplit.length - 1]; // the current mimic is the last part of the tag
            getMimicStructure(cMimic, cServer, mimic);
            mimicListDone++;
            //System.out.println("executing ...");
            statement.executeBatch();
            //System.out.println("... done");
            System.out.println("mimic: " + mimic);
            numberBegin = 0; // reset value
            numberEnd = 0; // reset value
        }
        //System.out.println("executing ...");
        statement.executeBatch();
        //System.out.println("... done");

        String[] servers = new String[]{"heathrow", "heathro2", "heathro3", "heathro4", "heathro5"};
        for (String server : servers) {
            selectServerMimics(server); // populate serverMimics with the mimics from that server
            System.out.println(server);
            for (String mimic : serverMimics) {
                System.out.println(server + " - " + mimic);
                getMimicObjectUse(mimic, server);
                //loadedObjects.clear();
                loadedObjects_Mimics.clear();
            }
        }

        statement.executeBatch();

        System.out.println("begin " + numberBegin);
        System.out.println("end " + numberEnd);

        updateObjectDetails();

    }

    /**
     * method to update an object with 'y' for shape/dbpoint if any of the object it uses has 'y' for shape/dbpoint
     * pObject uses sObject
     */
    public void updateObjectDetails() throws SQLException {
        String batchShape = "UPDATE objectDetails SET shape='y' WHERE tag IN " // make pObject shape 'y' where in 
                + "(SELECT pTag FROM objectConnections WHERE sTag IN " // get the list of pObjects that that use a sObject where in
                + "(SELECT tag FROM objectDetails WHERE shape = 'y'))"; // get the list of sObject that have 'y' for shape
        String dbPoint = "UPDATE objectDetails SET dbPoint='y' WHERE tag IN "
                + "(SELECT pTag FROM objectConnections WHERE sTag IN "
                + "(SELECT tag FROM objectDetails WHERE dbPoint = 'y'))";
        for (int i = 0; i < 15; i++) {
            statement.addBatch(batchShape);
            statement.addBatch(dbPoint);
        }
        statement.executeBatch();
    }

    public Set<String> selectObjects(String mimic) throws SQLException {

        //Set<String> h = new HashSet<String>();
        //h.add("a");
        //h.add("b");
        Set<String> set = new HashSet<String>();
        String sql = "SELECT object FROM objectDetails WHERE mimic IN "
                + "(SELECT sItem FROM connections WHERE pItem = '" + mimic + "')";
        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();
        // while statement to check the count until it goes above 1 (until an OS is found)
        while (rs.next()) {
            set.add(rs.getString("object")); // add each object to the set
        }
        return set;
    }

    public void addTagStructure(String tag) throws SQLException {
        int layers = tag.replaceAll("[^.]", "").length();
        //System.out.println(layers + "  " + tag);
        String batchSQL = "IF NOT EXISTS (SELECT tag FROM tagStructure WHERE tag = '" + tag.toLowerCase() + "') "
                + "BEGIN INSERT INTO tagStructure (tag, layers) VALUES ('" + tag.toLowerCase() + "', '" + layers + "') END;";
        statement.addBatch(batchSQL);
    }

    public void addObjectUses(String longTag, String object) throws SQLException {
        String[] tagSplit = longTag.split("\\."); // long can tag includes many mimics
        String server = tagSplit[0];
        String mimic = tagSplit[tagSplit.length - 1];
        String tag = server + "." + mimic + "." + object;
        String batchSQL = "IF NOT EXISTS (SELECT tag FROM objectUses WHERE tag = '" + tag.toLowerCase() + "') "
                + "BEGIN INSERT INTO objectUses (tag, server, mimic, object) VALUES ('" + tag.toLowerCase() + "', '" + server.toLowerCase() + "', '" + mimic.toLowerCase() + "', '" + object.toLowerCase() + "') END;";
        statement.addBatch(batchSQL);
    }

    public void addObjectDetails(String longTag) throws SQLException {
        String[] tagSplit = longTag.split("\\."); // long can tag includes many mimics
        String server = tagSplit[0];
        String mimic = tagSplit[tagSplit.length - 1];
        String tag = server + "." + mimic + "." + objectName;
        String batchSQL = "IF NOT EXISTS (SELECT tag FROM objectDetails WHERE tag = '" + tag.toLowerCase() + "') "
                + "BEGIN INSERT INTO objectDetails (tag, server, mimic, object, shape, dbPoint) VALUES ('" + tag.toLowerCase() + "', '" + server.toLowerCase() + "', '" + mimic.toLowerCase() + "', '" + objectName.toLowerCase() + "', '" + objectShape.toLowerCase() + "', '" + objectDBPoint.toLowerCase() + "') END;";
        statement.addBatch(batchSQL);
        objectName = ""; // reset value
        objectShape = "n"; // reset value
        objectDBPoint = "n"; // reset value
    }

    public void addTreeConnections(String pTag, String sItem) throws SQLException {
        String[] tagSplit = pTag.split("\\."); // long can tag includes many mimics
        String server = tagSplit[0];
        String pItem = tagSplit[tagSplit.length - 1];
        String tag = server + "." + pItem + "." + sItem;
        String batchSQL = "IF NOT EXISTS (SELECT tag FROM treeConnections WHERE tag = '" + tag.toLowerCase() + "') "
                + "BEGIN INSERT INTO treeConnections (tag, server, pItem, sItem) VALUES ('" + tag.toLowerCase() + "', '" + server.toLowerCase() + "', '" + pItem.toLowerCase() + "', '" + sItem.toLowerCase() + "') END;";
        statement.addBatch(batchSQL);
    }

    public void addObjectConnections(String server, String pMimic, String pObject, String sMimic, String sObject) throws SQLException {
        String tag = server + "." + pMimic + "." + pObject + "." + sMimic + "." + sObject;
        String pTag = server + "." + pMimic + "." + pObject;
        String sTag = server + "." + sMimic + "." + sObject;
        String batchSQL = "IF NOT EXISTS (SELECT tag FROM objectConnections WHERE tag = '" + tag.toLowerCase() + "') "
                + "BEGIN INSERT INTO objectConnections (tag, server, pMimic, pObject, sMimic, sObject, pTag, sTag) VALUES "
                + "('" + tag.toLowerCase() + "', '" + server.toLowerCase() + "', '" + pMimic.toLowerCase() + "', '" + pObject.toLowerCase() + "', '" + sMimic.toLowerCase() + "', '" + sObject.toLowerCase() + "', '" + pTag.toLowerCase() + "', '" + sTag.toLowerCase() + "') END;";
        statement.addBatch(batchSQL);
    }

    public int countTagStructure(String tag) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT (*) AS count FROM tagStructure WHERE tag = '" + tag + "'";
        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();
        // while statement to check the count until it goes above 1 (until an OS is found)
        while (rs.next()) {
            count = rs.getInt("count"); // create a string for each of the names in the database
        }
        return count;
    }

    public void getTopMimics(String filename, String server) throws SQLException {
        BufferedReader br = null;
        filepath = "C:\\HEART\\" + server + "\\Mimics\\";
        File file = new File(filepath, filename);
        if (file.exists()) {
            //System.out.println("Searching " + filename);
            try {
                String sCurrentLine;
                br = new BufferedReader(new FileReader(filepath + filename));
                while ((sCurrentLine = br.readLine()) != null) {
                    // Check that the string is not empty
                    if (sCurrentLine.trim().length() != 0) {
                        scanTopMimics(filename, sCurrentLine, server);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                //             e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Cannot Find: " + filename);
        }
    }

    public void scanTopMimics(String filename, String fileline, String server) throws SQLException {
        //  replace all symbols with a space, other than comment symbol !
        String wSpace = fileline.replaceAll("[^a-zA-z0-9]", " ");

        Scanner sc = new Scanner(wSpace);
        String s;
        //  while the strind has words
        while (sc.hasNext()) {
            s = sc.next();
            //  if the word contains a comment symbol !

            if (s.length() == 1) { //  if string is a single character, do nothing
            } else if (s.equalsIgnoreCase("set")) {  //  if 'set' is found - indicates right click access
                if (sc.hasNext()) {
                    String s1 = sc.next();
                    String[] lineWords = fileline.replaceAll("\"", " ").split("\\s+");  //  separate the tag in to a list of strings
                    if (s1.equalsIgnoreCase("mimic")) { // if mimic
                        tMimic = lineWords[lineWords.length - 1];
                        //System.out.println(tMimic + "  " + tServer);
                        addTagStructure(tServer + "." + tMimic); // add found mimc to tagStructure
                        tMimicCount++; // counting the mimics in 
                    } else if (s1.equalsIgnoreCase("source")) { // if server 
                        tServer = lineWords[lineWords.length - 1];
                    }
                }
            }
        }
    }

    /**
     * Get the first level of mimics from SQL tagStructure
     */
    public void selectTopMimics() {
        try {
            String sql = "SELECT tag FROM tagStructure WHERE layers = '1';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString("tag"));
            }
            topMimics = (String[]) list.toArray(new String[list.size()]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * select all mimics on the sever, using the object table
     */
    public void selectServerMimics(String server) {
        try {
            String sql = "SELECT DISTINCT mimic FROM objectDetails WHERE server = '" + server + "';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString("mimic"));
            }
            serverMimics = (String[]) list.toArray(new String[list.size()]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void selectLoadedObjects(String mimic, String server) {
        try {
            String sql = "SELECT object FROM objectDetails WHERE mimic = '" + mimic + "' AND server = '" + server + "';";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                //loadedObjects.add(rs.getString("mimic"));
                loadedObjects_Mimics.put(rs.getString("object"), mimic); // add object and mimic
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void getMimicStructure(String filename, String server, String tag) throws SQLException {
        BufferedReader br = null;
        filepath = "C:\\HEART\\" + server + "\\Mimics\\";
        File file = new File(filepath, filename);
        if (file.exists()) {
            //System.out.println("Searching " + filename);
            try {
                String sCurrentLine;
                br = new BufferedReader(new FileReader(filepath + filename));
                while ((sCurrentLine = br.readLine()) != null) {
                    // Check that the string is not empty
                    if (sCurrentLine.trim().length() != 0) {
                        scanMimicStructure(filename, sCurrentLine, server, tag);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                //             e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Cannot Find: " + filename);
        }
    }

    public void scanMimicStructure(String filename, String fileline, String server, String tag) throws SQLException {
        // replace all symbols with a space, other than comment symbol !
        String wSpace = fileline.replaceAll("[^a-zA-z0-9=]", " ");

        Scanner sc = new Scanner(wSpace);
        String s;
        // while the strind has words
        while (sc.hasNext()) {
            s = sc.next();
            if (s.length() == 1) { // if string is a single character, do nothing
            } else if (s.equalsIgnoreCase("load")) { // if a mimic is used, add it to the list of mimics to scan
                if (sc.hasNext()) {
                    String foundMimic = sc.next();
                    addTagStructure(tag + "." + foundMimic);
                    addTreeConnections(tag, foundMimic);
                    mimicList[mimicListLength] = tag + "." + foundMimic;
                    mimicListLength++;
                }
            } else if (s.equalsIgnoreCase("object")) {
                if (sc.hasNext()) {
                    String objectWParam = sc.next();
                    String[] split = objectWParam.split("\\("); // split tag into object and parameters
                    String object = split[0]; // object is the first part
                    //addTagStructure(tag + "." + object);
                    addObjectUses(tag, object);
                    objectName = object; // objectName used for SQL objectDetails
                }
            } else if (s.equalsIgnoreCase("mimic")) {
                if (fileline.contains("MIMIC = main_object w=400"))
                {
                 String sss ="";
                }
                if (numberBegin == numberEnd) {
                    // end of object                
                    if (fileline.contains("mimic =") || fileline.contains("mimic=")||
                            fileline.contains("Mimic =") || fileline.contains("Mimic=") ||
                            fileline.contains("MIMIC =") || fileline.contains("MIMIC=")) {
                        String[] parts = fileline.split(" ");
                        if (parts[0].equalsIgnoreCase("mimic") && parts.length>2){                          
                            if (parts[1].equalsIgnoreCase("=")){
                                objectName = parts[2];
                                addObjectDetails(tag);   
                                addObjectConnections(server, filename, "mimic", filename, objectName);
                            } else if (parts[2].equalsIgnoreCase("=")){
                               objectName = parts[3];
                               addObjectDetails(tag);
                               addObjectConnections(server, filename, "mimic", filename, objectName);
                            }
                            //System.out.println("***N*** "+main_obj+" ::: "+wSpace+" ::: "+fileline+" + "+" + ");
                        } 
                    }  
                } else {
                // mimic is a popup?
                }
            } else if (s.equalsIgnoreCase("begin")) {
                numberBegin++;
            } else if (s.equalsIgnoreCase("end")) {
                numberEnd++;
                if (numberBegin == numberEnd) {
                    // end of object
                    addObjectDetails(tag);
                }
            } else if (shapeArray.contains(s)) {
                // shape keyword is found
                objectShape = "y";
            } else if (s.toLowerCase().contains("db_address".toLowerCase())) {
                // contains is used because string may be db_address(parameters)
                // database is accessed
                objectDBPoint = "y";
            }
        }
    }

    public void getMimicObjectUse(String filename, String server) throws SQLException {
        BufferedReader br = null;
        filepath = "C:\\HEART\\" + server + "\\Mimics\\";
        File file = new File(filepath, filename);
        if (file.exists()) {
            //System.out.println("Searching " + filename);
            try {
                String sCurrentLine;
                br = new BufferedReader(new FileReader(filepath + filename));
                while ((sCurrentLine = br.readLine()) != null) {
                    // Check that the string is not empty
                    if (sCurrentLine.trim().length() != 0) {
                        scanMimicObjectUse(filename, sCurrentLine, server);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                //             e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Cannot Find: " + filename);
        }
    }

    public void scanMimicObjectUse(String filename, String fileline, String server) throws SQLException {
        // replace all symbols with a space, other than comment symbol !
        String wSpace = fileline.replaceAll("[^a-zA-z0-9]", " ");

        Scanner sc = new Scanner(wSpace);
        String s;
        // while the strind has words
        while (sc.hasNext()) {
            s = sc.next();
            if (s.length() == 1) { // if string is a single character, do nothing
            } else if (s.equalsIgnoreCase("load")) { // if a mimic is used, add it to the list of mimics to scan
                if (sc.hasNext()) {
                    String loadedMimic = sc.next();
                    selectLoadedObjects(loadedMimic, server); // add loaded mimic objects to loadedObjects
                }
            } else if (s.equalsIgnoreCase("object")) {
                if (sc.hasNext()) {
                    String objectWParam = sc.next();
                    String[] split = objectWParam.split("\\("); // split tag into object and parameters
                    String object = split[0]; // object is the first part
                    //addTagStructure(tag + "." + object);
                    objectName = object; // objectName used for SQL objectDetails
                }
            } else if (s.equalsIgnoreCase("mimic")) {
                if (fileline.contains("mimic =") || fileline.contains("mimic=")||
                        fileline.contains("Mimic =") || fileline.contains("Mimic=") ||
                        fileline.contains("MIMIC =") || fileline.contains("MIMIC=")) {
                    String[] parts = fileline.split(" ");
                    if (parts[0].equalsIgnoreCase("mimic") && parts.length>2){   
                        if (parts[1].equalsIgnoreCase("=")){
                            objectName = parts[2];
                            addObjectConnections(server, filename, "mimic", filename, objectName);                     
                        } else if (parts[2].equalsIgnoreCase("=")){
                           objectName = parts[3];
                           addObjectConnections(server, filename, "mimic", filename, objectName);
                        }
                        //System.out.println("***N*** "+main_obj+" ::: "+wSpace+" ::: "+fileline+" + "+" + ");
                    } 
                }  
            } else if (loadedObjects_Mimics.containsKey(s)) {
                // s is the key - object
                // mimic is the value
                String sMimic = loadedObjects_Mimics.get(s); // get the value to the key
                addObjectConnections(server, filename, objectName, sMimic, s);
            }
        }
    }

    /*
     * a list of all shape keywords used in MDL
     */
    private static final Set<String> shapeArray = new HashSet<String>(Arrays.asList(
            new String[]{
                "adjb",
                "anyb",
                "arc",
                "bell",
                "box",
                "circle",
                "menb",
                "disk",
                "fill",
                "key",
                "line",
                "pie",
                "polygon",
                "rect",
                "rotadjb",
                "rotanyb",
                "rotarc",
                "rotbox",
                "rotmenb",
                "rotpie",
                "rotrect",
                "rotselb",
                "selb",
                "text"
            }));

}


/**** sql 
  SELECT * FROM [HEART].[dbo].[objectConnections] as table1 where table1. IN (SELECT tag FROM [HEART].[dbo].[objectDetails] where shape = 'y' AND dbPoint = 'y') 

  inner join

(SELECT pTag, count(*) as frequency
  FROM [HEART].[dbo].[objectConnections] as table2 where pTag IN (
  SELECT tag FROM [HEART].[dbo].[objectDetails] where shape = 'y' AND dbPoint = 'y') group by pTag order by frequency)

  ON table1.pTag=table2.pTag
ORDER BY table2.frequency;


  SELECT [mimic]
FROM [HEART].[dbo].[objectDetails] as t1
INNER JOIN [HEART].[dbo].[objectConnections] as t2
ON t1.mimic=t2.pMimic;
 */
