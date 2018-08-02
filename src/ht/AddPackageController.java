/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import ht.DBConnection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mikko
 */
public class AddPackageController implements Initializable {

    @FXML
    private ComboBox<String> chooseObjectComboBox;
    @FXML
    private Button addObjectToPackage;
    @FXML
    private TextField objectNameField;
    @FXML
    private TextField objectSizeField;
    @FXML
    private TextField objectWeightField;
    private CheckBox breakableChoiceBox;
    @FXML
    private Button makeObjectButton;
    @FXML
    private ComboBox<String> fromCityComboBox;
    @FXML
    private ComboBox<String> fromAutomatComboBox;
    @FXML
    private ComboBox<String> toCityComboBox;
    @FXML
    private ComboBox<String> toAutomatComboBox;
    @FXML
    private Button cancelButton;
    @FXML
    
    private Button makePackageButton;
    private ToggleGroup tg;
    boolean thingAddedToPackage = false;
    
    @FXML
    private RadioButton breakableRadioButton;
    @FXML
    private RadioButton firstClassRadioButton;
    @FXML
    private RadioButton thirdClassRadioButton;
    @FXML
    private RadioButton secondClassRadioButton;
    @FXML
    private Button deleteThingPButton;
    @FXML
    private Button deleteThingDBButton;
    @FXML
    private ListView<String> listView;
    @FXML
    private Label infoLabel;
    @FXML
    private Label notificationLabel;
    @FXML
    private TextField fromFirstName;
    @FXML
    private TextField toPhoneNumber;
    @FXML
    private TextField toLastName;
    @FXML
    private TextField toFirstName;
    @FXML
    private TextField fromPhoneNumber;
    @FXML
    private TextField fromLastName;
    @FXML
    private Label firstClassLabel;
    @FXML
    private Label secondClassLabel;
    @FXML
    private Label thirdClassLabel;
    
    private DBConnection dbc = DBConnection.getInstance();
    
    private ArrayList<SmartPost> sps;
    private ArrayList<PostPackage> pps;
    private ArrayList<Thing> ts;
    private ArrayList<Double> ci; //postal class info
    private PostPackage p = null;
    private int packageId = 0;
    private int thingId = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ci = dbc.getClassInfo();
        tg = new ToggleGroup();
        firstClassRadioButton.setToggleGroup(tg);
        firstClassRadioButton.setUserData(1);
        secondClassRadioButton.setToggleGroup(tg);
        secondClassRadioButton.setUserData(2);
        thirdClassRadioButton.setToggleGroup(tg);
        thirdClassRadioButton.setUserData(3);
        
        infoLabel.setText("Paino:\nKoko:\nSisältö:");
        firstClassLabel.setText("Max. paino: " + ci.get(1) +"kg\nMax. koko: " + ci.get(2) +"m^3\nMax. etäisyys: 150 km");
        secondClassLabel.setText("Max. paino: " + ci.get(4) +"kg\nMax. koko: " + ci.get(5) +"m^3");
        thirdClassLabel.setText("Max. paino: " + ci.get(7) +"kg\nMax. koko: " + ci.get(8) +"m^3");
        notificationLabel.setWrapText(true);
        listView.getItems().add("");
        
        fromFirstName.setMaxSize(200, 30);
        fromLastName.setMaxSize(200, 30);
        fromPhoneNumber.setMaxSize(200, 30);
        toFirstName.setMaxSize(200, 30);
        toLastName.setMaxSize(200, 30);
        toPhoneNumber.setMaxSize(200, 30);
        fromCityComboBox.setMaxSize(200, 30);
        fromAutomatComboBox.setMaxSize(200, 30);
        toCityComboBox.setMaxSize(200, 30);
        toAutomatComboBox.setMaxSize(200, 30);
        toFirstName.setMinSize(200, 30);
        toLastName.setMinSize(200, 30);
        toPhoneNumber.setMinSize(200, 30);
        fromCityComboBox.setMinSize(200, 30);
        fromAutomatComboBox.setMinSize(200, 30);
        toCityComboBox.setMinSize(200, 30);
        toAutomatComboBox.setMinSize(200, 30);
        chooseObjectComboBox.setMinSize(180, 30);
        addObjectToPackage.setMinSize(120, 30);
        objectNameField.setMinSize(180, 30);
        objectWeightField.setMinSize(180, 30);
        objectSizeField.setMinSize(180, 30);
        
