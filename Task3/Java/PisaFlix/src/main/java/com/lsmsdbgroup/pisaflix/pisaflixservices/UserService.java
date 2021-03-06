package com.lsmsdbgroup.pisaflix.pisaflixservices;

import com.lsmsdbgroup.pisaflix.Entities.Film;
import com.lsmsdbgroup.pisaflix.Entities.User;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.*;
import com.lsmsdbgroup.pisaflix.dbmanager.Interfaces.UserManagerDatabaseInterface;
import com.lsmsdbgroup.pisaflix.pisaflixservices.Interfaces.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService implements UserServiceInterface {

    private final AuthenticationServiceInterface authenticationService;
    private final UserManagerDatabaseInterface userManager;

    UserService(UserManagerDatabaseInterface userManager, AuthenticationServiceInterface authenticationService) {
        this.authenticationService = authenticationService;
        this.userManager = userManager;
    }

    @Override
    public void deleteLoggedAccount() throws UserNotLoggedException, InvalidPrivilegeLevelException {
        deleteUserAccount(authenticationService.getLoggedUser());
    }

    @Override
    public User getById(Long id) {
        User user = userManager.getById(id);
        return user;
    }

    @Override
    public Set<User> getAll(int limit) {
        Set<User> users = userManager.getAll(limit);
        return users;
    }

    @Override
    public Set<User> getFiltered(String nameFilter, int limit, int skip) {
        Set<User> users = userManager.getFiltered(nameFilter, limit, skip);
        return users;
    }

    @Override
    public void updateUser(User user) {
        userManager.update(user);
    }

    @Override
    public void deleteUserAccount(User user) throws UserNotLoggedException, InvalidPrivilegeLevelException {
        if (!authenticationService.isUserLogged()) {
            throw new UserNotLoggedException("You must be logged in order to delete accounts");
        }

        if (!Objects.equals(authenticationService.getLoggedUser().getId(), user.getId()) && authenticationService.getLoggedUser().getPrivilegeLevel() < UserPrivileges.ADMIN.getValue()) {
            throw new InvalidPrivilegeLevelException("You must have administrator privileges in order to delete other user accounts");
        }
        userManager.delete(user.getId());
        if (Objects.equals(authenticationService.getLoggedUser().getId(), user.getId())) {
            authenticationService.logout();
        }
    }

    

    @Override
    public void changeUserPrivileges(User u, UserPrivileges newPrivilegeLevel) throws UserNotLoggedException, InvalidPrivilegeLevelException {
        if (!authenticationService.isUserLogged()) {
            throw new UserNotLoggedException("You must be logged in order to change account privileges");
        }
        User loggedUser = authenticationService.getLoggedUser();

        if (newPrivilegeLevel.getValue() < UserPrivileges.NORMAL_USER.getValue()) {
            throw new InvalidPrivilegeLevelException("Privilege level must me greater or equal than Normal user");
        }

        if (newPrivilegeLevel.getValue() > loggedUser.getPrivilegeLevel()) {
            throw new InvalidPrivilegeLevelException("You can't set privileges greater than yours");
        }

        if (u.getPrivilegeLevel() >= loggedUser.getPrivilegeLevel()) {
            throw new InvalidPrivilegeLevelException("You can't change privileges to users that have privileges equal or greater than yours");
        }

        u.setPrivilegeLevel(newPrivilegeLevel.getValue());
        userManager.update(u);
    }

    private boolean checkDuplicates(String username, String email) {
        return userManager.checkDuplicates(username, email);
    }

    @Override
    public void register(String username, String password, String email, String firstName, String lastName) throws InvalidFieldException {
        if (password.isBlank()) {
            throw new InvalidFieldException("Password is mandatory");
        }
        String hashedPassword = SHA256(password);
        if (username.isBlank() || !username.matches("^[a-zA-Z0-9._-]{3,}$")) {
            throw new InvalidFieldException("Only valid usernames are accepted");
        }
        if (checkDuplicates(username, email)) {
            throw new InvalidFieldException("Username or Email already exist");
        }
        if (email.isBlank() || !email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")) {
            throw new InvalidFieldException("Only valid emails are accepted");
        }
        if (!firstName.matches("[a-zA-Z]+") && !firstName.isEmpty()) {
            throw new InvalidFieldException("Only valid names are accepted");
        }
        if (!lastName.matches("[a-zA-Z]+") && !lastName.isEmpty()) {
            throw new InvalidFieldException("Only valid names are accepted");
        }
        // TODO: aggiornare il campo password nel DB ad almeno 64 caratteri e
        // Sostituire password con hashedPassword nella chiamata alla create()
        // Controllare se l'username esist già nel db
        userManager.create(username, hashedPassword, firstName, lastName, email, 0);
    }
    
    private String SHA256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Change this to UTF-16 if needed
            md.update(text.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            String hex = String.format("%064x", new BigInteger(1, digest));

            return hex;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PisaFlixServices.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void follow(User follower, User followed) {
        userManager.follow(follower, followed);
    }

    @Override
    public boolean isFollowing(User follower, User followed) {
        return userManager.isFollowing(follower, followed);
    }

    @Override
    public void unfollow(User follower, User followed) {
        userManager.unfollow(follower, followed);
    }

    @Override
    public long countFollowers(User user) {
        return userManager.countFollowers(user);
    }

    @Override
    public long countFollowingUsers(User user) {
        return userManager.countFollowing(user).get("followingUsers");
    }

    @Override
    public long countFollowingFilms(User user) {
        return userManager.countFollowing(user).get("followingFilms");
    }

    @Override
    public long countTotalFollowing(User user) {
        HashMap<String, Long> count = userManager.countFollowing(user);
        return count.get("followingUsers") + count.get("followingFilms");
    }

    @Override
    public Set<User> getFollowers(User user) {
        return userManager.getFollowers(user);
    }

    @Override
    public Set<User> getFollowingUsers(User user) {
        return userManager.getFollowingUsers(user);
    }

    @Override
    public Set<Film> getFollowingFilms(User user) {
        return userManager.getFollowingFilms(user);
    }

    @Override
    public Set<User> getSuggestedUsers(User user, int limit) {
        return userManager.getSuggestedUsers(user, limit);
    }
    
    @Override
    public Set<User> getVerySuggestedUsers(User user, int limit) {
        return userManager.getVerySuggestedUsers(user, limit);
    }

    @Override
    public Set<User> getMixedUsers(User user) {
        
        Set<User> mix= userManager.getVerySuggestedUsers(user, userManager.getLimit());
        
        if(mix.size() < userManager.getLimit()){
            
            mix.addAll(userManager.getSuggestedUsers(user, userManager.getLimit() - mix.size()));
            
        }
        
        
        if(mix.size() < userManager.getLimit()){
            
            mix.addAll(userManager.getDifferentUsers(mix, userManager.getLimit() - mix.size()));
            
        }
        
        return mix;
        
    }

    @Override
    public Set<User> getDifferentUsers(Set<User> userSet, int limit) {
        return userManager.getDifferentUsers(userSet, limit);
    }
}
