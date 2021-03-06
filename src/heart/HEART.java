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
public class HEART {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    PreparedStatement pst2 = null;
    
    String[] mimicReadToDo;  //  this is the list of mimics that have been found in the system that are used
    String currentMimic;  //  the name of the mimic that is currently being analysed
    String filepath;  //  the file path where the mimics are stored
    String server;  //  the server that is being used
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

    //  public String startMimic;
    // String[] shapeArray;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //  call the method to start analysing the HEART data
        new HEART().doOrder();
        //new HEART().oneOff();
    }
    
    /*
    * method used in test phases to call methods that need testing
    */
    public void oneOff(){
        conn = JavaConnect.ConnectDB();
        addOutstation("heathro3");
        addOutstation("heathro4");
    }
    
    /*
    * method to return a list of all the files in a specific directory
    */
    public static String[] fileNames(String directoryPath) {
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
    public void doOrder() {
        conn = JavaConnect.ConnectDB();
        //  analyse mimics
        addMimics("heathrow", "BROWSER");
        addMimics("heathro2", "BROWSER");
        addMimics("heathro3", "BROWSER");
        addMimics("heathro4", "BROWSER");
        addMimics("heathro5", "BROWSER");
        System.out.println("********** addMimics **********");
        //  analyse outstations
        addOutstation("heathrow");
        addOutstation("heathro2");
        addOutstation("heathro3");
        addOutstation("heathro4");
        addOutstation("heathro5");
        System.out.println("********** addOutstation **********");
        //  analyse db points used by outstations
        osMappingCSV("heathrow");
        osMappingCSV("heathro2");
        osMappingCSV("heathro3");
        osMappingCSV("heathro4");
        osMappingCSV("heathro5");
        System.out.println("********** osMappingCSV **********");
        //  update db points with name
        dbUpdateCSV("heathrow");
        dbUpdateCSV("heathro2");
        dbUpdateCSV("heathro3");
        dbUpdateCSV("heathro4");
        dbUpdateCSV("heathro5");
        System.out.println("********** dbUpdateCSV **********");
        // add alarms that are mapped to db points
        alarmCSV("heathrow");
        alarmCSV("heathro1");
        alarmCSV("heathro2");
        alarmCSV("heathro3");
        alarmCSV("heathro4");
        alarmCSV("heathro5");
        System.out.println("********** alarmCSV **********");
        //  update all items with the number of connections they have
        updateConnectionsMimic();
        System.out.println("********** updateConnectionsMimic **********");
        updateConnectionsOutstation();
        System.out.println("********** updateConnectionsOutstations **********");
        updateConnectionsDBPoint();
        System.out.println("********** updateConnectionsDBPoint **********");
        updateConnectionsAlarm();
        System.out.println("********** updateConnectionsAlarm **********");
    }
    
    /*
    * method to analyse all mimics, regardless of server
    * only used for testing
    */
    public void addAllMimcs() {
        conn = JavaConnect.ConnectDB();  //  get the connection url
         mimicReadToDo = new String[10000];  //  maximum number of mimics
         server = "ALL";
         mimicReadToDo[0] = "MAIN_CB";  //  starting mimics  
         filepath = "C:\\HEART\\" + server + "\\Mimics\\";
         String[] fileName = fileNames("C:\\HEART\\ALL\\Mimics\\");
         //2788
 
         mimicToDoLength = 1;
         mimicDoneLength = 0;

         while (mimicDoneLength < 2788) {
             // reset counting variables
             numberObject = 0;
             numberObjectShape = 0;
             numberTotalShapes = 0;
             numberVar = 0;
             numberDBPoint = 0;
             linesOfCode = 0;
             numberBegin = 0;
             numberEnd = 0;
             insideObject = false;
             insideObjectShape = false;
             comment = false;
             
             currentMimic = fileName[mimicDoneLength];  //  set the current mimic to the next one in the list
             readMimic(currentMimic, server);  //  analyse the current mimic
 
             insertMimic(server, currentMimic, linesOfCode, numberObject, numberObjectShape, numberTotalShapes, numberBegin, numberVar, numberDBPoint);
             System.out.println("DONE: "+currentMimic);
             mimicDoneLength++;
         }
    }

    /*
     * method to analyse used mimcs and add data to the SQL DB
     */
    public void addMimics(String server, String startMimic) {
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
    
    /*
    * method to add outstations from .csv file to SQL server
    */
    public void addOutstation(String server) {
        String csvFile = "C:\\HEART\\" + server + "\\osconfig.csv";  //  location .csv file containing outstations
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            Statement statement = conn.createStatement();
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] os = line.split(cvsSplitBy); //  split the .csv row in to columns
                int count = 0;
                String idTag = server + "::" + os[1];
                String idTag2 = server + "::os:" + os[1];
                //  create SQL statement to add outstation to the allOutstations table
                String batchSQL = "IF NOT EXISTS (SELECT idTag FROM allOutstations WHERE idTag = '" + idTag + "') BEGIN INSERT INTO allOutstations (idTag, server, OSName, OSNumber) VALUES ('" + idTag + "', '" + server + "', '" + os[0] + "', '" + os[1] + "') END;";
                //  create SQL statement to add outstation to the connections table as secondary item for 'os' - needed for tree
                String batchSQL2 = "IF NOT EXISTS (SELECT idTag FROM connections WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO connections (idTag, server, primaryItem, secondaryItem, connectionType) VALUES ('" + idTag2 + "', '" + server + "', 'os', '" + os[1] + "', 'OS') END;";                
                //  add SQL statements to batch
                statement.addBatch(batchSQL);
                statement.addBatch(batchSQL2);
                System.out.println(os[1]);  //  print outstation number
            }
            System.out.println("start");
            statement.executeBatch();  //  execute batch of SQL statements
            System.out.println("finish");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        //System.out.println("Done");
    }

    /*
     * method to add the OS-DB connections where the OS is used and add the DB point to SQLDB
     */
    public void osMappingCSV(String server) {
        String csvFile = "C:\\HEART\\" + server + "\\osmapping.csv";  //  location .csv file containing outstation mapping
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String type = "OSDB";  //  type of connection to be inputted to SQL DB
        try {
            Statement statement1 = conn.createStatement();
            Statement statement2 = conn.createStatement();
            br = new BufferedReader(new FileReader(csvFile));
            //  while there are rows/lines of the .csv
            while ((line = br.readLine()) != null) {
                String[] os = line.split(cvsSplitBy); //  split the .csv row by column in to list of strings
                int count = 0;
                // check to see whether the OS referenced in the mapping .csv exists in the SQLDB
                String sql = "SELECT COUNT (*) AS count FROM allOutstations WHERE OSNumber = '" + os[1] + "' AND server = '" + server + "'";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                // while statement to check the count until it goes above 1 (until an OS is found)
                while (rs.next() && count == 0) {
                    count = rs.getInt("count");  //  create a string for each of the names in the database
                }
                //  if the OS is found then add the OS-DB connection to SQLDB and add basic DB information to SQLDB 
                if (count > 0) {
                    String idTag = server + "::" + os[1] + ":" + os[0];
                    String idTag2 = server + "::" + os[0];
                    //  create SQL statement to add outstation and database point to connections table
                    statement1.addBatch("IF NOT EXISTS (SELECT idTag FROM connections WHERE idTag = '" + idTag + "') BEGIN  INSERT INTO connections (idTag, server, primaryItem, secondaryItem, connectionType) VALUES ('" + idTag + "', '" + server + "', '" + os[1] + "', '" + os[0] + "', '" + type + "') END;");
                    System.out.println("OS [DB Point= " + os[0] + " , OS Number=" + os[1] + "]");
                    String pointNumber = os[0];
                    String outstation = os[1];
                    //  check the first character of the DB point number to give the type (b - Boolean, c - Character String, e - Engineering Unit, etc)
                    //  create SQL statement to add database point to the allDBPoints table
                    switch (pointNumber.charAt(0)) {
                        case 'b':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Boolean', '" + outstation + "') END;");
                            break;
                        case 'B':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Boolean', '" + outstation + "') END;");
                            break;
                        case 'c':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Character String', '" + outstation + "') END;");
                            break;
                        case 'C':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Character String', '" + outstation + "') END;");
                            break;
                        case 'e':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Engineering Unit', '" + outstation + "') END;");
                            break;
                        case 'E':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Engineering Unit', '" + outstation + "') END;");
                            break;
                        case 's':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Schedule', '" + outstation + "') END;");
                            break;
                        case 'S':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Schedule', '" + outstation + "') END;");
                            break;
                        case 'p':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Profile', '" + outstation + "') END;");
                            break;
                        case 'P':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Profile', '" + outstation + "') END;");
                            break;
                        case 'm':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Multibit', '" + outstation + "') END;");
                            break;
                        case 'M':
                            statement2.execute("IF NOT EXISTS (SELECT idTag FROM allDBPoints WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag2 + "', '" + server + "', '" + pointNumber + "', 'Multibit', '" + outstation + "') END;");
                            break;
                        default:
                            break;
                    }
                } else {
                    System.out.println("Not Used [DB Point= " + os[0] + " , OS Number=" + os[1] + "]");
                }
            }
            System.out.println("Database is now updating OS-DB connections...");
            statement1.executeBatch();  //  execute batch sql statement
            System.out.println("   ...complete");
            System.out.println("Database is now updating allDBPoins...");
            statement2.executeBatch();  //  execute batch sql statement
            System.out.println("   ...complete");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to add the alarm to the SQL DB
     */
    public void alarmCSV(String server) {
        String csvFile = "C:\\HEART\\" + server + "\\alarm.csv";  //  .csv file location
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String type = "Alarm";  //  type of connection to be inputted to SQLDB
        try {
            Statement statement1 = conn.createStatement();
            Statement statement2 = conn.createStatement();
            br = new BufferedReader(new FileReader(csvFile));
            //  while there are rows/lines of the .csv
            while ((line = br.readLine()) != null) {
                String[] os = line.split(cvsSplitBy); //  split the .csv row by column in to list of strings
                int count = 0;
                // check to see whether the OS referenced in the mapping .csv exists in the SQL DB
                String sql = "SELECT COUNT (*) AS count FROM allDBPoints WHERE pointNumber = '" + os[0] + "' AND server = '" + server + "'";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                // while statement to check the count until it goes above 1 (until an alarm is found)
                while (rs.next() && count == 0) {
                    count = rs.getInt("count");  //  create a string for each of the names in the database
                }
                //  if the alarm is found then add the alarm connection to SQL DB and add alarm information to SQL DB 
                if (count > 0) {
                    String idTag = server + "::" + os[0] + ":" + os[3];
                    String idTag2 = server + "::" + os[3];
                    //  create SQL statement to add the connection between the alarm and the outstation to the connections table
                    statement1.addBatch("IF NOT EXISTS (SELECT idTag FROM connections WHERE idTag = '" + idTag + "') BEGIN  INSERT INTO connections (idTag, server, primaryItem, secondaryItem, connectionType) VALUES ('" + idTag + "', '" + server + "', '" + os[0] + "', '" + os[3] + "', '" + type + "') END;");
                    //  create SQL statement to add the alarm to the allAlarms table
                    statement2.addBatch("IF NOT EXISTS (SELECT idTag FROM allAlarms WHERE idTag = '" + idTag2 + "') BEGIN INSERT INTO allAlarms (idTag, server, alarmName, dbPoint) VALUES ('" + idTag2 + "', '" + server + "', '" + os[3] + "', '" + os[0] + "') END;");
                    System.out.println("Alarm [DBPoint = " + os[0] + ", Name =" + os[3] + "]");
                } else {
                    System.out.println("Not Used [Alarm = " + os[3] + "]");
                }
            }
            System.out.println("Database is now updating alarm connections...");
            statement1.executeBatch();  //  execute batch sql statement
            System.out.println("   ...complete");
            System.out.println("Database is now updating allAlarms ...");
            statement2.executeBatch();  //  execute batch sql statement
            System.out.println("   ...complete");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to update DBPoints, name and number of connections
     */
    public void dbUpdateCSV(String server) {
        String csvFile = "C:\\HEART\\" + server + "\\db.csv";  //  .csv file location
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(csvFile));
            //  while there are rows/lines of the .csv
            while ((line = br.readLine()) != null) {
                String[] os = line.split(cvsSplitBy); //  split the .csv row by column in to list of strings
                int count = 0;
                // create an SQL statement that counts all of the occurances of the db point in the connections table and update the allDBPoints table
                String sql = "UPDATE allDBPoints SET pointName = '" + os[1] + "',"
                        + " primaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + os[0] + "' AND server = '" + server + "'),"
                        + " secondaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + os[0] + "' AND server = '" + server + "'),"
                        + " totalCon = (SELECT COUNT (*) FROM connections WHERE (primaryItem = '" + os[0] + "' OR secondaryItem = '" + os[0] + "') AND server = '" + server + "')"
                        + " WHERE pointNumber = '" + os[0] + "' AND server = '" + server + "'";
                pst = conn.prepareStatement(sql);
                pst.execute();  //  execute SQL statement
                System.out.println("UPDATE allDBPoints SET pointName = '" + os[1] + "' WHERE pointNumber = '" + os[0] + "' AND server = '" + server + "'");
            }
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to delete duplicates - - - works, but does not get called - - - not needed anymore, SQL DB handles duplication
     */
    public void removeConnectionDuplicates() {
        try {
            String sql = "DELETE FROM connections WHERE ID NOT IN (SELECT min(id) FROM connections GROUP BY primaryItem, secondaryItem)";
            pst = conn.prepareStatement(sql);
            pst.execute();
            //System.out.println("Duplicates have been removed from the database");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
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
     * method to work out the type of a DBPoint based on the first letter
     * then call the method to add the DBPoint data to DBSQL
     */
    public void addDBPoint(String s, String os, String server) {
        switch (s.charAt(0)) {
            case 'b':
                insertDBPoint(server, s, "Boolean", os);
                numberDBPoint++;
                break;
            case 'B':
                insertDBPoint(server, s, "Boolean", os);
                numberDBPoint++;
                break;
            case 'c':
                insertDBPoint(server, s, "Character String", os);
                numberDBPoint++;
                break;
            case 'C':
                insertDBPoint(server, s, "Character String", os);
                numberDBPoint++;
                break;
            case 'e':
                insertDBPoint(server, s, "Engineering Unit", os);
                numberDBPoint++;
                break;
            case 'E':
                insertDBPoint(server, s, "Engineering Unit", os);
                numberDBPoint++;
                break;
            case 's':
                insertDBPoint(server, s, "Schedule", os);
                numberDBPoint++;
                break;
            case 'S':
                insertDBPoint(server, s, "Schedule", os);
                numberDBPoint++;
                break;
            case 'p':
                insertDBPoint(server, s, "Profile", os);
                numberDBPoint++;
                break;
            case 'P':
                insertDBPoint(server, s, "Profile", os);
                numberDBPoint++;
                break;
            case 'm':
                insertDBPoint(server, s, "Multibit", os);
                numberDBPoint++;
                break;
            case 'M':
                insertDBPoint(server, s, "Multibit", os);
                numberDBPoint++;
                break;
            default:
                break;
        }
        //System.out.println("DB Point: " + s);
    }

    /*
     * method to add DBPoint data to DB SQL
     */
    public void insertDBPoint(String server, String pointNumber, String type, String os) {
        String idTag = server + "::" + pointNumber;
        try {
            //  create the SQL query using the variables that are parsed in
            String sql = "INSERT INTO allDBPoints (idTag, server, pointNumber, type, outstation) VALUES ('" + idTag + "', '" + server + "', '" + pointNumber + "', '" + type + "', '" + os + "')";
            pst = conn.prepareStatement(sql);
            pst.execute();  //  execute SQL query
            //System.out.println("Database entry: " + server + " server, " + pointName + " Mimic, " + type + " LoC,  containDB points");
        } catch (Exception e) {
            // JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to update the mimic in SQLDB with number of connections
     */
    public void updateConnectionsMimic() {
        try {
            // Connection connection = new getConnection();
            Statement statement = conn.createStatement();
            String sql = "SELECT mimicName, server FROM allMimics";  // select mimics that are in SQLDB - these are to be updated
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("mimicName");  //  get the name of mimic
                String itemServer = rs.getString("server");  //  get the server of mimc
                System.out.println("Counting connections for " + itemName);
                /*
                 * batchSQL works by counting: 
                 *  1) the number of times the mimic is a primary item for a connection
                 *  2) the number of times the mimic is a secondary item for a connection
                 *  3) the number of times the mimic appears for a connection
                 * it then updates the mimic data with the number of connections
                 */
                String batchSQL = "UPDATE allMimics SET primaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + itemName + "' AND server = '" + itemServer + "'), "
                        + "secondaryCon = (SELECT COUNT (*) FROM connections WHERE secondaryItem = '" + itemName + "' AND server = '" + itemServer + "'), "
                        + "totalCon = (SELECT COUNT (*) FROM connections WHERE (primaryItem = '" + itemName + "' OR secondaryItem = '" + itemName + "') AND server = '" + itemServer + "') "
                        + "WHERE mimicName = '" + itemName + "' AND server = '" + itemServer + "';";
                statement.addBatch(batchSQL);  //  add query to batch sql statement  
            }
            System.out.println("Please wait whilst database is updated. This may take some time...");
            statement.executeBatch();  //  execute the batch sql statement
            System.out.println("   ...completed");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to update the DBPoint in SQLDB with number of connections
     */
    public void updateConnectionsDBPoint() {
        try {
            // Connection connection = new getConnection();
            Statement statement = conn.createStatement();
            String sql = "SELECT pointNumber, server FROM allDBPoints";  // select DB points that are in SQLDB - these are to be updated
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("pointNumber");  //  get the number of the DB point
                String server = rs.getString("server");  //  get the server of DB point
                System.out.println("Counting connections for " + itemName);
                /*
                 * batchSQL works by counting: 
                 *  1) the number of times the DBPoint is a primary item for a connection
                 *  2) the number of times the DBPoint is a secondary item for a connection
                 *  3) the number of times the DBPoint appears for a connection
                 * it then updates the mimic data with the number of connections
                 */
                String batchSQL = "UPDATE allDBPoints SET primaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + itemName + "' AND server = '" + server + "'), "
                        + "secondaryCon = (SELECT COUNT (*) FROM connections WHERE secondaryItem = '" + itemName + "' AND server = '" + server + "'), "
                        + "totalCon = (SELECT COUNT (*) FROM connections WHERE (primaryItem = '" + itemName + "' OR secondaryItem = '" + itemName + "') AND server = '" + server + "') WHERE pointNumber = '" + itemName + "' AND server = '" + server + "'";
                statement.addBatch(batchSQL);  //  add query to batch sql statement  
            }
            System.out.println("Please wait whilst database is updated. This may take some time...");
            statement.executeBatch();  //  execute the batch sql statement
            System.out.println("   ...completed");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to update the Outstation in SQL DB with number of connections
     */
    public void updateConnectionsOutstation() {
        try {
            // Connection connection = new getConnection();
            Statement statement = conn.createStatement();
            String sql = "SELECT osNumber, server FROM allOutstations";  // select outstation that are in SQL DB - these are to be updated
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("osNumber");  //  get the number of outstation
                String server = rs.getString("server");  //  get the server of mimc
                System.out.println("Counting connections for " + itemName);
                /*
                 * batchSQL works by counting: 
                 *  1) the number of times the Outstation is a primary item for a connection
                 *  2) the number of times the Outstation is a secondary item for a connection
                 *  3) the number of times the Outstation appears for a connection
                 * it then updates the mimic data with the number of connections
                 */
                String batchSQL = "UPDATE allOutstations SET primaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + itemName + "' AND server = '" + server + "'), "
                        + "secondaryCon = (SELECT COUNT (*) FROM connections WHERE secondaryItem = '" + itemName + "' AND server = '" + server + "'), "
                        + "totalCon = (SELECT COUNT (*) FROM connections WHERE (primaryItem = '" + itemName + "' OR secondaryItem = '" + itemName + "') AND server = '" + server + "') WHERE osNumber = '" + itemName + "' AND server = '" + server + "'";
                statement.addBatch(batchSQL);  //  add query to batch sql statement  
            }
            System.out.println("Please wait whilst database is updated. This may take some time...");
            statement.executeBatch();  //  execute the batch sql statement
            System.out.println("   ...complete");
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
        }
    }

    /*
     * method to update the Alarm in SQLDB with number of connections
     */
    public void updateConnectionsAlarm() {
        try {
            // Connection connection = new getConnection();
            Statement statement = conn.createStatement();
            String sql = "SELECT alarmName, server FROM allAlarms";  // select alarms that are in SQLDB - these are to be updated
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("alarmName");  //  get the number of the alarm
                String itemServer = rs.getString("server");  //  get the server of alarm
                System.out.println("Counting connections for " + itemName);
                /*
                 * batchSQL works by counting: 
                 *  1) the number of times the alarm is a primary item for a connection
                 *  2) the number of times the alarm is a secondary item for a connection
                 *  3) the number of times the alarm appears for a connection
                 * it then updates the mimic data with the number of connections
                 */
                String batchSQL = "UPDATE allAlarms SET primaryCon = (SELECT COUNT (*) AS count FROM connections WHERE primaryItem = '" + itemName + "' AND server = '" + itemServer + "'), "
                        + "secondaryCon = (SELECT COUNT (*) FROM connections WHERE secondaryItem = '" + itemName + "' AND server = '" + itemServer + "'), "
                        + "totalCon = (SELECT COUNT (*) FROM connections WHERE (primaryItem = '" + itemName + "' OR secondaryItem = '" + itemName + "') AND server = '" + itemServer + "') WHERE alarmName = '" + itemName + "' AND server = '" + itemServer + "';";
                statement.addBatch(batchSQL);  //  add query to batch sql statement  
            }
            System.out.println("Please wait whilst database is updated. This may take some time...");
            statement.executeBatch();  //  execute the batch sql statement
            System.out.println("   ...complete");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
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

    public void scanMimic(String filename, String fileline, String server) {
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
                                mimicReadToDo[mimicToDoLength] = foundMimic;  //  add the mimic to the list
                                mimicToDoLength++;  //  increment the list length
                                count--;  //  stop the while loop
                            }
                        }
                        else if (foundMimic.equals(mimicReadToDo[count])) {
                            count = -1;  //  mimic is already in the list, exit the while loop
                        } else {
                            count--;  //  check the next item in the list
                        }

                    }
                    //System.out.println(currentMimic + " contains " + foundMimic);
                    //  insert the newly found mimic in to the SQL DB
                    insertConnection(server, currentMimic, foundMimic, "mimic");//*************************************** needs to be uncommented
                } else if (s.equalsIgnoreCase("object")) {
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
                    }
                } /*} else if (isNumeric(s)) {
                 System.out.println("OS: " + s);
                 }*/ else if (Character.isDigit(s.charAt(1))) {
                    if (isDPBPoint(s)) {
                        addDBPoint(s + " Mimic", "Mimic: " + filename, server);
                    }
                } else {

                    if (insideObjectShape && shapeArray.contains(s)) {
                        numberTotalShapes++;
                        numberObjectShape++;
                        insideObjectShape = false;
                    } else if (shapeArray.contains(s)) {
                        numberTotalShapes++;
                    }
                }
            }
        }
    }

    public void readMimic(String filename, String server) {
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
                        linesOfCode++; // increment the lines of code by 1
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
            //System.out.println("Cannot Find: " + filename);
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
