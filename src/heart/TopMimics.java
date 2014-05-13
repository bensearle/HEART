/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bsearle
 */
public class TopMimics {

    Set<String> mimicObjects = null;
    boolean isTop = false; // boolean to show whether mimic is top level
    String tempServer = "blank";
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    PreparedStatement pst2 = null;
    Statement statement;

    String objectName;
    int objectShapes;
    String[] mimicReadToDo;  //  this is the list of mimics that have been found in the system that are used
    String currentMimic;  //  the name of the mimic that is currently being analysed
    String filepath;  //  the file path where the mimics are stored
    //String server;  //  the server that is being used
    int mimicToDoLength;  //  the total number of mimics that need to be anaylsed
    int mimicDoneLength;  //  the total number of mimics that have been anaylsed
    int numberObject;  //  counter for how many objects the mimic contains
    int numberObjectShape;  //  counter for how many of the objects contain shapes
    int numberTotalShapes;  //  counter for the amount of times a shape keyword occurs
    int numberBegin;  //  counter for the number of times 'begin' appears in a mimic
    int numberEnd;  //  counter for the number of times 'end' appears in a mimic
    int numberVar;  //  counter for the number of times 'var' appears in a mimic
    int numberDBPoint;  //  counter for how many database points are found in a mimic
    int linesOfCode;  //  counter for the lines of code in a mimic
    boolean insideObject;  //  boolean to show whether the current analysing point is inside an object
    boolean insideObjectShape;  //  boolean to show whether the current analysing point in an object has found a shape
    boolean comment;  //  boolean to show whether the current analysing point is part or a comment

    String topMimic;

