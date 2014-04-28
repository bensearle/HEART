/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

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
        new MimicConnections().doOrder();
        //new HEART().oneOff();
    }

    /*
     * method used in test phases to call methods that need testing
     */
    public void oneOff() {
        conn = JavaConnect.ConnectDB();

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
        System.out.print(statement);
        //
    }

    /*
     * method to analyse all mimics, regardless of server
     * only used for testing
     */
    public void addAllMimcis(String server) throws SQLException {
        mimicReadToDo = new String[10000];  //  maximum number of mimics
        mimicReadToDo[0] = "MAIN_CB";  //  starting mimics  
        filepath = "C:\\HEART\\" + server + "\\Mimics\\";
        String[] fileNames = getFileNames("C:\\HEART\\" + server + "\\Mimics\\");
        //2788

        mimicToDoLength = 1;
        mimicDoneLength = 0;

        for (String m : fileNames) {
            //readMimic(m, server);
            topMimic = m;
            addMimics(server, m);
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
        mimicReadToDo[0] = startMimic;
        mimicToDoLength = 1;
        mimicDoneLength = 0;

        while (mimicToDoLength != mimicDoneLength) {
            //  reset counting variables
            numberObject = 0;
            numberObjectShape = 0;
            numberTotalShapes = 0;
            numberVar = 0;
            numberDBPoint = 0;
            linesOfCode = 0;
            numberBegin = 0;
            numberEnd = 0;
            //  set booleans to false
            insideObject = false;
            insideObjectShape = false;
            comment = false;

            currentMimic = mimicReadToDo[mimicDoneLength];  //  set the current mimic to the next one in the list
            readMimic(currentMimic, server);  //  analyse the current mimic

            // at this point insert to database the mimic name along with number of objects, lines of codes, etc
            //System.out.println(currentMimic + " has " + linesOfCode + " lines of code, " + numberObject + " object, " + numberObjectShape + " shapeobject, " + numberTotalShapes + " total shapes, " + numberBegin + " begin, " + numberEnd + " end, ");
            //  insert the mimic to the SQL DB, including all data that has been collected
            insertMimic(server, currentMimic, linesOfCode, numberObject, numberObjectShape, numberTotalShapes, numberBegin, numberVar, numberDBPoint);
            mimicDoneLength++;  //  increment the number of mimics that have been analysed, so that the next one can be selected
        }
    }

    /*
     * method to add connections to SQL DB, only used for mimics, everything else uses batch SQL statements
     */
    public void insertConnection(String server, String primary, String secondary, String type) {
        String idTag = server + "::" + primary + ":" + secondary;
        try {
            //  create the SQL query using the variables that are parsed in
            String sql = "INSERT INTO connections (idTag, server, primaryItem, secondaryItem, connectionType) VALUES ('" + idTag + "', '" + server + "', '" + primary + "', '" + secondary + "', '" + type + "')";
            pst = conn.prepareStatement(sql);
            pst.execute();  //  execute SQL query
            //("Database entry: " + type + " reference between " + primary + " and " + secondary);
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to add detailed mimic data to SQLDB
     * does not include how many connections mimic has
     * not all connections have been added at the point the mimic is added
     * see updateConnectionsMimic() for adding of connections
     */
    public void insertMimic(String server, String mimicName, int loc, int object, int objectShape, int totalShape, int containers, int variables, int dbPoints) {
        String idTag = server + "::" + mimicName;
        try {
            //  create the SQL query using the variables that are parsed in
            String sql = "INSERT INTO allMimics (idTag, server, mimicName, loc, object, objectShape, totalShape, containers, variables, dbPoints) VALUES ('" + idTag + "', '" + server + "', '" + mimicName + "', '" + loc + "', '" + object + "', '" + objectShape + "', '" + totalShape + "', '" + containers + "', '" + variables + "', '" + dbPoints + "')";
            pst = conn.prepareStatement(sql);
            pst.execute();  //  execute SQL query
            //("Database entry: " + server + " server, " + mimicName + " Mimic, " + loc + " LoC, " + object + " objects (" + objectShape + " shapes), " + totalShape + " total shapes, " + containers + " containers, " + variables + " variables, " + dbPoints + " DB points");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to check whether a string is a DBPoint and return boolean value
     * DBPoint must be 1 letter, at least 1 digit
     */
    public boolean isDPBPoint(String s) {
        boolean ans = false;
        //  regex for a database point is 1 letter followed by numbers
        Pattern p = Pattern.compile("^\\p{Alpha}{1}\\d+$");  //  regular expression for letter, followed by many digits
        Matcher m = p.matcher(s);
        System.out.print("Input: " + s + "... ");
        //  if the input string matches the format of the regular expression, set return value to be true
        if (m.find()) {
            //("Found match: " + m.group(0));
            ans = true;
        }
        return ans;  //  return value
    }

    /*
     * method to check whether input is a number and return boolean value
     */
    public boolean isNumeric(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            // s is not numeric
            return false;
        }
    }

    public void scanMimic(String filename, String fileline, String server) throws SQLException {
        //  replace all symbols with a space, other than comment symbol !
        fileline.replaceAll("[^a-zA-z0-9!]", " ");

        Scanner sc = new Scanner(fileline);
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
                    while (count >= 0) {
                        //  if the mimic is not in the ToDo list, add it
                        if (count == 0) {
                            //  check the list of found mimics and add new found one if not there
                            if (foundMimic.equals(mimicReadToDo[count])) {
                                count--;  //  stop the while loop
                            } else {
                                String idTag = server + "." + topMimic + "." + foundMimic;
                                //insertConnection(server, currentMimic, foundMimic, "mimic");//*************************************** needs to be uncommented
                                String batchSQL = "IF NOT EXISTS (SELECT idTag FROM mimicTree WHERE idTag = '" + idTag + "') BEGIN INSERT INTO mimicTree (idTag, server, pMimic, sMimic, layers) VALUES ('" + idTag + "', '" + server + "', '" + topMimic + "', '" + foundMimic + "', 1) END;";
                                //  add SQL statements to batch
                                statement.addBatch(batchSQL);
                                System.out.println(batchSQL);
                                mimicReadToDo[mimicToDoLength] = foundMimic;  //  add the mimic to the list
                                mimicToDoLength++;  //  increment the list length
                                count--;  //  stop the while loop
                            }
                        } else if (foundMimic.equals(mimicReadToDo[count])) {
                            count = -1;  //  mimic is already in the list, exit the while loop
                        } else {
                            count--;  //  check the next item in the list
                        }

                    }
                }
            }
        }
    }

    public void readMimic(String filename, String server) throws SQLException {
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
                        scanMimic(filename, sCurrentLine, server);
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
     * method to delete all data in the SQLDB
     */
    public void emptyDatabase() {
        try {
            Statement statement = conn.createStatement();
            statement.addBatch("DELETE FROM allMimics;");  //  add query to batch sql statement  
            statement.addBatch("DELETE FROM allAlarms;");  //  add query to batch sql statement  
            statement.addBatch("DELETE FROM allDBPoints;");  //  add query to batch sql statement  
            statement.addBatch("DELETE FROM allMimics;");  //  add query to batch sql statement
            statement.addBatch("DELETE FROM allOutstations;");  //  add query to batch sql statement  
            statement.addBatch("DELETE FROM connections;");  //  add query to batch sql statement  
            System.out.println("Everything deleted");
            statement.executeBatch();  //  execute the batch sql statement
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }
}
