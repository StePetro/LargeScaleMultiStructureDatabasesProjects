package com.lsmsdbgroup.pisaflixg;

import com.lsmsdbgroup.pisaflix.Entities.*;
import com.lsmsdbgroup.pisaflix.pisaflixservices.PisaFlixServices;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class AddProjectionController implements Initializable {
    
    @FXML
    private ComboBox cinemaComboBox;
    @FXML
    private ComboBox filmComboBox;
    @FXML
    private DatePicker dateDatePicker;
    @FXML
    private ComboBox timeComboBox;    
    @FXML
    private TextField roomTextField;
    @FXML
    private Label successLabel;
    
    DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        
        Set<Film> filmSet = PisaFlixServices.filmService.getAll();
        Set<Cinema> cinemaSet = PisaFlixServices.cinemaService.getAll();
        ObservableList observableFilmSet = FXCollections.observableArrayList(filmSet);
        ObservableList observableCinemaSet = FXCollections.observableArrayList(cinemaSet);
        

        filmComboBox.getItems().setAll(observableFilmSet);
        filmComboBox.getItems().add("All");
        
        cinemaComboBox.getItems().setAll(observableCinemaSet);
        cinemaComboBox.getItems().add("All");
        
        LocalTime lt = LocalTime.MIN;
        for(int i = 0; i < 48; ++i){
            
            timeComboBox.getItems().add(lt);
            lt=lt.plusMinutes(30);
        }
    }    
    
    private void resetFields(){
        cinemaComboBox.setValue("All");
        filmComboBox.setValue("All");
        dateDatePicker.setValue(null);
        roomTextField.setText("");
    }
    
    private void errorLabel(String s){       
        successLabel.setTextFill(Color.RED);
        successLabel.setText(s);
        successLabel.setManaged(true);
        successLabel.setVisible(true);
    }
    
    @FXML 
    private void clickAddProjectionButton(){
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        if(cinemaComboBox.getValue() == null || cinemaComboBox.getValue() == "All"){
            errorLabel("You must select a cinema");
            return;
        }
        if(filmComboBox.getValue() == null || filmComboBox.getValue() == "All"){
            errorLabel("You must select a film");
            return;
        }
        if(dateDatePicker.getValue() == null){
            errorLabel("You must select a day");
            return;
        }
        if(timeComboBox.getValue() == null){
            errorLabel("You must select a time");
            return;
        }
        if(roomTextField.getText() == null || !roomTextField.getText().matches("\\d+")){
            errorLabel("You must select a room");
            return;
        } else {
        }
        
        
        Cinema cinema = (Cinema) cinemaComboBox.getValue();
        Film film = (Film) filmComboBox.getValue();
        LocalTime lt = (LocalTime) timeComboBox.getValue();
        LocalDate ld = dateDatePicker.getValue();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        LocalDateTime ldt = LocalDateTime.of(ld, lt);
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        
        int room =  Integer.parseInt(roomTextField.getText());
        
        if(PisaFlixServices.projectionService.checkDuplicates(cinema.getIdCinema(), film.getIdFilm(), dateFormat.format(date), room)){
            errorLabel("Projection already scheduled");
            return;
        }
        
        try {
            PisaFlixServices.projectionService.addProjection(cinema, film, date, room);
        } catch (UserNotLoggedException | InvalidPrivilegeLevelException ex) {
            System.out.println(ex.getMessage());
        }
        successLabel.setTextFill(Color.GREEN);
        successLabel.setText("Projection succesfully scheduled");
        successLabel.setManaged(true);
        successLabel.setVisible(true);
        resetFields();
    }
    
}
