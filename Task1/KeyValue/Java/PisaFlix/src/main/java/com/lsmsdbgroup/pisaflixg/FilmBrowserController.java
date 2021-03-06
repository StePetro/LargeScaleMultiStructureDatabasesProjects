package com.lsmsdbgroup.pisaflixg;

import com.lsmsdbgroup.pisaflix.Entities.Film;
import com.lsmsdbgroup.pisaflix.pisaflixservices.PisaFlixServices;
import com.lsmsdbgroup.pisaflix.pisaflixservices.UserPrivileges;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.InvalidPrivilegeLevelException;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.UserNotLoggedException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class FilmBrowserController extends BrowserController implements Initializable{
    
    public FilmBrowserController(){}
   
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            super.initialize();
            filterTextField.setPromptText("Title filter");
            searchFilms(null, null);
        } catch (Exception ex) {
            App.printErrorDialog("Films", "There was an inizialization error", ex.toString() + "\n" + ex.getMessage());
        }
    }
    
    @Override
    public Pane createCardPane(String title, String publishDate, int id) {
        Pane pane = new Pane();
        try {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FilmCard.fxml"));
                FilmCardController fcc = new FilmCardController(title, publishDate, id);
                loader.setController(fcc);
                pane = loader.load();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (Exception ex) {
            App.printErrorDialog("Films", "An error occurred creating the film card", ex.toString() + "\n" + ex.getMessage());
        }
        return pane;
    }
    
    @FXML
    @Override
    public void filter() {
        try {
            String titleFilter = filterTextField.getText();

            searchFilms(titleFilter, null);
        } catch (Exception ex) {
            App.printErrorDialog("Films", "An error occurred searching the films", ex.toString() + "\n" + ex.getMessage());
        }
    }
    
    @FXML
    @Override
    public void add() {
        try {
            try {
                PisaFlixServices.userService.checkUserPrivilegesForOperation(UserPrivileges.MODERATOR, "add a new film");
            } catch (UserNotLoggedException | InvalidPrivilegeLevelException ex) {
                System.out.println(ex.getMessage());
                return;
            }
            App.setMainPageReturnsController("AddFilm");
        } catch (Exception ex) {
            App.printErrorDialog("Films", "An error occurred", ex.toString() + "\n" + ex.getMessage());
        }
    }
    
    @FXML
    public void searchFilms(String titleFilter, Date dateFilter) {
        try {
            Set<Film> films = PisaFlixServices.filmService.getFilmsFiltered(titleFilter, dateFilter, dateFilter);
            populateScrollPane(films);
        } catch (Exception ex) {
            App.printErrorDialog("Films", "An error occurred searching the films", ex.toString() + "\n" + ex.getMessage());
        }
    }
    
    public void populateScrollPane(Set<Film> films) {
        tilePane.getChildren().clear();
        String title;
        String publishDate;
        int id;

        Pane pane;
        int i = 0;
        for (Film film : films) {
            title = film.getTitle();
            publishDate = film.getPublicationDate().toString();
            id = film.getIdFilm();

            pane = createCardPane(title, publishDate, id);
            tilePane.getChildren().add(pane);
        }
    }
}
