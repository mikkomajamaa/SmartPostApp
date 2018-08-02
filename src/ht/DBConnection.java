/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import static java.lang.Math.round;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mikko
 */
public class DBConnection {
    private ArrayList<SmartPost> sps = null;
    private ArrayList<PostPackage> pps = null;
    private ArrayList<Thing> ts = null;
    private int thingTrigger = 1;
    private int packageInfoTabTrigger = 1;
    private int personTabTrigger = 1;
    private ArrayList<Double> classInfo = null;
    private URL url;
    BufferedReader br;
    private String content = "";
    String line = "";
    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private Document doc;
    private NodeList nodesCode;
    private NodeList nodesCity;
    private NodeList nodesAddress;
    private NodeList nodesAvailability;
    private NodeList nodesPostOffice;
    private NodeList nodesLat;
    private NodeList nodesLng;
    private int i;
    private int j;
    private Node nodeCode;
    private Node nodeCity;
    private Node nodeAddress;
    private Node nodeAvailability;
    private Node nodePostOffice;
    private Node nodeLat;
    private Node nodeLng;
    private Element eCode;
    private Element eCity;
    private Element eAddress;
    private Element eAvailability;
    private Element ePostOffice;
    private Element eLat;
    private Element eLng;
    private int trigger;
    private static DBConnection instance = null;
    Connection conn = null;
    PreparedStatement pstmt = null;

    protected DBConnection() {
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;

    }

    //trigger is set by the user in the inializer window based on whether he or she wants to continue the last session or start a new one
    public void setTrigger(int i) {
        trigger = i;
    }

    public int getTrigger() {
        return trigger;
    }
    
    //triggers below are used to update the info on comboboxes only when the data has changed
    public void setThingTrigger(int i) {
        thingTrigger = i;
    }
    
    public int getThingTrigger() {
        return thingTrigger;
    }
    
    public void setPersonTabTrigger(int i) {
        personTabTrigger = i;
    }
    
    public int getPersonTabTrigger() {
        return personTabTrigger;
    }

    public void setPackageInfoTabTrigger(int i) {
        packageInfoTabTrigger = i;
    }
    
    public int getPackageInfoTabTrigger() {
        return packageInfoTabTrigger;
    }
    //parse XML and insert the SPs' data to the DB
    public void XMLParser() throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
        url = new URL("http://iseteenindus.smartpost.ee/api/?request=destinations&country=FI&type=APT");
        br = new BufferedReader(new InputStreamReader(url.openStream()));

        while ((line = br.readLine()) != null) {
            content += line + "\n";
        }

        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();

        doc = dBuilder.parse(new InputSource(new StringReader(content)));
        doc.getDocumentElement().normalize();

        nodesCode = doc.getElementsByTagName("postalcode");
        nodesCity = doc.getElementsByTagName("city");
        nodesAddress = doc.getElementsByTagName("address");
        nodesAvailability = doc.getElementsByTagName("availability");
        nodesPostOffice = doc.getElementsByTagName("name");
        nodesLat = doc.getElementsByTagName("lat");
        nodesLng = doc.getElementsByTagName("lng");

