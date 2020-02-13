package com.lsmsdbgroup.pisaflix.pisaflixservices.Interfaces;

import com.lsmsdbgroup.pisaflix.Entities.*;
import com.lsmsdbgroup.pisaflix.pisaflixservices.exceptions.*;
import java.util.*;

public interface FilmServiceInterface {

    Set<Film> getFilmsFiltered(String titleFilter, Date startDateFilter, Date endDateFilter);

    Set<Film> getAll();

    Film getById(Long id);

    boolean addFilm(String title, Date publicationDate) throws UserNotLoggedException, InvalidPrivilegeLevelException;

    void updateFilm(Film film) throws UserNotLoggedException, InvalidPrivilegeLevelException;

    void deleteFilm(Long idFilm) throws UserNotLoggedException, InvalidPrivilegeLevelException;
}
