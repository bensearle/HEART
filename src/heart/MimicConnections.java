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

/**
 *
 * @author bsearle
 */
public class MimicConnections {

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
        new MimicConnections().doOrder();
        System.out.println("********** FINISH **********");
        //new HEART().oneOff();
    }


    /*
     * method to return a list of all the files in a specific directory
     */
    public static String[] getFileNames(String directoryPath) {
        File dir = new File(directoryPath);
        Collection<String> files = new ArrayList<String>();
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();

            for (File file : listFiles) {
                if (file.isFile()) {
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }

    /*
     * method to call all methods in the order that they need to be executed
     */
    public void doOrder() throws SQLException {
        conn = JavaConnect.ConnectDB();
        statement = conn.createStatement();

        //  analyse mimics
        addAllMimcis("heathrow");
        System.out.println("********** addMimics **********");
        statement.executeBatch();
        System.out.println("********** batch **********");
    }

    /*
     * 
     */
    public void addAllMimcis(String server) throws SQLException {
        String[] fileNames = getFileNames("C:\\HEART\\" + server + "\\Mimics\\");
        //2788

        //  for every mimic in the list
        for (String m : fileNames) {
            String p = server;
            String s = p + "." + m;
            addConnectionTree(server, p, s);  //  add server/mimic connection
            //readMimic(m, server);
            topMimic = m;  //  set the top mimic
            addMimics(server, m);  //  add all mimic underneath the top mimic
            System.out.println(m);
        }
        //statement.executeBatch();

    }

    /*
     * method to analyse used mimcs and add data to the SQL DB
     */
    public void addMimics(String server, String startMimic) throws SQLException {
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

            /* System.out.println("");
             delimiter = "\\.";
             temp = tag.split("\\.");
             for (int i = 0; i < temp.length; i++) {
             System.out.println(temp[i]);
             }*/
            String[] parts = tag.split("\\.");  //  separate the tag in to a list of strings
            int i = parts.length;
            currentMimic = parts[parts.length - 1];  //  select the last part of the tag - mimic name
            readMimic(currentMimic, server, tag);  //  analyse the current mimic

            // at this point insert to database the mimic name along with number of objects, lines of codes, etc
            //System.out.println(currentMimic + " has " + linesOfCode + " lines of code, " + numberObject + " object, " + numberObjectShape + " shapeobject, " + numberTotalShapes + " total shapes, " + numberBegin + " begin, " + numberEnd + " end, ");
            //  insert the mimic to the SQL DB, including all data that has been collected
            //insertMimic(server, currentMimic, linesOfCode, numberObject, numberObjectShape, numberTotalShapes, numberBegin, numberVar, numberDBPoint);
            mimicDoneLength++;  //  increment the number of mimics that have been analysed, so that the next one can be selected
        }
    }

    public void addConnectionTree(String server, String p, String s) throws SQLException {
        String batchSQL = "INSERT INTO connectionTree (server, pItem, sItem) VALUES ('" + server + "', '" + p + "', '" + s + "');";
        statement.addBatch(batchSQL);
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
                                mimicReadToDo[mimicToDoLength] = tag+"."+foundMimic;  //  add the mimic to the list
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
                    addConnectionTree(server, tag , tag + "." + objectName);
                    objectShapes = 0;
                    numberObject++;
                    insideObjectShape = true;
                } else if (s.equalsIgnoreCase("var")) {
                    numberVar++;
                } else if (s.equalsIgnoreCase("begin")) {
                    numberBegin++;
                } else if (s.equalsIgnoreCase("end")) {
                    numberEnd++;
                    if (numberBegin - numberEnd == 0) {
                        insideObjectShape = false;
                        if (objectShapes > 0) {
                            String idTag = server + "." + currentMimic + "." + objectName;
                            String batchSQL = "IF NOT EXISTS (SELECT idTag FROM objectShapes WHERE idTag = '" + idTag + "') BEGIN INSERT INTO objectShapes (idTag, server, mimic, object, shapes) VALUES ('" + idTag + "', '" + server + "', '" + currentMimic + "', '" + objectName + "', '" + objectShapes + "') END;";
                            statement.addBatch(batchSQL);
                            objectShapes = 0;
                        }
                    }
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

    /*
     * a list of all shape keywords used in MDL
     */
    private static final Set<String> shapeArray = new HashSet<String>(Arrays.asList(
            new String[]{
                "rect",
                "rotrect",
                "polygon",
                "fill",
                "pie",
                "arc"
            }));

    public void readMimic(String filename, String server, String tag) throws SQLException {
        BufferedReader br = null;
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
}
