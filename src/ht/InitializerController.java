/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import ht.DBConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mikko
 */
public class InitializerController implements Initializable {

    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;
    @FXML
    private AnchorPane ap;
    @FXML
    private Label label;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        yesButton.setPrefSize(60, 30);
        noButton.setPrefSize(60, 30);
        ap.setPrefSize(220, 80);
        yesButton.setLayoutX(10);
        yesButton.setLayoutY(50);
        noButton.setLayoutX(160);
        noButton.setLayoutY(50);
        label.setMaxWidth(Double.MAX_VALUE);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        label.setAlignment(Pos.CENTER);
    }    

    @FXML
    private void yesButtonAction(ActionEvent event) {
        DBConnection.getInstance().setTrigger(1);
        DBConnection.getInstance().createDBforStubbornUserWhoChoosesToContinueLastSessionWithoutEverUsingTheProgram();
        DBConnection.getInstance().updateDB();
        Stage packageStage = new Stage();
        try {
            Parent page = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
            Scene scene = new Scene(page);

            packageStage.setScene(scene);
            packageStage.setResizable(false);
            packageStage.show();
            Stage stage = (Stage) yesButton.getScene().getWindow();
            stage.close();

        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void noButtonAction(ActionEvent event) {
        DBConnection.getInstance().setTrigger(0);
        Stage packageStage = new Stage();
        try {
            Parent page = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
            Scene scene = new Scene(page);

            packageStage.setScene(scene);
            packageStage.setResizable(false);
            packageStage.show();
            Stage stage = (Stage) noButton.getScene().getWindow();
            stage.close();

        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
