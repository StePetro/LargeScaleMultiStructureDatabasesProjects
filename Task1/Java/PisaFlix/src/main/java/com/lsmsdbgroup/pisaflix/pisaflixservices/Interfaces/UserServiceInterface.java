package com.lsmsdbgroup.pisaflix.pisaflixservices.Interfaces;

import com.lsmsdbgroup.pisaflix.Entities.User;
import com.lsmsdbgroup.pisaflix.pisaflixservices.UserPrivileges;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.*;
import java.util.Set;

public interface UserServiceInterface {

    void deleteLoggedAccount() throws UserNotLoggedException, InvalidPrivilegeLevelException;

    User getUserById(int id);

    Set<User> getAll();

    Set<User> getFiltered(String nameFilter);

    void updateUser(User user);

    void register(String username, String password, String email, String firstName, String lastName) throws InvalidFieldException;

    void deleteUserAccount(User u) throws UserNotLoggedException, InvalidPrivilegeLevelException;

    void checkUserPrivilegesForOperation(UserPrivileges privilegesToAchieve) throws UserNotLoggedException, InvalidPrivilegeLevelException;

    void checkUserPrivilegesForOperation(UserPrivileges privilegesToAchieve, String operation) throws UserNotLoggedException, InvalidPrivilegeLevelException;

    void changeUserPrivileges(User u, UserPrivileges newPrivilegeLevel) throws UserNotLoggedException, InvalidPrivilegeLevelException;
}
