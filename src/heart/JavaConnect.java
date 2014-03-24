/*
 * The Firstco Assistant Resourcing Tool, designed and built by Ben Searle
 * for IN2030 Work Based Project at City University London
 */
package heart;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 * @author Ben Searle
 */

public class JavaConnect {
    // define conn as the connection to the database
    Connection conn = null;
    public static Connection ConnectDB(){
        try{
            //  use JDBC driver for SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //  full url of database with login credentials
            String connectionUrl = "jdbc:sqlserver://fst-dt40\\SQLEXPRESS:1433;databaseName=HEART;user=bensearle;password=BenSearle8;";
            Connection con = DriverManager.getConnection(connectionUrl);
            //    JOptionPane.showMessageDialog(null, "Connection Established");
            return con;
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }
}