    //  public String startMimic;
    // String[] shapeArray;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        //  call the method to start analysing the HEART data
        System.out.println("********** START **********");
        //new TopMimics().doOrder();
        new TopMimics().testing();
        System.out.println("********** FINISH **********");
        //new HEART().oneOff();
    }

    public void testing() throws SQLException {
        conn = JavaConnect.ConnectDB();
        statement = conn.createStatement();
        getMimicObjects("BENTEST");
    }

    /*
     * method to call all methods in the order that they need to be executed
     */
    public void doOrder() throws SQLException {
        conn = JavaConnect.ConnectDB();
        statement = conn.createStatement();

        /*
         * search BROWSER mimics to find top level mimics
         */
        isTop = true;
        // readMimic (mimic, server, tag)
        readMimic("BROWSER", "heathrow", "heathrow");
        readMimic("BROWSER", "heathro2", "heathro2");
        readMimic("BROWSER", "heathro3", "heathro3");
        readMimic("BROWSER", "heathro4", "heathro4");
        readMimic("BROWSER", "heathro5", "heathro5");
        isTop = false;
        statement.executeBatch();
        
        getTopMimics("heathrow");
        System.out.println("********** addMimics **********");
        statement.executeBatch();
        System.out.println("********** batch **********");
    }

    public void addMimic(String server, String mimic) throws SQLException {
        String idTag = server + "." + mimic;
        String batchSQL = "IF NOT EXISTS (SELECT idTag FROM heartMimics WHERE idTag = '" + idTag + "') BEGIN INSERT INTO heartMimics (idTag, server, mimicName) VALUES ('" + idTag + "', '" + server + "', '" + mimic + "') END;";
        statement.addBatch(batchSQL);
        if (isTop) {
            String batchSQL2 = "UPDATE heartMimics SET isTop = 'true' WHERE idTag = '" + idTag + "';";
            statement.addBatch(batchSQL2);
        }
        System.out.println(batchSQL);
    }

    public void addConnectionTree(String server, String p, String s) throws SQLException {
        String batchSQL = "INSERT INTO connectionTree (server, pItem, sItem) VALUES ('" + server + "', '" + p + "', '" + s + "');";
        statement.addBatch(batchSQL);
    }

    public void addMimicMimicConnection(String p, String s) throws SQLException {
        String idTag = p + "." + s;
        String batchSQL = "IF NOT EXISTS (SELECT idTag FROM heartMimicConnections WHERE idTag = '" + idTag + "') BEGIN INSERT INTO heartMimicConnections (idTag, pMimic, sMimic) VALUES ('" + idTag + "', '" + p + "', '" + s + "') END;";
        statement.addBatch(batchSQL);
    }

    /*
     * method to get the accessible objects of a given shape
     */
    public void getMimicObjects(String mimicName) throws SQLException {
        Set<String> objects = null;
        String[] listObjects = new String[100];
        int i = 0;
        String sql = "SELECT object FROM heartObjects WHERE mimic IN (SELECT sMimic FROM heartMimicConnections WHERE pMimic = '" + mimicName + "');";
        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();
        while (rs.next()) {
            String o = rs.getString("object");
            System.out.println(o);
            listObjects[i] = o;
            i++;
            System.out.println(o);
            //objects.add(o);       
        }
        System.out.println(Arrays.toString(listObjects));
        mimicObjects = new HashSet<String>(Arrays.asList(listObjects));
        System.out.println("*************" + mimicObjects);
    }

    public void getTopMimics(String server) throws SQLException {
        String sql = "SELECT mimicName FROM heartMimics WHERE isTop = 'true';";
        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();
        while (rs.next()) {
            String m = rs.getString("mimicName");
            String p = server;
            String s = p + "." + m;
            addConnectionTree(server, p, s);  //  add server/mimic connection
            //readMimic(m, server);
            topMimic = m;  //  set the top mimic
            findMimics(server, m);  //  add all mimic underneath the top mimic
            System.out.println(m);
        }
    }

    /*
     * method to analyse used mimcs and add data to the SQL DB
     */
    public void findMimics(String server, String startMimic) throws SQLException {
        mimicReadToDo = new String[10000];  //  maximum number of mimics
        filepath = "C:\\HEART\\" + server + "\\Mimics\\";
        mimicReadToDo[0] = server + "." + startMimic;
        mimicToDoLength = 1;
        mimicDoneLength = 0;
        String tag;
        while (mimicToDoLength != mimicDoneLength) {
            //  reset counting variables
            //    numberObject = 0;
            //    numberObjectShape = 0;
            //    numberTotalShapes = 0;
            //    numberVar = 0;
            //     numberDBPoint = 0;
            //     linesOfCode = 0;
            numberBegin = 0;
            numberEnd = 0;
            //  set booleans to false
            insideObject = false;
            insideObjectShape = false;
            comment = false;

            tag = mimicReadToDo[mimicDoneLength];  //  set the current mimic to the next one in the list
            String[] parts = tag.split("\\.");  //  separate the tag in to a list of strings
            int i = parts.length;
            currentMimic = parts[parts.length - 1];  //  select the last part of the tag - mimic name
            getMimicObjects(currentMimic); // create the list of objects that this mimic can use
            readMimic(currentMimic, server, tag);  //  analyse the current mimic

            // at this point insert to database the mimic name along with number of objects, lines of codes, etc
            //System.out.println(currentMimic + " has " + linesOfCode + " lines of code, " + numberObject + " object, " + numberObjectShape + " shapeobject, " + numberTotalShapes + " total shapes, " + numberBegin + " begin, " + numberEnd + " end, ");
            //  insert the mimic to the SQL DB, including all data that has been collected
            //insertMimic(server, currentMimic, linesOfCode, numberObject, numberObjectShape, numberTotalShapes, numberBegin, numberVar, numberDBPoint);
            mimicDoneLength++;  //  increment the number of mimics that have been analysed, so that the next one can be selected
        }
    }

    public void scanMimic(String filename, String fileline, String server, String tag) throws SQLException {
        //  replace all symbols with a space, other than comment symbol !
        String wSpace = fileline.replaceAll("[^a-zA-z0-9!]", " ");

        Scanner sc = new Scanner(wSpace);
        String s;
        //  while the strind has words
        while (sc.hasNext()) {
            s = sc.next();
            //  if the word contains a comment symbol !
            if (s.contains("!")) {
                int stringCounter = 0;
                // count occurences of !
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == '!') {
                        stringCounter++;
                    }
                }
                if (stringCounter % 2 == 0) {
                    //  even !
                    //  comment begins and ends, no action required
                } else {
                    //  odd !
                    //  comment either begins or ends, switch the comment variable
                    comment = !comment; // switch boolean variable
                }
            }

            // if the code is not part of a comment
            if (!comment) {
                if (s.length() == 1) { //  if string is a single character, do nothing
                } else if (s.equalsIgnoreCase("set")) {  //  if 'set' is found - indicates right click access
                    if (sc.hasNext()) {
                        String s1 = sc.next();
                        if (s1.equalsIgnoreCase("mimic")) {
                            String[] parts = fileline.replaceAll("\"", " ").split("\\s+");  //  separate the tag in to a list of strings
                            //String[] parts = fileline.split("\\s+");  //  separate the tag in to a list of strings
                            //String mimic = parts[parts.length - 1];  //  select the last part of the tag - mimic name
                            if (parts.length >= 5) {
                                Pattern p = Pattern.compile("\"([^\"]*)\"");
                                Matcher m = p.matcher(fileline);
                                while (m.find()) {
                                    String mimic = m.group(1);
                                    addMimic(tempServer.toLowerCase(), mimic.toUpperCase());
                                    addConnectionTree(tempServer.toLowerCase(), tag, tag + "." + mimic.toUpperCase());
                                }
                                tempServer = server; // set the tempServer back to the original server
                            }
                        } else if (s1.equalsIgnoreCase("source")) { // if server 
                            Pattern p = Pattern.compile("\"([^\"]*)\"");
                            Matcher m = p.matcher(fileline);
                            while (m.find()) {
                                tempServer = m.group(1);
                            }
                        }
                    }
                } else if (s.equalsIgnoreCase("load")) {  //  if 'load' is found
                    int count = mimicToDoLength - 1;
                    String foundMimic = sc.next();
                    foundMimic = foundMimic.toUpperCase();
                    while (count >= 0) {
                        //  if the mimic is not in the ToDo list, add it
                        if (count == 0) {
                            //  check the list of found mimics and add new found one if not there
                            if (foundMimic.equals(mimicReadToDo[count].split("\\.")[mimicReadToDo[count].split("\\.").length - 1])) {
                                count--;  //  stop the while loop

                            } else {
                                addMimicMimicConnection(topMimic, foundMimic);
                                String idTag = server + "." + topMimic + "." + foundMimic;
                                //insertConnection(server, currentMimic, foundMimic, "mimic");//*************************************** needs to be uncommented
                                String batchSQL = "IF NOT EXISTS (SELECT idTag FROM mimicTree WHERE idTag = '" + idTag + "') BEGIN INSERT INTO mimicTree (idTag, server, pMimic, sMimic, layers) VALUES ('" + idTag + "', '" + server + "', '" + topMimic + "', '" + foundMimic + "', 1) END;";
                                //  add SQL statements to batch

                                statement.addBatch(batchSQL);
                                System.out.println(batchSQL);

                                addConnectionTree(server, tag, tag + "." + foundMimic);
                                /* tag = mimicReadToDo[mimicDoneLength];  //  set the current mimic to the next one in the list
                                 String[] parts = tag.split("\\.");  //  separate the tag in to a list of strings
                                 int i = parts.length;
                                 currentMimic = parts[parts.length - 1];  //  select the last part of the tag - mimic name
                                 */
                                //mimicReadToDo[mimicToDoLength].split("\\.")[mimicReadToDo[mimicToDoLength].split("\\.").length - 1] = foundMimic;
                                mimicReadToDo[mimicToDoLength] = tag + "." + foundMimic;  //  add the mimic to the list
                                mimicToDoLength++;  //  increment the list length
                                count--;  //  stop the while loop
                            }
                        } else if (foundMimic.equals(mimicReadToDo[count].split("\\.")[mimicReadToDo[count].split("\\.").length - 1])) {
                            count = -1;  //  mimic is already in the list, exit the while loop
                        } else {
                            count--;  //  check the next item in the list
                        }

                    }
                } else if (s.equalsIgnoreCase("object")) {
                    objectName = sc.next();
                    addConnectionTree(server, tag, tag + "." + objectName);
                    objectShapes = 0;
                    numberObject++;
                    insideObjectShape = true;
                } else if (s.equalsIgnoreCase("request")) { //  mimic object 
                    numberVar++;
                } else if (s.equalsIgnoreCase("begin")) {
                    numberBegin++;
                } else if (s.equalsIgnoreCase("end")) {
                    numberEnd++;
                    if (numberBegin - numberEnd == 0) {
                        insideObjectShape = false;
                        if (objectShapes > 0) {
                            String idTag = server + "." + currentMimic + "." + objectName;
                            String batchSQL = "IF NOT EXISTS (SELECT idTag FROM heartObjects WHERE idTag = '" + idTag + "') BEGIN INSERT INTO heartObjects (idTag, server, mimic, object, shapes) VALUES ('" + idTag + "', '" + server + "', '" + currentMimic + "', '" + objectName + "', '" + objectShapes + "') END;";
                            statement.addBatch(batchSQL);
                            objectShapes = 0;
                        }
                    }
                } else if (mimicObjects.contains(s)) {
                    // mimic objects needs to be initialised at some point
                    addConnectionTree(server, topMimic, s);
                } else {
                    if (insideObjectShape && shapeArray.contains(s)) {
                        addConnectionTree(server, tag + "." + objectName, tag + "." + objectName + "." + s);
                        //objectShapes++;
                        //numberTotalShapes++;
                        // numberObjectShape++;

                        // String idTag = server + "." + currentMimic + "." + objectName;
                        // String batchSQL = "IF NOT EXISTS (SELECT idTag FROM objectShapes WHERE idTag = '" + idTag + "') BEGIN INSERT INTO objectShapes (idTag, server, mimic, object) VALUES ('" + idTag + "', '" + server + "', '" + currentMimic + "', '" + objectName + "') END;";
                        // System.out.println(batchSQL);
//  add SQL statements to batch
                    } else if (shapeArray.contains(s)) {
                        objectShapes++;
                        numberTotalShapes++;
                    }
                }
            }
        }
    }

    public void readMimic(String filename, String server, String tag) throws SQLException {
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
                        scanMimic(filename, sCurrentLine, server, tag);
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
