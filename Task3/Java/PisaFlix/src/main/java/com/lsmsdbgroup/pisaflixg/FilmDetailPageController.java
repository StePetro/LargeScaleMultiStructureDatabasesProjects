package com.lsmsdbgroup.pisaflixg;

import com.lsmsdbgroup.pisaflix.pisaflixservices.WikiScraper;
import com.lsmsdbgroup.pisaflix.Entities.*;
import com.lsmsdbgroup.pisaflix.pisaflixservices.*;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class FilmDetailPageController implements Initializable {

    private Film film;

    @FXML
    private Label titleLabel;

    @FXML
    private Label publishDateLabel;
    
    @FXML
    private TextArea postArea;

    @FXML
    private VBox postVBox;

    @FXML
    private Button followButton;
    
    @FXML
    private Button postButton;

    @FXML
    private Label followerLabel;

    @FXML
    private Pagination pagination;

    @FXML
    private ScrollPane scrollableDescription;

    @FXML
    private ImageView moviePosterImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            if (PisaFlixServices.authenticationService.isUserLogged()) {
                followButton.setDisable(false);
                postButton.setDisable(false);
                postArea.setPromptText("Write here a post for the film...");
                postArea.setEditable(true);
            }

            pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex)
                    -> refreshPosts());
        } catch (Exception ex) {
            App.printErrorDialog("Film Details", "An error occurred in inizialization", ex.toString() + "\n" + ex.getMessage());
        }
    }

    public void setTitleLabel(String title) {
        titleLabel.setText(title);
    }

    public void setPublishDate(String publishDate) {
        publishDateLabel.setText(publishDate);
    }

    private Pane createPost(String username, String timestamp, String postStr, Post post) {
        Pane pane = new Pane();
        try {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Post.fxml"));
                PostController postController = new PostController(username, timestamp, postStr, 0);
                postController.setPost(post);
                loader.setController(postController);
                pane = loader.load();
            } catch (IOException ex) {
                App.printErrorDialog("Film Details", "IOException", ex.toString() + "\n" + ex.getMessage());
            }
        } catch (Exception ex) {
            App.printErrorDialog("Film Details", "An error occurred creating the post", ex.toString() + "\n" + ex.getMessage());
        }
        return pane;
    }

    public void setFilm(Film film) {
        this.film = film;

        setFollowButton();

        setTitleLabel(film.getTitle());
        
        Calendar cld = Calendar.getInstance();
        cld.setTime(film.getPublicationDate());
        
        setPublishDate(String.valueOf(cld.get(Calendar.YEAR)));

        WikiScraper scraper = new WikiScraper(film.getWikiPage());
        String url = scraper.scrapePosterLink();
        if (url != null) {
            moviePosterImageView.setImage(new Image("https:" + url));
        }
        
        followerLabel.setText("(" + PisaFlixServices.filmService.countFollowers(film) + ")");
        
        int count = PisaFlixServices.postService.count(film);
        if (count == 0) {
            pagination.pageCountProperty().setValue(1);
        } else {
            pagination.setPageCount((int) Math.ceil(count*1.0/PisaFlixServices.filmService.getPostPageSize()*1.0));
        }
        
        refreshPosts();
        
    }

    public void refreshFilm() {
        film = PisaFlixServices.filmService.getById(film.getId());
    }

    public void refreshPosts() {
        
        postVBox.getChildren().clear();

        Set<Post> posts = PisaFlixServices.filmService.getRelatedPosts(film, pagination.getCurrentPageIndex());
        
        posts.forEach((post) -> {
            postVBox.getChildren().add(createPost(post.getUser().getUsername(), post.getTimestamp().toString(), post.getText(), post));
        });
        
    }

    @FXML
    private void followUnfollow() {
        if (PisaFlixServices.filmService.isFollowing(film, PisaFlixServices.authenticationService.getLoggedUser())) {
            PisaFlixServices.filmService.unfollow(film, PisaFlixServices.authenticationService.getLoggedUser());
            followButton.setText("+ Follow");
        } else {
            PisaFlixServices.filmService.follow(film, PisaFlixServices.authenticationService.getLoggedUser());
            followButton.setText("- Unfollow");
        }
        followerLabel.setText("(" + PisaFlixServices.filmService.countFollowers(film) + ")");
    }
    
    @FXML
    private void addPost() throws IOException {
        try {
            String post = postArea.getText();
            User user = PisaFlixServices.authenticationService.getLoggedUser();
            Set<Film> filmSet = new LinkedHashSet<>();
            filmSet.add(film);
            PisaFlixServices.postService.create(post, user, filmSet);

            refreshFilm();
            refreshPosts();
        } catch (Exception ex) {
            App.printErrorDialog("Comments", "An error occurred creating the post", ex.toString() + "\n" + ex.getMessage());
        }
    }
    
    public void setFollowButton() {
        if (PisaFlixServices.authenticationService.isUserLogged()) {
            if (!PisaFlixServices.filmService.isFollowing(film, PisaFlixServices.authenticationService.getLoggedUser())) {
                followButton.setText("+ Follow");
            } else {
                followButton.setText("- Unfollow");
            }
        }
    }

}