        deleteThingPButton.setMinSize(120, 30);
        deleteThingDBButton.setMaxSize(120, 30);
        cancelButton.setMinSize(120, 30);
        makePackageButton.setMinSize(120, 30);
        
        listView.setMinWidth(426);
        
        sps = dbc.getSps();
        pps = dbc.getPostPackages();
        ts = dbc.getTs();
        
        //add all cities to the combobox
        for (SmartPost sp: sps) {
            if (!toCityComboBox.getItems().contains(sp.getCity())) {
                toCityComboBox.getItems().add(sp.getCity());
            }
            if (!fromCityComboBox.getItems().contains(sp.getCity())) {
                fromCityComboBox.getItems().add(sp.getCity());
            }
        }
        
        for (PostPackage pp: pps) {
            if ((pp.getId()) >= packageId) {
                packageId = pp.getId();
            }
        }
        packageId++;
        
        for (Thing t: ts) {
            chooseObjectComboBox.getItems().add(t.getName());
        }
        dbc.setThingTrigger(0);
    }
    
    
    

    @FXML
    private void addObjectToPackageAction(ActionEvent event) {
                if (chooseObjectComboBox.getValue() != null) {
                    //if nothing has been added to package, create a new one
                    if (!thingAddedToPackage) {

                        // create package
                        dbc.insertPackage();

                        //add thing to package
                        dbc.insertThingToPackage(dbc.getThingId(chooseObjectComboBox.getSelectionModel().getSelectedItem()), dbc.getPackageId());
                        thingAddedToPackage = true;
                        
                        
                        p = new PostPackage(packageId);
                        for (Thing t: ts) {
                            if (t.getName().equals(chooseObjectComboBox.getSelectionModel().getSelectedItem())) {
                                p.insertThingToPackage(t);
                            }
                        }
                     //else add thing to already made package  
                    } else if ((thingAddedToPackage)) { 
                        dbc.insertThingToPackage(dbc.getThingId(chooseObjectComboBox.getSelectionModel().getSelectedItem()), dbc.getPackageId());
                        for (Thing t: ts) {
                            if (t.getName().equals(chooseObjectComboBox.getSelectionModel().getSelectedItem())) {
                                p.insertThingToPackage(t);
                            }
                        }
                    }

                    listView.getItems().clear();
                    infoLabel.setText("Paino: " + round(p.getWeight(),3) +
                                "\nKoko: " + round(p.getSize(), 5) + 
                                "\nSisältö:");
                    for (Thing t: p.getThingsInPackage()) {
                        listView.getItems().add(t.getName());
                    }
                }

    }

    @FXML
    private void makeObjectButtonAction(ActionEvent event) {
        //pattern for allowed size
        String pattern = "\\d*";
        if (objectNameField.getText().isEmpty()) {
            notificationLabel.setText("Aseta nimi.");
        }
        else if (objectSizeField.getText().isEmpty()) {
            notificationLabel.setText("Aseta koko.");
        }
        else if (!Pattern.matches(pattern, objectSizeField.getText())) {
            notificationLabel.setText("Virheellinen koko.");
        }
        else if (objectWeightField.getText().isEmpty()) {
            notificationLabel.setText("Aseta paino.");
        }
        else {
            for (Thing t: ts) {
                if ((t.getThingId()) >= thingId) {
                    thingId = t.getThingId();
            }
            }
            thingId++;
            System.out.println(thingId);
            }
            try {
                if (Double.parseDouble(objectWeightField.getText()) > 0) {
                    Thing t = new Thing(thingId, objectNameField.getText(), Double.parseDouble(objectSizeField.getText()), Double.parseDouble(objectWeightField.getText()), breakableRadioButton.isSelected());
                    dbc.insertThing(objectNameField.getText(), objectSizeField.getText(), Double.parseDouble(objectWeightField.getText()), breakableRadioButton.isSelected());
                    notificationLabel.setText("Lisättiin esine tietokantaan.");
                    dbc.setThingTrigger(1);
                    ts.add(t);
                } else {
                    notificationLabel.setText("Virheellinen paino.");
                }
            }
            //erronous value for weight
            catch (NumberFormatException e) {
                notificationLabel.setText("Virheellinen paino.");
            }
        
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        //if something has been added to package and thus package has been added to DB, remove the package from the DB
        if (thingAddedToPackage == true) {
            dbc.deletePackageFromDB(dbc.getPackageId());
        }
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void makePackageButtonAction(ActionEvent event) {
        double weightLimit = 0;
        double sizeLimit = 0;
        String patternNumber1 = "\\d+";
        String patternNumber2 = "[+]{1}\\d+";
        String patternName = "[a-zA-Z]+";
        
        if ((fromAutomatComboBox.getValue() == null) || (toAutomatComboBox.getValue() == null)) {
            notificationLabel.setText("Valitse automaatit.");
        }
        else if ((fromFirstName.getText().isEmpty()) || (fromLastName.getText().isEmpty()) || (fromPhoneNumber.getText().isEmpty())) {
            notificationLabel.setText("Anna lähettäjän tiedot.");
        }
        else if ((toFirstName.getText().isEmpty()) || (toLastName.getText().isEmpty()) || (toPhoneNumber.getText().isEmpty())) {
            notificationLabel.setText("Anna vastaanottajan tiedot.");
        }
        else if (!Pattern.matches(patternName, fromFirstName.getText()) || !Pattern.matches(patternName, fromLastName.getText())) {
            notificationLabel.setText("Virheellinen lähettäjän nimi.");
        }
        else if (!Pattern.matches(patternName, toFirstName.getText()) || !Pattern.matches(patternName, toLastName.getText())) {
            notificationLabel.setText("Virheellinen vastaanottajan nimi.");
        }
        else if (!Pattern.matches(patternNumber1, fromPhoneNumber.getText()) && !Pattern.matches(patternNumber2, fromPhoneNumber.getText())) {
            notificationLabel.setText("Virheellinen lähettäjän puhelinnumero.");
        }
        else if (!Pattern.matches(patternNumber1, toPhoneNumber.getText()) && !Pattern.matches(patternNumber2, toPhoneNumber.getText())) {
            notificationLabel.setText("Virheellinen vastaanottajan puhelinnumero.");
        } else if (tg.getSelectedToggle() == null) {
                notificationLabel.setText("Valitse luokka.");
        }
        else if ((thingAddedToPackage) && (!p.getThingsInPackage().isEmpty())) {
            dbc.setPackagesClass(packageId, (Integer) tg.getSelectedToggle().getUserData());
            
            switch ((Integer) tg.getSelectedToggle().getUserData()) {
                case 1:
                    weightLimit = ci.get(1);
                    sizeLimit = ci.get(2);
                    break;
                case 2:
                    weightLimit = ci.get(4);
                    sizeLimit = ci.get(5);
                    break;
                case 3:
                    weightLimit = ci.get(7);
                    sizeLimit = ci.get(8);
                    break;
            }
                if ((p.getWeight()) < weightLimit) {
                    int spId = dbc.getSmartPostId(fromAutomatComboBox.getSelectionModel().getSelectedItem());

                    //set the starting location for the package
                    dbc.insertLocation(packageId, spId);

                    dbc.insertPackageInfo(packageId, dbc.getSmartPostId(fromAutomatComboBox.getSelectionModel().getSelectedItem()), dbc.getSmartPostId(toAutomatComboBox.getSelectionModel().getSelectedItem()));
                    
                    //Add sender's info to db
                    dbc.insertPersonInfo(fromFirstName.getText(), fromLastName.getText(), fromPhoneNumber.getText());
                    dbc.setSenderID(dbc.getPersonsID(fromFirstName.getText(), fromLastName.getText(), fromPhoneNumber.getText()), packageId);
                    
                    
                    //Add receiver's info to db
                    dbc.insertPersonInfo(toFirstName.getText(), toLastName.getText(), toPhoneNumber.getText());
                    dbc.setReceiverID(dbc.getPersonsID(toFirstName.getText(), toLastName.getText(), toPhoneNumber.getText()), packageId);
                    
                    //Add postpackage to SP
                    for (SmartPost sp: sps) {
                        if (sp.getName().equals(fromAutomatComboBox.getSelectionModel().getSelectedItem())) {
                            p.setFromSP(sp.getSpid());
                        }
                        if (sp.getName().equals(toAutomatComboBox.getSelectionModel().getSelectedItem())) {
                            p.setToSP(sp.getSpid());
                        }
                    }
                    pps.add(p);
                    dbc.setTrigger(1);
                    dbc.setPersonTabTrigger(1);
                    dbc.setPackageInfoTabTrigger(1);
                    Stage stage = (Stage) makePackageButton.getScene().getWindow();
                    stage.close();
                    
                    
                    
                //size is more than allowed
                } else if ((p.getSize()) > sizeLimit){
                    notificationLabel.setText("Tarkista kokoraja.");
                }
                //weight is more than allowed
                else {
                    notificationLabel.setText("Tarkista painoraja.");
                }
            
        //nothing has been added to the package
        } else {
             notificationLabel.setText("Paketti on tyhjä.");
        }
    }

    @FXML
    private void breakableRadioButtonAction(ActionEvent event) {
    }

    @FXML
    private void firstClassRadioButtonAction(ActionEvent event) {
    }

    @FXML
    private void thirdClassRadioButton(ActionEvent event) {
    }

    @FXML
    private void secondClassRadioButtonAction(ActionEvent event) {
    }

    @FXML
    private void fromAutomatComboBoxClicked(MouseEvent event) {
        fromAutomatComboBox.getItems().clear();
        //get the smartposts from the particular city
        for (SmartPost sp: sps) {
            if (sp.getCity().equals(fromCityComboBox.getSelectionModel().getSelectedItem())) {
                fromAutomatComboBox.getItems().add(sp.getName());
            }
        }
    }

    @FXML
    private void toAutomatComboBoxClicked(MouseEvent event) {
        toAutomatComboBox.getItems().clear();
        //get the smartposts from the particular city and add them to the combobox
        for (SmartPost sp: sps) {
            if (sp.getCity().equals(toCityComboBox.getSelectionModel().getSelectedItem())) {
                toAutomatComboBox.getItems().add(sp.getName());
            }
        }
    }

    @FXML
    private void chooseObjectComboBoxClicked(MouseEvent event) {
        if (dbc.getThingTrigger() == 1) {
            chooseObjectComboBox.getItems().clear();
            for (Thing t: ts) {
                chooseObjectComboBox.getItems().add(t.getName());
            }
            dbc.setThingTrigger(0);
        }
    }

    @FXML
    private void deleteThingPButtonAction(ActionEvent event) {
        if ((chooseObjectComboBox.getValue() != null) && (thingAddedToPackage)){
            for (Thing t: ts) {
                if (t.getName().equals(chooseObjectComboBox.getSelectionModel().getSelectedItem())) {
                    thingId = t.getThingId();
                    System.out.println("poistetaan paketista: " + thingId);
                }
            }
            dbc.deleteThingFromPackage(packageId, thingId);

            Iterator<Thing> it = p.getThingsInPackage().iterator();
            while (it.hasNext()) {
                if (it.next().getThingId() == thingId) {
                    it.remove();
                    break;
                }
            }
                
            //update the package
            listView.getItems().clear();
            infoLabel.setText("Paino: " + round(p.getWeight(),3) +
                                    "\nKoko: " + round(p.getSize(),5) + 
                                    "\nSisältö:");
            for (Thing t: p.getThingsInPackage()) {
                listView.getItems().add(t.getName());
            }
            if (listView.getItems().isEmpty()) {
                listView.getItems().add("");
            }
        }
    }

    @FXML
    private void deleteThingDBButtonAction(ActionEvent event) {
        if (chooseObjectComboBox.getValue() != null) {
            dbc.deleteThingFromDB(chooseObjectComboBox.getSelectionModel().getSelectedItem());
            
            //also delete the deleted thing from the package
            for (Thing t: ts) {
                if (t.getName().equals(chooseObjectComboBox.getSelectionModel().getSelectedItem())) {
                    thingId = t.getThingId();
                }
            }
            System.out.println("poistettavan id: " + thingId);

            dbc.deleteThingFromPackage(packageId, thingId);

            Iterator<Thing> itts = ts.iterator();
            while (itts.hasNext()) {
                if (itts.next().getThingId() == thingId) {
                    itts.remove();
                }
            }
            
            if (p != null) {
                Iterator<Thing> it = p.getThingsInPackage().iterator();
                while (it.hasNext()) {
                    if (it.next().getThingId() == thingId) {
                        it.remove();
                    }
                }

                listView.getItems().clear();
                infoLabel.setText("Paino: " + round(p.getWeight(), 3) +
                                        "\nKoko: " + round(p.getSize(), 5) + 
                                        "\nSisältö:");
                for (Thing t: p.getThingsInPackage()) {
                    listView.getItems().add(t.getName());
                }
                if (listView.getItems().isEmpty()) {
                    listView.getItems().add("");
                }
            }
            chooseObjectComboBox.getItems().clear();
            dbc.setThingTrigger(1);
        }
        
    }

    //rounding function for rounding weight's and size's double values
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
