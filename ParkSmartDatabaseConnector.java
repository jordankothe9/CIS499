/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parksmartdatabaseconnector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jorda
 */
public class ParkSmartDatabaseConnector {

    Statement globalStatement;

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     */
    public static void main(String[] args) {
        SQLconnection sql;
        boolean PiIntialed = false;
        ServerSocket ss;

        try {

            //load the properties file
            InputStream isprop = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(isprop);
            String Address = prop.getProperty("address");
            int sqlport = Integer.valueOf(prop.getProperty("sqlport"));
            String user = prop.getProperty("user");
            String password = prop.getProperty("pass");
            boolean ModePlate = prop.getProperty("Mode","Plate").equals("Plate");
            //check the properties file
            if (Address.equals("sql.domain.com")) {
                System.out.println("Config file not setup!");
                exit(1);
            }

            //sql instance creation
            sql = new SQLconnection(Address, sqlport, user, password);

            //open the port 1619
            ss = new ServerSocket(Integer.valueOf(prop.getProperty("port")));

            while (ModePlate) {
                checkForMessages(ss, sql);

            }

        } catch (SQLException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("The SQL connection failed or there was a schema violation");

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);

            System.out.println("SQL driver not found, make sure sqlijdbc4.jar is in the lib folder");
        } catch (IOException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void checkForMessages(ServerSocket ss, SQLconnection sql) throws IOException {
        //create a socket for the clients to connect too
        PiScanner Pi = new PiScanner(ss);

        //get the string from the connected client. The client is disconnected each time it's done sending a string
        String message = Pi.getString();

        //output the message from the plate scanner to the console
        System.out.println("Message recived from " + Pi.getClientIP() + ": \n" + message);

        //send the string to the database
        proccessVehicleSTR(sql, message);
    }

    public static void proccessVehicleSTR(SQLconnection sql, String message) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(message));
            reader.readLine(); // throw away opening bracket
            reader.readLine(); // throw away VehicleType

            String Plate = getString(reader.readLine()); //LicensePlate

            float confidence = Float.parseFloat(getdata(reader.readLine()));

            String Make = getString(reader.readLine()); //Make            

            String Model = getString(reader.readLine()); //model            

            String Color = getString(reader.readLine()); //Color

            String Region = getString(reader.readLine()); //get the state

            //is the car going in or out?, Then convert status to boolean         
            boolean entry = getString(reader.readLine()).equals("in");

            String TimeStampSTR = getString(reader.readLine()); //time stamp in ms since 1970 or "now"
            Timestamp stamp;
            if (TimeStampSTR.contains("now")) {
                stamp = new Timestamp(System.currentTimeMillis()); //Set the timestamp to the current time
            } else {
                stamp = new Timestamp((long) Double.parseDouble(TimeStampSTR)); //convert timestamp to an int and then an actual time stamp
            }

            sql.addVehicle(Plate, Color, Make, Region, Model, confidence);
            sql.addHistory(Plate, entry, 1, stamp);

        } catch (SQLException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    //This method extracts String information from the "JSON" data being sent from the scanner
    public static String getString(String line) {
        StringTokenizer stk = new StringTokenizer(line, "\"");
        stk.nextToken(); //throw away emtpy space
        stk.nextToken(); //throw away title of the data
        stk.nextToken(); //throw away the ':'
        String data = stk.nextToken();
        return data;
    }

    //This method extracts the number values from the "JSON" data being sent from the scanner
    public static String getdata(String line) {
        //get the String after the ':'
        StringTokenizer stk = new StringTokenizer(line, ":");
        stk.nextToken();
        String data = stk.nextToken();

        //Get the String within the double quotes
        stk = new StringTokenizer(data, ",");
        data = stk.nextToken();
        return data;
    }

    //the following methods were only used in testing:
    public static void manualAddVehical(SQLconnection sql) throws SQLException {
        Scanner input = new Scanner(System.in);
        //input from user
        System.out.println("enter pass number");
        String Pass = input.next();
        System.out.println("enter plate number");
        String PlateNumber = input.next();
        System.out.println("enter Region");
        String Region = input.next();
        System.out.println("Enter Color");
        String Color = input.next();
        System.out.println("Enter Make followed by model");
        String Make = input.next();
        String Model = input.next();

        //send to db
        sql.addVehicle(Pass, PlateNumber, Color, Make, Region, Model);
    }

    public static void proccessVehicleSTIN(SQLconnection sql) {
        try {
            Scanner input = new Scanner(System.in);
            input.nextLine(); // throw away opening bracket
            input.nextLine(); // throw away VehicleType
            String Plate = input.nextLine(); //LicensePlate
            Plate = getString(Plate);
            String plateconf = input.nextLine(); //LicensePlate accuracy
            plateconf = getdata(plateconf);
            //convert plateconf to float
            float confidence = (float) Double.parseDouble(plateconf);
            String Make = input.nextLine(); //Make
            Make = getString(Make);
            String Model = input.nextLine(); //model
            Model = getString(Model);
            String Color = input.nextLine(); //Color
            Color = getString(Color);
            String Region = input.nextLine(); //get the state
            Region = getString(Region);
            String Status = input.nextLine(); //is the car going in or out?
            Status = getString(Status);
            //conver status into bool
            boolean entry = Status.equals("in");
            String TimeStamp = input.nextLine(); //time stamp in ms since 1970
            TimeStamp = getString(TimeStamp);
            //convert timestamp to an int and then an actual time stamp
            Timestamp stamp = new Timestamp((long) Double.parseDouble(TimeStamp));
            input.nextLine(); //throw away closing bracket

            sql.addVehicle(Plate, Color, Make, Region, Model, confidence);
            sql.addHistory(Plate, entry, 1, stamp);

        } catch (SQLException ex) {
            Logger.getLogger(ParkSmartDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

}
