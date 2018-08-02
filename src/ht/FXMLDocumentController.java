/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import ht.DBConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author mikko
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label label;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private Button addButton;
    @FXML
    private Button newPackageButton;
    @FXML
    private Button deleteRoutesButton;
    @FXML
    private Button sendPackageButton;
    @FXML
    private ComboBox<Integer> choosePackageComboBox;
    @FXML
    private WebView webView;

    @FXML
    private Button deletePackageButton;
    
    private ArrayList<String> routes;
    @FXML
    private Tab smartPostTab;
    @FXML
    private Tab logTab;
    @FXML
    private ListView<String> packageInfoListView;
    @FXML
    private AnchorPane ap;
    @FXML
    private GridPane gridPane;
    @FXML
    private GridPane gridPaneWButtons;
    @FXML
    private GridPane gridPaneWComboBoxAndButton;
    @FXML
    private Tab personTab;
    @FXML
    private ListView<String> personInfoListView;
    
    private ArrayList<String> alreadyAddedcitysAutomats = new ArrayList<>();
    
    private DBConnection dbc = DBConnection.getInstance();
    
    private ArrayList<SmartPost> sps;
    private ArrayList<PostPackage> pps;
    private ArrayList<Thing> ts;
    private ArrayList<Double> ci; //postal class info
    boolean logTabTrigger = true;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        addButton.setMinSize(70, 30);
        deleteRoutesButton.setMinSize(175, 30);
        deletePackageButton.setMinSize(140, 30);
        newPackageButton.setMinSize(140, 30);
        newPackageButton.setMinSize(140,30);
        cityComboBox.setMaxSize(175, 30);
        choosePackageComboBox.setMaxSize(140, 30);
        
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(100.0);
        gridPaneWButtons.getColumnConstraints().add(cc);
        label.setWrapText(true);
        
        webView.getEngine().load(getClass().getResource("index.html").toExternalForm());
        
        //empty the old DB and create a new one, trigger is set by user in the initializer window
        if (dbc.getTrigger() == 0) {
            dbc.createDB();
            dbc.connect();
            try {
            //READ XML
            dbc.XMLParser();
            } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dbc.setTrigger(1);
        
        //add all the cities to the combobox
        sps = dbc.getSps();
        pps = dbc.getPostPackages();
        ts = dbc.getTs();
        ci = dbc.getClassInfo();
        
        for (SmartPost sp: sps) {
            if (!cityComboBox.getItems().contains(sp.getCity())) {
                cityComboBox.getItems().add(sp.getCity());
            }
        }

        dbc.closeConnection();
    }

    //add chose city's SP automat's to the map
    @FXML
    private void addButtonAction(ActionEvent event) {
        if ((cityComboBox.getSelectionModel().getSelectedItem() != null) && (!alreadyAddedcitysAutomats.contains(cityComboBox.getSelectionModel().getSelectedItem()))) {
            for (SmartPost sp: sps) {
                if (sp.getCity().equals(cityComboBox.getSelectionModel().getSelectedItem())) {
                    String location = "'" + sp.getAddress() + ", " + sp.getZip() + " " + sp.getCity() + "'";
                    String info = "'" + sp.getName() + sp.getAvailability() + "'";
                    String color = "'red'";
                    webView.getEngine().executeScript("document.goToLocation(" + location + "," + info + ", " + color + ")");
                }
            }
            label.setText("Lisätään kaupungin " + cityComboBox.getSelectionModel().getSelectedItem() + " SmartPost-automaatit kartalle.");
            alreadyAddedcitysAutomats.add(cityComboBox.getSelectionModel().getSelectedItem());
        } else if (cityComboBox.getSelectionModel().getSelectedItem() == null) {
            label.setText("Valitse kaupunki.");
        } else {
            label.setText("Kaupungin " + cityComboBox.getSelectionModel().getSelectedItem() + " SmartPost-automaatit on jo lisätty kartalle.");
        }
    }

    //open new package -window
    @FXML
    private void newPackageButtonAction(ActionEvent event) {
        label.setText("");
        Stage packageStage = new Stage();
        try {
            Parent page = FXMLLoader.load(getClass().getResource("addPackage.fxml"));
            Scene scene = new Scene(page);

            packageStage.setScene(scene);
            packageStage.setResizable(false);
            packageStage.show();

        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //delete routes from the map
    @FXML
    private void deleteRoutesButtonAction(ActionEvent event) {
        webView.getEngine().executeScript("document.deletePaths()");
        label.setText("Poistetaan löydetyt reitit kartalta.");
    }

    //send package
    @FXML
    private void sendPackageButtonAction(ActionEvent event) {
        if (choosePackageComboBox.getValue() != null) {
            //temporary string to draw the route on to the map
            String temp = "";
            int packageId = choosePackageComboBox.getSelectionModel().getSelectedItem();
            for (PostPackage pp: dbc.getPostPackages()) {
                if (pp.getId() == choosePackageComboBox.getSelectionModel().getSelectedItem()) {
                    //arraylist for the from and to SP's geografical coordinates
                    String color = "";
                    String cl = Integer.toString(dbc.getPackagesClass(packageId));
                    
                    String fromLat = null;
                    String fromLon = null;
                    String toLat = null;
                    String toLon = null;
                    ArrayList<String> route = new ArrayList<>();
                    for (SmartPost sp: sps) {
                        if (sp.getSpid() == pp.getFromSP()) {
                            fromLat = sp.getLat();
                            fromLon = sp.getLon();
                        }
                        if (sp.getSpid() == pp.getToSP()) {
                            toLat = sp.getLat();
                            toLon = sp.getLon();
                        }
                    }
                    route.add(fromLat);
                    route.add(fromLon);
                    route.add(toLat);
                    route.add(toLon);

                    //color is picked based on the packages class
                    switch (pp.getPostalClass()) {
                        case 1:
                            color = "blue";
                            break;
                        case 2:
                            color = "green";
                            break;
                        case 3:
                            color = "red";
                            break;
                        default:
                            break;
                    }

                    temp = "document.createPath(" + route + ",'" + color + "'," + cl + ")";
                    
                    //if the routes length is over 150 and class is set to first, the package class is changed to third
                    if ((getRouteLength(packageId) > 150) && (pp.getPostalClass() == 1)){
                        dbc.setPackagesClass(packageId, 3);
                        pp.setClass(3);
                        cl = Integer.toString(3);
                        color = "red";
                        temp = "document.createPath(" + route + ",'" + color + "'," + cl + ")";
                        label.setText("Luokka muutettiin kolmanteen, koska etäisyysrajoituksia ei noudatettu. Lähetettiin paketti nro " + packageId);
                    } else {
                        label.setText("Lähetettiin paketti nro " + packageId);
                    }
                    
                    dbc.setDates(getRouteLength(packageId), packageId, pp.getPostalClass());
                    
                    
                    //draw the route
                    webView.getEngine().executeScript(temp);
                    //draw the from and to SP's on the map
                    String start = dbc.getSmartPostsDrawOnID(dbc.getSmartPostIdonPackageId(packageId));
                    String end = dbc.getSmartPostsDrawOnID(dbc.getToSmartPostIdonPackageId(packageId));
                    
                    dbc.setSent(packageId);
                    dbc.setLocation(packageId, -1);
                    
                    webView.getEngine().executeScript(start);
                    webView.getEngine().executeScript(end);
                    choosePackageComboBox.getItems().clear();
                    
                    dbc.setTrigger(1);
                    logTabTrigger = true;
                    
                    pp.setSent();
                    break;
                }
                
            }
            
            Iterator<PostPackage> it = pps.iterator();
            while (it.hasNext()) {
                if (it.next().getId() == packageId) {
                    it.remove();
                    break;
                }
            }
        } else {
            label.setText("Valitse lähetettävä paketti.");
        }
    }


    @FXML
    private void choosePackageComboBoxClicked(MouseEvent event) {
        if (dbc.getTrigger() == 1) {
            //empty the packagecombobox and get the packages currently in the DB
            choosePackageComboBox.getItems().clear();
            for (PostPackage pp: dbc.getPostPackages()) {
                if (!pp.isSent()) {
                    choosePackageComboBox.getItems().add(pp.getId());
                }
            }
            dbc.setTrigger(0);
        }
    }

    //delete package
    @FXML
    private void deletePackageButtonAction(ActionEvent event) {
        if (choosePackageComboBox.getValue() != null) {
            dbc.deletePackageFromDB((choosePackageComboBox.getSelectionModel().getSelectedItem()));
            label.setText("Poistettiin paketti nro " + choosePackageComboBox.getSelectionModel().getSelectedItem() + " tietokannasta.");
            int packageId = choosePackageComboBox.getSelectionModel().getSelectedItem();
            Iterator<PostPackage> it = pps.iterator();
            while (it.hasNext()) {
                if (it.next().getId() == packageId) {
                    it.remove();
                    break;
                }
            }
            choosePackageComboBox.getItems().clear();
            dbc.setTrigger(1);
        } else {
            label.setText("Valitse poistettava paketti.");
        }
    }

    @FXML
    private void smartPostTabSelected(Event event) {
        label.setText("");
    }

    @FXML
    private void logTabSelected(Event event) {
        if (logTabTrigger) {
            for (PostPackage pp: dbc.getPostPackages()) {
                getRouteLength(pp.getId());
            }
            packageInfoListView.getItems().clear();
            for (String s: dbc.getPackageIds()) {
                String thingsInPackage = dbc.getCompletePackageInfo(Integer.parseInt(s)) + "\nSisältö: ";
                for (String s2: dbc.getThingsInPackage(Integer.parseInt(s))) {
                    thingsInPackage += "\n" + s2;

                    //if the thing in the package is breakable and the class is third, the thing breaks
                    if (dbc.isBreakable(dbc.getThingId(s2)) 
                            && ((dbc.getPackagesClass(Integer.parseInt(s)) == 3)
                            && dbc.isSent(Integer.parseInt(s)))){
                        thingsInPackage += ", HAJOSI MATKALLA";
                    }
                }
                packageInfoListView.getItems().add(thingsInPackage);
            }
            if (packageInfoListView.getItems().isEmpty()) {
                packageInfoListView.getItems().add("");
            }
            logTabTrigger = false;
        }
        
    }

    //get route length the package
    public double getRouteLength(int id) {
        double length = 0;
        for (PostPackage pp: dbc.getPostPackages()) {
            if ((pp.getId() == id) && (pp.getLength() == 0) & (!pp.isSent())) {
                //temporary string get the route length
               String temp;
               //arraylist for the from and to SP's geografical coordinates 
               String fromLat = null;
               String fromLon = null;
               String toLat = null;
               String toLon = null;
               ArrayList<String> route = new ArrayList<>();
               for (SmartPost sp: sps) {
                   if (sp.getSpid() == pp.getFromSP()) {
                       fromLat = sp.getLat();
                       fromLon = sp.getLon();
                   }
                   if (sp.getSpid() == pp.getToSP()) {
                       toLat = sp.getLat();
                       toLon = sp.getLon();
                   }
               }
               route.add(fromLat);
               route.add(fromLon);
               route.add(toLat);
               route.add(toLon);

               String color = "";
               String cl = Integer.toString(pp.getPostalClass());

               //color is picked based on the packages class
               switch (pp.getPostalClass()) {
                   case 1:
                       color = "blue";
                       break;
                   case 2:
                       color = "green";
                       break;
                   case 3:
                       color = "red"; 
                       break;
                   default:
                       break;
               }

               temp = "document.getLength(" + route + ",'" + color + "'," + cl + ")";
               length = (double) webView.getEngine().executeScript(temp);
               pp.setLength(length);
               dbc.setLength(length, pp.getId());
            }
        }
        return length;
    }

    //person tab
    @FXML
    private void personTabSelected(Event event) {
        if (dbc.getPersonTabTrigger() == 1) {
            //empty the person info -listview and get currently up to date info from the DB
            personInfoListView.getItems().clear();
            for (String s: dbc.getPersons()) {
                personInfoListView.getItems().add(s);
            }
            if (personInfoListView.getItems().isEmpty()) {
                personInfoListView.getItems().add("");
            }
            dbc.setPersonTabTrigger(0);
        }
    }
}
