package com.lsmsdbgroup.pisaflix.Entities;

import java.io.Serializable;
import java.util.*;
import javax.persistence.*;

@Entity
@Table(name = "Projection")
public class Projection implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idProjection")
    private Integer idProjection;

    @Basic(optional = false)
    @Column(name = "dateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @Basic(optional = false)
    @Column(name = "room")
    private int room;

    @JoinColumn(name = "idCinema", referencedColumnName = "idCinema")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Cinema idCinema;

    @JoinColumn(name = "idFilm", referencedColumnName = "idFilm")
    @ManyToOne(fetch = FetchType.EAGER)
    private Film idFilm;

    public Projection() {
    }

    public Projection(Integer idProjection) {
        this.idProjection = idProjection;
    }

    public Projection(Integer idProjection, Date dateTime, int room) {
        this.idProjection = idProjection;
        this.dateTime = dateTime;
        this.room = room;
    }

    public Projection(Integer idProjection, Date dateTime, int room, Cinema cinema, Film film) {

        this(idProjection, dateTime, room);
        idCinema = cinema;
        idFilm = film;
    }

    public Integer getIdProjection() {
        return idProjection;
    }

    public void setIdProjection(Integer idProjection) {
        this.idProjection = idProjection;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public Cinema getIdCinema() {
        return idCinema;
    }

    public String getCinema() {
        return idCinema.getName();
    }

    public void setIdCinema(Cinema idCinema) {
        this.idCinema = idCinema;
    }

    public Film getIdFilm() {
        return idFilm;
    }

    public String getFilm() {
        return idFilm.getTitle();
    }

    public void setIdFilm(Film idFilm) {
        this.idFilm = idFilm;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProjection != null ? idProjection.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Projection)) {
            return false;
        }
        Projection other = (Projection) object;
        return !((this.idProjection == null && other.idProjection != null) || (this.idProjection != null && !this.idProjection.equals(other.idProjection)));
    }

    @Override
    public String toString() {
        return "idProjection=" + idProjection + " ]\ndateTime:"
                + dateTime.toString() + "\nroom:" + room + "\ncinema:"
                + idCinema.toString() + "\nfilm:" + idFilm.toString();
    }

}
