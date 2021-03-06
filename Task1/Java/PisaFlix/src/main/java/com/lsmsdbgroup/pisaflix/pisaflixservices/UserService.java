package com.lsmsdbgroup.pisaflix.pisaflixservices;

import com.lsmsdbgroup.pisaflix.Entities.User;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.*;
import com.lsmsdbgroup.pisaflix.dbmanager.Interfaces.UserManagerDatabaseInterface;
import com.lsmsdbgroup.pisaflix.pisaflixservices.Interfaces.*;
import java.util.Objects;
import java.util.Set;

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
    public User getUserById(int id) {
        User user = userManager.getById(id);
        return user;
    }

    @Override
    public Set<User> getAll() {
        Set<User> users = userManager.getAll();
        return users;
    }

    @Override
    public Set<User> getFiltered(String nameFilter) {
        Set<User> users = userManager.getFiltered(nameFilter);
        return users;
    }

    @Override
    public void updateUser(User user) {
        userManager.update(user);
    }

    @Override
    public void deleteUserAccount(User u) throws UserNotLoggedException, InvalidPrivilegeLevelException {
        if (!authenticationService.isUserLogged()) {
            throw new UserNotLoggedException("You must be logged in order to delete accounts");
        }
        if (!Objects.equals(authenticationService.getLoggedUser().getIdUser(), u.getIdUser()) && authenticationService.getLoggedUser().getPrivilegeLevel() < UserPrivileges.ADMIN.getValue()) {
            throw new InvalidPrivilegeLevelException("You must have administrator privileges in order to delete other user accounts");
        }
        userManager.delete(u.getIdUser());
        if (Objects.equals(authenticationService.getLoggedUser().getIdUser(), u.getIdUser())) {
            authenticationService.logout();
        }
    }

    @Override
    public void checkUserPrivilegesForOperation(UserPrivileges privilegesToAchieve) throws UserNotLoggedException, InvalidPrivilegeLevelException {
        checkUserPrivilegesForOperation(privilegesToAchieve, "do this operation");
    }

    @Override
    public void checkUserPrivilegesForOperation(UserPrivileges privilegesToAchieve, String operation) throws UserNotLoggedException, InvalidPrivilegeLevelException {
        if (!authenticationService.isUserLogged()) {
            throw new UserNotLoggedException("You must be logged in order to " + operation);
        }
        if (authenticationService.getLoggedUser().getPrivilegeLevel() < privilegesToAchieve.getValue()) {
            throw new InvalidPrivilegeLevelException("You don't have enought privilege to " + operation);
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

    @Override
    public void register(String username, String password, String email, String firstName, String lastName) throws InvalidFieldException {
        if (username.isBlank() || !username.matches("^[a-zA-Z0-9._-]{3,}$")) {
            throw new InvalidFieldException("Only valid usernames are accepted");
        }
        if (checkDuplicates(username, email)) {
            throw new InvalidFieldException("Username or Email already exist");
        }
        if (email.isBlank() || !email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")) {
            throw new InvalidFieldException("Only valid emails are accepted");
        }
        if (password.isBlank()) {
            throw new InvalidFieldException("Password is mandatory");
        }
        if (!firstName.matches("[a-zA-Z]+") && !firstName.isEmpty()) {
            throw new InvalidFieldException("Only valid names are accepted");
        }
        if (!lastName.matches("[a-zA-Z]+") && !lastName.isEmpty()) {
            throw new InvalidFieldException("Only valid names are accepted");
        }
        userManager.create(username, password, firstName, lastName, email, 0);
    }

    private boolean checkDuplicates(String username, String email) {
        return userManager.checkDuplicates(username, email);
    }
}
