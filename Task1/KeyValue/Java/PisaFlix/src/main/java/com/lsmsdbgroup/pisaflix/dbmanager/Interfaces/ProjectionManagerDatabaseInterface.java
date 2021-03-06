package com.lsmsdbgroup.pisaflix.dbmanager.Interfaces;

import com.lsmsdbgroup.pisaflix.Entities.*;
import java.util.*;

public interface ProjectionManagerDatabaseInterface {

    void create(Date dateTime, int room, Film film, Cinema cinema);

    void delete(int idProjection);

    void update(int idProjection, Date dateTime, int room);

    Set<Projection> getAll();

    Projection getById(int projectionId);

    Set<Projection> queryProjection(int cinemaId, int filmId, String date, int room);

    boolean checkDuplicates(int cinemaId, int filmId, String date, int room);

}