        for (i = 0; i < nodesCode.getLength(); i++) {
            nodeCode = nodesCode.item(i);
            nodeCity = nodesCity.item(i);
            nodeAddress = nodesAddress.item(i);
            nodeAvailability = nodesAvailability.item(i);
            nodePostOffice = nodesPostOffice.item(i);
            nodeLat = nodesLat.item(i);
            nodeLng = nodesLng.item(i);

            eCode = (Element) nodeCode;
            eCity = (Element) nodeCity;
            eAddress = (Element) nodeAddress;
            eAvailability = (Element) nodeAvailability;
            ePostOffice = (Element) nodePostOffice;
            eLat = (Element) nodeLat;
            eLng = (Element) nodeLng;

            String sql = "INSERT OR IGNORE INTO SmartPost(nimi, aukioloajat, latitude, longitude, osoite, kaupunki, postinro) VALUES(?,?,?,?,?,?,?)";
            pstmt = null;
            try {
                pstmt = this.conn.prepareStatement(sql);
                pstmt.setString(1, ePostOffice.getTextContent());
                pstmt.setString(2, eAvailability.getTextContent());
                pstmt.setString(3, eLat.getTextContent());
                pstmt.setString(4, eLng.getTextContent());
                pstmt.setString(5, eAddress.getTextContent());
                pstmt.setString(6, eCity.getTextContent().toUpperCase());
                pstmt.setString(7, eCode.getTextContent());
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //connect to the DB
    public void connect() {
        try {
            //db parameters
            String url = "jdbc:sqlite:SmartPostDB";
            //create a connection to the database
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

        public ArrayList<PostPackage> getPps() {
        if (pps == null) {
            pps = new ArrayList<>();
            this.connect();
            PreparedStatement stmt = null;
            try {
                String sql = "SELECT * FROM paketti";
                stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    PostPackage pp = new PostPackage(rs.getInt("pktid"));
                    pp.setClass(rs.getInt("luokkanro"));
                    System.out.println("uusi paketti");
                    pps.add(pp);
                }
                
                sql = "SELECT * FROM sisaltaa";
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    
                }
                
                
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.closeConnection();
        }
        return pps;
    }
        
         public ArrayList<Double> getClassInfo() {
        if (classInfo == null) {
            this.connect();
            classInfo = new ArrayList<>();
            String sql = "SELECT * FROM luokka";
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    classInfo.add(rs.getDouble("luokkanro"));
                    classInfo.add(rs.getDouble("painoraja"));
                    classInfo.add(rs.getDouble("kokoraja"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.closeConnection();
        }
        return classInfo;
    }
    
    public ArrayList<PostPackage> getPostPackages() {
        this.connect();
        ArrayList<PostPackage> pps = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM lahetystiedot INNER JOIN paketti ON paketti.pktid = lahetystiedot.pktid";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PostPackage p = new PostPackage(rs.getInt("pktid"));
                p.setClass(rs.getInt("luokkanro"));
                p.setFromSP(rs.getInt("lahtospid"));
                p.setToSP(rs.getInt("kohdespid"));
                if ((rs.getInt("lahetetty")) == 1) {
                    p.setSent();
                }
                pps.add(p);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.closeConnection();
        return pps;
    }
            
    public ArrayList<Thing> getTs() {
        if (ts == null) {
            ts = new ArrayList<>();
            this.connect();
            PreparedStatement stmt = null;
            try {
                String sql = "SELECT * FROM esine";
                stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Thing t = new Thing(rs.getInt("esineid"), rs.getString("nimi"), rs.getDouble("koko"), rs.getDouble("paino"), rs.getBoolean("voikoHajota"));
                    ts.add(t);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.closeConnection();
        }
        return ts;    
    }

    public ArrayList<SmartPost> getSps() {
        if (sps == null) {
            sps = new ArrayList<>();
            this.connect();
            PreparedStatement stmt = null;
            try {
                String sql = "SELECT * FROM SmartPost";
                stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    SmartPost sp = new SmartPost(rs.getInt("SPid"),
                            rs.getString("nimi"), rs.getString("aukioloajat"),
                            rs.getString("latitude"), rs.getString("longitude"),
                            rs.getString("osoite"), rs.getString("postinro"),
                            rs.getString("kaupunki"));
                    sps.add(sp);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.closeConnection();
        }
        return sps;
    }
    
    //get all different cities that have SP(s) from the DB
    public ArrayList<String> getCities() {
        this.connect();
        ArrayList<String> cities = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT kaupunki FROM SmartPost;");
            while (rs.next()) {
                cities.add(rs.getString("kaupunki"));

            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.closeConnection();
        return cities;
    }

    //get the string whom with one can draw SP on to the map based on SP's ID
    public String getSmartPostsDrawOnID(int id) {
        this.connect();
        String sp = null;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM SmartPost WHERE spid = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String location = "'" + rs.getString("osoite") + ", " + rs.getString("postinro") + " " + rs.getString("kaupunki") + "'";
                String info = "'" + rs.getString("nimi") + rs.getString("aukioloajat") + "'";
                String color = "'red'";
                this.closeConnection();
                return "document.goToLocation(" + location + "," + info + ", " + color + ")";
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return sp;
    }

    //get all the ID(s) of SP(s) from a particular city
    public ArrayList<Integer> getSmartPostIDs(String city) {
        this.connect();
        ArrayList<Integer> sps = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM SmartPost WHERE kaupunki = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sps.add(rs.getInt("spid"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return sps;
    }

    //get all the name(s) of SP(s) from a particular city
    public ArrayList<String> getSmartPosts(String city) {
        this.connect();
        ArrayList<String> sps = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM SmartPost WHERE kaupunki = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sps.add(rs.getString("nimi"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return sps;
    }

    //get all the things from a particular package
    public ArrayList<String> getThingsInPackage(int packageId) {
        this.connect();
        ArrayList<String> things = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT esine.nimi FROM sisaltaa INNER JOIN esine ON sisaltaa.esineid = esine.esineid WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                things.add(rs.getString("nimi"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return things;
    }

    //get SP's ID based on it's name
    public int getSmartPostId(String name) {
        this.connect();
        SmartPost sp = null;
        PreparedStatement stmt = null;
        int temp = 0;
        try {
            String sql = "SELECT * FROM SmartPost WHERE nimi = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getInt("SPid");
            }
            this.closeConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //select smartpost.spid from smartpost inner join lahetystiedot on smartpost.latitude = lahetystiedot.kohdelat and smartpost.longitude = lahetystiedot.kohdelon where pktid = 1;
    public int getSmartPostIdonPackageId(int id) {
        this.connect();
        SmartPost sp = null;
        PreparedStatement stmt = null;
        int temp = 0;
        try {
            String sql = "SELECT * FROM sijaitsee WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getInt("SPid");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get to SP's ID based on package's ID
    public int getToSmartPostIdonPackageId(int packageid) {
        this.connect();
        SmartPost sp = null;
        PreparedStatement stmt = null;
        int temp = 0;
        try {
            String sql = "SELECT smartpost.spid AS SPid FROM smartpost INNER JOIN"
                    + " lahetystiedot ON smartpost.spid = lahetystiedot.kohdespid WHERE pktid = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getInt("SPid");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get the package's route in a form of an array ([fromlat, fromlon, tolat, tolon])
    public ArrayList<String> getRoute2(int id) {
        this.connect();
        ArrayList<String> temp = new ArrayList<>();
        String sql = "SELECT * FROM \"lahto-ja_saapumisgeografinensijainti\" WHERE pktid = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp.add(rs.getString("lahtolat"));
                temp.add(rs.getString("lahtolon"));
                temp.add(rs.getString("kohdelat"));
                temp.add(rs.getString("kohdelon"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;

    }

    //get a desired column from SP based on SP's name
    public String getSmartPostString(String s, String r) {
        this.connect();
        SmartPost sp = null;
        PreparedStatement stmt = null;
        String temp = null;
        try {
            String sql = "SELECT * FROM SmartPost WHERE nimi = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getString(r);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get thing's ID based on it's name
    public int getThingId(String name) {
        this.connect();
        int temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM esine WHERE nimi = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getInt("esineid");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get boolean on whether the package is sent or not based on it's ID
    public boolean isSent(int packageId) {
        this.connect();
        boolean temp = true;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM lahetystiedot WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getBoolean("lahetetty");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get boolean on whether the thing is breakable or not based on it's ID
    public boolean isBreakable(int thingId) {
        this.connect();
        boolean temp = true;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM esine WHERE esineid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, thingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getBoolean("voikoHajota");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get the routes length based on package's ID
    public double getRouteLength(int packageId) {
        this.connect();
        double temp = 0.0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM lahetystiedot WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getDouble("matkanpituus");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }
    

    
    //get boolean on whether the package is empty or not based on it's ID
    public boolean isPackageEmpty(int packageId) {
        this.connect();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT COUNT (*) FROM sisaltaa WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) != 0) {
                    this.closeConnection();
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.closeConnection();
        return false;
    }

    //set the package sent based on it's ID
    public void setSent(int packageId) {
        this.connect();
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE lahetystiedot SET lahetetty = 1 WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //set the packages length based on it's ID
    public void setLength(double length, int packageid) {
        this.connect();
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE lahetystiedot SET matkanpituus = ? WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, length);
            stmt.setInt(2, packageid);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //set package's leaving date and time and package's arriving date and time based on leaving date and time, routes length and the packages class
    public void setDates(double length, int packageid, int packageclass) {
        this.connect();
        double speed = 0;
        //packages class determines the speed off the package
        switch (packageclass) {
            case 1:
                //first class: 80km/h
                speed = 80 / 3.6 / 1000;
                break;
            case 2:
                //second class: 60km/h
                speed = 60 / 3.6 / 1000;
                break;
            case 3:
                //third class: 40km/h
                speed = 40 / 3.6 / 1000;
                break;
        }
        
        //seconds needed for the particular route based on speed and the routes length
        int seconds = (int) round(length / speed);

        Timestamp timestamp = new Timestamp(new Date().getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());

        //add needed seconds for the route to current time
        cal.add(Calendar.SECOND, seconds);
        timestamp = new Timestamp(cal.getTime().getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String arrivingTime = dateFormat.format(timestamp);

        //leaving time is current time
        String leavingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE lahetystiedot SET lahtoaika = ?, saapumisaika = ? WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, leavingTime);
            stmt.setString(2, arrivingTime);
            stmt.setInt(3, packageid);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public void setSenderID(int senderId, int packageId) {
        this.connect();
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE lahetystiedot SET lahettajaid = ? WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, packageId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public void setReceiverID(int senderId, int packageId) {
        this.connect();
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE lahetystiedot SET vastaanottajaid = ? WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, packageId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public int getPackageId() {
        this.connect();
        int temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT MAX(pktid) FROM paketti";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                temp = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get package's weight based on it's ID
    public double getWeight(int packageId) {
        this.connect();
        double temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT SUM(paino) FROM sisaltaa INNER JOIN esine ON sisaltaa.esineid = esine.esineid WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, packageId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                temp = rs.getDouble(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get package's size based on it's ID
    public double getSize(int packageId) {
        this.connect();
        double temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT SUM(koko) FROM sisaltaa INNER JOIN esine ON sisaltaa.esineid = esine.esineid WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, packageId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                temp = rs.getDouble(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    public int getPackagesClass(int id) {
        this.connect();
        int temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM paketti WHERE pktid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                temp = rs.getInt("luokkanro");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get all the package ID's in string format in an arraylist
    public ArrayList<String> getPackageIds() {
        this.connect();
        ArrayList<String> temp = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT pktid FROM paketti";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp.add(Integer.toString(rs.getInt("pktid")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    public void insertThingToPackage(int thingId, int packageId) {
        this.connect();
        String sql = "INSERT INTO sisaltaa(esineid, pktid) VALUES(?,?)";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, thingId);
            pstmt.setInt(2, packageId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //insert package to the DB
    public void insertPackage() {
        this.connect();
        String sql = "INSERT INTO paketti DEFAULT VALUES";
        pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.closeConnection();
    }

    public void setPackagesClass(int id, int cl) {
        this.connect();
        String sql = "UPDATE paketti SET luokkanro = ? WHERE pktid = ?";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, cl);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //insert thing to the DB
    public void insertThing(String n, String s, double w, boolean b) {
        this.connect();
        String sql = "INSERT OR IGNORE INTO esine(nimi, paino, koko, voikoHajota) VALUES(?,?,?,?)";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setString(1, n);
            pstmt.setDouble(2, w);
            pstmt.setString(3, s);
            pstmt.setBoolean(4, b);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public void deleteThingFromDB(String n) {
        this.connect();
        String sql = "DELETE FROM esine WHERE nimi = ?";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setString(1, n);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.closeConnection();
    }

    public void deleteThingFromPackage(int packageId, int thingId) {
        this.connect();
        String sql = "DELETE FROM sisaltaa WHERE pktid = ? AND esineid = ?";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, packageId);
            pstmt.setInt(2, thingId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.closeConnection();
    }

    public void deletePackageFromDB(int id) {
        this.connect();
        String sql1 = "PRAGMA FOREIGN_KEYS = ON;\n";
        String sql2 = "DELETE FROM paketti WHERE pktid = ?";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql1);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql2);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.closeConnection();
    }

    //get all the things from the DB in string format in an arraylist
    public ArrayList<String> getThings() {
        this.connect();
        ArrayList<String> things = new ArrayList<>();
        String sql = "SELECT nimi FROM esine";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                things.add(rs.getString("nimi"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return things;
    }

    //insert package's location
    public void insertLocation(int pid, int spid) {
        this.connect();
        String sql = "INSERT INTO sijaitsee(pktid, spid) VALUES (?,?)";
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, pid);
            pstmt.setInt(2, spid);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //update the package's location
    public void setLocation(int pid, int spid) {
        this.connect();
        String sql = "UPDATE sijaitsee SET SPid = ? WHERE pktid = ?";
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, spid);
            pstmt.setInt(2, pid);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //insert package's from and to SPs' ID's
    public void insertPackageInfo(int packageid, int fromSPid, int toSPid) {
        this.connect();
        String sql = "INSERT INTO lahetystiedot(pktid, lahtospid, kohdespid) VALUES(?,?,?)";
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, packageid);
            pstmt.setInt(2, fromSPid);
            pstmt.setInt(3, toSPid);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //insert a new (postal) class to the DB
    public void insertClass(int c, String s, double w) {
        this.connect();
        String sql = "INSERT INTO luokka(luokkanro, koko, painoraja) VALUES(?,?,?)";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setInt(1, c);
            pstmt.setString(2, s);
            pstmt.setDouble(3, w);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public void insertPersonInfo(String n, String s, String p) {
        this.connect();
        String sql = "INSERT OR IGNORE INTO hlotiedot(etunimi, sukunimi, puhnro) VALUES (?,?,?)";
        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.setString(1, n);
            pstmt.setString(2, s);
            pstmt.setString(3, p);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    //get person's ID based on his/her firstname, surname and phone number
    public int getPersonsID(String n, String s, String p) {
        this.connect();
        int temp = 0;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT hloid FROM hlotiedot WHERE etunimi = ? AND sukunimi = ? AND puhnro = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, n);
            stmt.setString(2, s);
            stmt.setString(3, p);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                temp = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get persons in string format in an arraylist
    public ArrayList<String> getPersons() {
        this.connect();
        ArrayList<String> temp = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM hlotiedot";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp.add(rs.getString("sukunimi") + " " + rs.getString("etunimi") + ", " + rs.getString("puhnro"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    //get (postal) class' weight limit
    public double getWeightLimit(int cl) {
        this.connect();
        double temp = -1;
        String sql = "SELECT * FROM luokka WHERE luokkanro = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cl);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getDouble("painoraja");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }
    //get (postal) class' size limit
    public double getSizeLimit(int cl) {
        this.connect();
        double temp = -1;
        String sql = "SELECT * FROM luokka WHERE luokkanro = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cl);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp = rs.getDouble("kokoraja");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
        return temp;
    }

    public String getCompletePackageInfo(int packageId) {
        this.connect();
        String sql = "SELECT * FROM taydelliset_lahetystiedot WHERE Paketti = ?";
        PreparedStatement stmt = null;
        String s = "";
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                s = "";
                s = "Paketin tunnusnumero: " + rs.getString("Paketti") + ", luokka: " + this.getPackagesClass(packageId) + "\nLähettäjä: " + rs.getString("Lahettajan nimi ja puhelinnumero")
                        + "\nVastaanottaja: " + rs.getString("Vastaanottajan nimi ja puhelinnumero")
                        + "\nLähtöautomaatti: " + rs.getString("Lahto SmarPost")
                        + "\nKohdeautomaatti: " + rs.getString("Kohde SmarPost")
                        + "\nLähtöaika: ";
                if (rs.getString("lahtoaika") == null) {
                    s += "Paketti odottaa kuljetuskäskyä\nSaapumisaika: -\nMatkan pituus: " + rs.getDouble("matkan_pituus");
                } else {
                    s += rs.getString("Lahtoaika") + "\nSaapumisaika: " + rs.getString("Saapumisaika") + "\nMatkan pituus: " + rs.getDouble("matkan_pituus") + " km";
                }
                s += "\nKuljetuksen tila: ";
                int temp1 = this.getToSmartPostIdonPackageId(packageId);
                if (this.getSmartPostIdonPackageId(packageId) == -1) {
                    s += "MATKALLA";
                } else if (this.getSmartPostIdonPackageId(packageId) == this.getToSmartPostIdonPackageId(packageId)){
                    s += "NOUDETTAVISSA";
                    
                } else {
                    s += "Paketti odottaa kuljetuskäskyä";
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.closeConnection();
        return s;
    }

    //on startup update packages location to it's to SP if the arriving time has passed
    public void updateDB() {
        this.connect();
        String sql = "SELECT saapumisaika as aika, pktid, kohdespid FROM lahetystiedot WHERE saapumisaika NOT NULL";
        PreparedStatement stmt = null;
        HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
        try {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String time = rs.getString("aika");
                try {
                    if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(time).before(new Date())) {
                        hm.put(rs.getInt("kohdespid"), rs.getInt("pktid"));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            Set set = hm.entrySet();
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                sql = "UPDATE sijaitsee SET spid = ? WHERE pktid = ?";
                stmt = null;
                stmt = this.conn.prepareStatement(sql);
                Map.Entry me = (Map.Entry) iterator.next();
                stmt.setInt(1, (int) me.getKey());
                stmt.setInt(2, (int) me.getValue());
                stmt.executeUpdate();
                sql = null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();
    }

    public void createDBforStubbornUserWhoChoosesToContinueLastSessionWithoutEverUsingTheProgram() {
        this.connect();
        
        String sql = "PRAGMA FOREIGN_KEYS = ON;\n";
        String sql1 = "CREATE TABLE IF NOT EXISTS SmartPost (\n"
                + "SPid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "\n"
                + "nimi VARCHAR(32),\n"
                + "aukioloajat VARCHAR(40) NOT NULL,\n"
                + "\n"
                + "longitude VARCHAR(20) NOT NULL,\n"
                + "latitude VARCHAR(20) NOT NULL,\n"
                + "\n"
                + "\n"
                + "osoite VARCHAR(30),\n"
                + "postinro VARCHAR(30), \n"
                + "kaupunki VARCHAR(30)\n"
                + ")";
        String sql2 = "CREATE TABLE IF NOT EXISTS esine (\n"
                + "esineid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "nimi VARCHAR(64) NOT NULL,\n"
                + "paino DOUBLE NOT NULL,\n"
                + "koko VARCHAR(12),\n"
                + "voikoHajota BOOLEAN NOT NULL,\n"
                + "\n"
                + "UNIQUE(nimi, paino, koko)\n"
                + ")";
        String sql3 = "CREATE TABLE IF NOT EXISTS paketti (\n"
                + "pktid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "luokkanro INTEGER,\n"
                + "FOREIGN KEY(luokkanro) REFERENCES luokka(luokkanro)\n"
                + ")";
        String sql4 = "CREATE TABLE IF NOT EXISTS luokka (\n"
                + "luokkanro INTEGER UNIQUE NOT NULL PRIMARY KEY,\n"
                + "kokoraja DOUBLE NOT NULL,\n"
                + "painoraja DOUBLE NOT NULL,\n"
                + "\n"
                + "CHECK (luokkanro IN ('1', '2', '3'))\n"
                + ")";
        String sql5 = "CREATE TABLE IF NOT EXISTS lahetystiedot (\n"
                + "pktid INTEGER PRIMARY KEY,\n"
                + "lahtospid INTEGER,\n"
                + "kohdespid INTEGER,\n"
                + "lahettajaid INTEGER,\n"
                + "vastaanottajaid INTEGER,\n"
                + "lahtoaika DATE,\n"
                + "saapumisaika DATE,\n"
                + "matkanpituus DOUBLE,\n"
                + "kohdelat VARCHAR(20),\n"
                + "kohdelon VARCHAR(20),\n"
                + "lahetetty BOOLEAN DEFAULT 0, \n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(lahettajaid) REFERENCES hlotiedot(hloid),\n"
                + "FOREIGN KEY(vastaanottajaid) REFERENCES hlotiedot(hloid)"
                + ")";
        String sql6 = "CREATE TABLE IF NOT EXISTS hlotiedot (\n"
                + "hloid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "etunimi VARCHAR(32) NOT NULL,\n"
                + "sukunimi VARCHAR(32) NOT NULL,\n"
                + "puhnro VARCHAR(13) NOT NULL\n"
                + ")";
        String sql8 = "CREATE TABLE IF NOT EXISTS sijaitsee (\n"
                + "pktid,\n"
                + "SPid,\n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(SPid) REFERENCES SmartPost(SPid)\n"
                + ");";
        String sql9 = "CREATE TABLE IF NOT EXISTS sisaltaa (\n"
                + "pktid,\n"
                + "esineid,\n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(esineid) REFERENCES esine(esineid)\n"
                + ");";
        String sql14 = "INSERT OR IGNORE INTO luokka VALUES (1, 0.027, 5)";
        String sql15 = "INSERT OR IGNORE INTO luokka VALUES (2, 0.008, 5)";
        String sql16 = "INSERT OR IGNORE INTO luokka VALUES (3, 1, 35)";

        String sql17 = "CREATE VIEW IF NOT EXISTS \"lahto-ja_saapumispaikka\"(\"Paketti\", \"Lahto SmarPost\", \"Kohde SmarPost\", lahtoaika, saapumisaika, matkan_pituus)\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"Paketti\",\n"
                + "	(lahto_sp.nimi || \" \" || lahto_sp.osoite || \" \" || lahto_sp.postinro || \" \" || lahto_sp.kaupunki) AS \"Lahto SmarPost\",\n"
                + "	(kohde_sp.nimi || \" \" || kohde_sp.osoite || \" \" || kohde_sp.postinro || \" \" || kohde_sp.kaupunki) AS \"Kohde SmarPost\",\n"
                + "	lahetystiedot.lahtoaika AS \"lahtoaika\", lahetystiedot.saapumisaika AS \"saapumisaika\", lahetystiedot.matkanpituus AS \"matkan_pituus\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN SmartPost AS lahto_sp\n"
                + "ON (lahto_sp.spid = lahetystiedot.lahtospid)\n"
                + "INNER JOIN SmartPost AS kohde_sp\n"
                + "ON (kohde_sp.spid = lahetystiedot.kohdespid)";
        String sql18 = "CREATE VIEW IF NOT EXISTS lahettajat_ja_vastaanottajat (\"Paketti\", \"Lahettajan nimi ja puhelinnumero\", \"Vastaanottajan nimi ja puhelinnumero\")\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"Paketti\",\n"
                + "	(lahettaja.etunimi || \" \" || lahettaja.sukunimi || \", \" || lahettaja.puhnro) AS \"Lahettajan nimi ja puhelinnumero\",\n"
                + "	(vastaanottaja.etunimi || \" \" || vastaanottaja.sukunimi || \", \" || vastaanottaja.puhnro) AS \"Vastaanottajan nimi ja puhelinnumero\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN hlotiedot AS lahettaja\n"
                + "ON (lahettaja.hloid = lahetystiedot.lahettajaid)\n"
                + "INNER JOIN hlotiedot AS vastaanottaja\n"
                + "ON (vastaanottaja.hloid = lahetystiedot.vastaanottajaid)";
        String sql19 = "CREATE VIEW IF NOT EXISTS\"taydelliset_lahetystiedot\"(Paketti, \"Lahettajan nimi ja puhelinnumero\", \"Vastaanottajan nimi ja puhelinnumero\", \"Lahto SmarPost\", \"Kohde SmarPost\", \"lahtoaika\", \"saapumisaika\", \"matkan_pituus\")\n"
                + "AS\n"
                + "SELECT lahettajat_ja_vastaanottajat.Paketti, lahettajat_ja_vastaanottajat.\"Lahettajan nimi ja puhelinnumero\", lahettajat_ja_vastaanottajat.\"Vastaanottajan nimi ja puhelinnumero\",\n"
                + "	\"lahto-ja_saapumispaikka\".\"Lahto SmarPost\" , \"lahto-ja_saapumispaikka\".\"Kohde SmarPost\", \"lahto-ja_saapumispaikka\".\"lahtoaika\",\"lahto-ja_saapumispaikka\".\"saapumisaika\", \"lahto-ja_saapumispaikka\".\"matkan_pituus\"\n"
                + " FROM lahettajat_ja_vastaanottajat INNER JOIN \"lahto-ja_saapumispaikka\" ON lahettajat_ja_vastaanottajat.Paketti = \"lahto-ja_saapumispaikka\".Paketti";
        String sql20 = "CREATE VIEW IF NOT EXISTS \"lahto-ja_saapumisgeografinensijainti\"(pktid, lahtolat, lahtolon, kohdelat, kohdelon)\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"pktid\",\n"
                + "	(lahto_sp.latitude) AS \"lahtolat\",\n"
                + "	(lahto_sp.longitude) AS \"lahtolon\",\n"
                + "	(kohde_sp.latitude) AS \"kohdelat\",\n"
                + "	(kohde_sp.longitude) AS \"kohdelon\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN SmartPost AS lahto_sp\n"
                + "ON (lahto_sp.spid = lahetystiedot.lahtospid)\n"
                + "INNER JOIN SmartPost AS kohde_sp\n"
                + "ON (kohde_sp.spid = lahetystiedot.kohdespid)";

        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql1);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql2);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql3);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql4);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql5);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql6);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql8);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql9);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql14);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql15);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql16);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql17);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql18);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql19);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql20);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();

    }

    //remove all the possibly existing tables needed for this program's DB and create them from a scratch
    public void createDB() {
        this.connect();
        
        String sqlInit = "DROP TABLE IF EXISTS SmartPost";
        String sqlInit1 = "DROP TABLE IF EXISTS esine";
        String sqlInit2 = "DROP TABLE IF EXISTS luokka";
        String sqlInit3 = "DROP TABLE IF EXISTS paketti";
        String sqlInit4 = "DROP TABLE IF EXISTS osoite";
        String sqlInit5 = "DROP TABLE IF EXISTS hlotiedot";
        String sqlInit6 = "DROP TABLE IF EXISTS lahetystiedot";
        String sqlInit7 = "DROP TABLE IF EXISTS varasto";
        String sqlInit8 = "DROP TABLE IF EXISTS sijaitsee";
        String sqlInit9 = "DROP TABLE IF EXISTS sisaltaa";
        String sqlInit10 = "DROP VIEW IF EXISTS \"taydelliset_lahetystiedot\"";
        String sqlInit11 = "DROP VIEW IF EXISTS \"lahto-ja_saapumispaikka\"";
        String sqlInit12 = "DROP VIEW IF EXISTS \"lahettajat_ja_vastaanottajat\"";
        String sqlInit13 = "DROP VIEW IF EXISTS \"lahto-ja_saapumisgeografinensijainti\"";

        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sqlInit);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit1);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit2);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit3);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit4);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit5);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit6);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit7);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit8);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit9);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit10);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit11);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit12);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sqlInit13);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sql = "PRAGMA FOREIGN_KEYS = ON;\n";
        String sql1 = "CREATE TABLE SmartPost (\n"
                + "SPid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "\n"
                + "nimi VARCHAR(32),\n"
                + "aukioloajat VARCHAR(40) NOT NULL,\n"
                + "\n"
                + "longitude VARCHAR(20) NOT NULL,\n"
                + "latitude VARCHAR(20) NOT NULL,\n"
                + "\n"
                + "\n"
                + "osoite VARCHAR(30),\n"
                + "postinro VARCHAR(30), \n"
                + "kaupunki VARCHAR(30)\n"
                + ")";
        String sql2 = "CREATE TABLE esine (\n"
                + "esineid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "nimi VARCHAR(64) NOT NULL,\n"
                + "paino DOUBLE NOT NULL,\n"
                + "koko VARCHAR(12),\n"
                + "voikoHajota BOOLEAN NOT NULL,\n"
                + "\n"
                + "UNIQUE(nimi, paino, koko)\n"
                + ")";
        String sql3 = "CREATE TABLE paketti (\n"
                + "pktid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "luokkanro INTEGER,\n"
                + "FOREIGN KEY(luokkanro) REFERENCES luokka(luokkanro)\n"
                + ")";
        String sql4 = "CREATE TABLE luokka (\n"
                + "luokkanro INTEGER UNIQUE NOT NULL PRIMARY KEY,\n"
                + "kokoraja DOUBLE NOT NULL,\n"
                + "painoraja DOUBLE NOT NULL,\n"
                + "\n"
                + "CHECK (luokkanro IN ('1', '2', '3'))\n"
                + ")";
        String sql5 = "CREATE TABLE lahetystiedot (\n"
                + "pktid INTEGER PRIMARY KEY,\n"
                + "lahtospid INTEGER,\n"
                + "kohdespid INTEGER,\n"
                + "lahettajaid INTEGER,\n"
                + "vastaanottajaid INTEGER,\n"
                + "lahtoaika DATE,\n"
                + "saapumisaika DATE,\n"
                + "matkanpituus DOUBLE,\n"
                + "lahetetty BOOLEAN DEFAULT 0, \n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(lahettajaid) REFERENCES hlotiedot(hloid),\n"
                + "FOREIGN KEY(vastaanottajaid) REFERENCES hlotiedot(hloid)"
                + ")";
        String sql6 = "CREATE TABLE hlotiedot (\n"
                + "hloid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "etunimi VARCHAR(32) NOT NULL,\n"
                + "sukunimi VARCHAR(32) NOT NULL,\n"
                + "puhnro VARCHAR(13) NOT NULL,\n"
                + "UNIQUE (etunimi, sukunimi, puhnro)"
                + ")";
        String sql8 = "CREATE TABLE sijaitsee (\n"
                + "pktid,\n"
                + "SPid,\n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(SPid) REFERENCES SmartPost(SPid)\n"
                + ");";
        String sql9 = "CREATE TABLE sisaltaa (\n"
                + "pktid,\n"
                + "esineid,\n"
                + "FOREIGN KEY(pktid) REFERENCES paketti(pktid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY(esineid) REFERENCES esine(esineid)\n"
                + ");";
        String sql10 = "INSERT OR IGNORE INTO esine (nimi, paino, koko, voikoHajota) VALUES (\"puuhöylä\", 1, \"0.0075\", false)";
        String sql11 = "INSERT OR IGNORE INTO esine (nimi, paino, koko, voikoHajota) VALUES (\"kirja\", 0.2, \"0.006\", false)";
        String sql12 = "INSERT OR IGNORE INTO esine (nimi, paino, koko, voikoHajota) VALUES (\"kahvikuppi\", 0.2, \"0.00768\", true)";
        String sql13 = "INSERT OR IGNORE INTO esine (nimi, paino, koko, voikoHajota) VALUES (\"Ming-vaasi\", 10, \"0.2\", true)";
        String sql14 = "INSERT OR IGNORE INTO luokka VALUES (1, 0.027, 5)";
        String sql15 = "INSERT OR IGNORE INTO luokka VALUES (2, 0.008, 5)";
        String sql16 = "INSERT OR IGNORE INTO luokka VALUES (3, 1.0, 35)";
        String sql17 = "CREATE VIEW IF NOT EXISTS \"lahto-ja_saapumispaikka\"(\"Paketti\", \"Lahto SmarPost\", \"Kohde SmarPost\", lahtoaika, saapumisaika, matkan_pituus)\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"Paketti\",\n"
                + "	(lahto_sp.nimi || \" \" || lahto_sp.osoite || \" \" || lahto_sp.postinro || \" \" || lahto_sp.kaupunki) AS \"Lahto SmarPost\",\n"
                + "	(kohde_sp.nimi || \" \" || kohde_sp.osoite || \" \" || kohde_sp.postinro || \" \" || kohde_sp.kaupunki) AS \"Kohde SmarPost\",\n"
                + "	lahetystiedot.lahtoaika AS \"lahtoaika\", lahetystiedot.saapumisaika AS \"saapumisaika\", lahetystiedot.matkanpituus AS \"matkan_pituus\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN SmartPost AS lahto_sp\n"
                + "ON (lahto_sp.spid = lahetystiedot.lahtospid)\n"
                + "INNER JOIN SmartPost AS kohde_sp\n"
                + "ON (kohde_sp.spid = lahetystiedot.kohdespid)";
        String sql18 = "CREATE VIEW IF NOT EXISTS lahettajat_ja_vastaanottajat (\"Paketti\", \"Lahettajan nimi ja puhelinnumero\", \"Vastaanottajan nimi ja puhelinnumero\")\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"Paketti\",\n"
                + "	(lahettaja.etunimi || \" \" || lahettaja.sukunimi || \", \" || lahettaja.puhnro) AS \"Lahettajan nimi ja puhelinnumero\",\n"
                + "	(vastaanottaja.etunimi || \" \" || vastaanottaja.sukunimi || \", \" || vastaanottaja.puhnro) AS \"Vastaanottajan nimi ja puhelinnumero\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN hlotiedot AS lahettaja\n"
                + "ON (lahettaja.hloid = lahetystiedot.lahettajaid)\n"
                + "INNER JOIN hlotiedot AS vastaanottaja\n"
                + "ON (vastaanottaja.hloid = lahetystiedot.vastaanottajaid)";
        String sql19 = "CREATE VIEW IF NOT EXISTS\"taydelliset_lahetystiedot\"(Paketti, \"Lahettajan nimi ja puhelinnumero\", \"Vastaanottajan nimi ja puhelinnumero\", \"Lahto SmarPost\", \"Kohde SmarPost\", \"lahtoaika\", \"saapumisaika\", \"matkan_pituus\")\n"
                + "AS\n"
                + "SELECT lahettajat_ja_vastaanottajat.Paketti, lahettajat_ja_vastaanottajat.\"Lahettajan nimi ja puhelinnumero\", lahettajat_ja_vastaanottajat.\"Vastaanottajan nimi ja puhelinnumero\",\n"
                + "	\"lahto-ja_saapumispaikka\".\"Lahto SmarPost\" , \"lahto-ja_saapumispaikka\".\"Kohde SmarPost\", \"lahto-ja_saapumispaikka\".\"lahtoaika\",\"lahto-ja_saapumispaikka\".\"saapumisaika\", \"lahto-ja_saapumispaikka\".\"matkan_pituus\"\n"
                + " FROM lahettajat_ja_vastaanottajat INNER JOIN \"lahto-ja_saapumispaikka\" ON lahettajat_ja_vastaanottajat.Paketti = \"lahto-ja_saapumispaikka\".Paketti";
        String sql20 = "CREATE VIEW IF NOT EXISTS \"lahto-ja_saapumisgeografinensijainti\"(pktid, lahtolat, lahtolon, kohdelat, kohdelon)\n"
                + "AS\n"
                + "SELECT lahetystiedot.pktid AS \"pktid\",\n"
                + "	(lahto_sp.latitude) AS \"lahtolat\",\n"
                + "	(lahto_sp.longitude) AS \"lahtolon\",\n"
                + "	(kohde_sp.latitude) AS \"kohdelat\",\n"
                + "	(kohde_sp.longitude) AS \"kohdelon\"\n"
                + "FROM lahetystiedot\n"
                + "INNER JOIN SmartPost AS lahto_sp\n"
                + "ON (lahto_sp.spid = lahetystiedot.lahtospid)\n"
                + "INNER JOIN SmartPost AS kohde_sp\n"
                + "ON (kohde_sp.spid = lahetystiedot.kohdespid)";

        pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql1);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql2);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql3);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql4);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql5);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql6);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql8);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql9);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql10);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql11);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql12);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql13);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql14);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql15);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql16);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql17);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql18);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql19);
            pstmt.executeUpdate();
            pstmt = this.conn.prepareStatement(sql20);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.closeConnection();

    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
