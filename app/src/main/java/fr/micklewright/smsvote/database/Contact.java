package fr.micklewright.smsvote.database;

import java.util.List;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table CONTACT.
 */
public class Contact {

    private Long number;
    private String name;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ContactDao myDao;

    private List<Application> applications;
    private List<Vote> votes;
    private List<Participation> participations;

    public Contact() {
    }

    public Contact(Long number) {
        this.number = number;
    }

    public Contact(Long number, String name) {
        this.number = number;
        this.name = name;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getContactDao() : null;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Application> getApplications() {
        if (applications == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ApplicationDao targetDao = daoSession.getApplicationDao();
            List<Application> applicationsNew = targetDao._queryContact_Applications(number);
            synchronized (this) {
                if(applications == null) {
                    applications = applicationsNew;
                }
            }
        }
        return applications;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetApplications() {
        applications = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Vote> getVotes() {
        if (votes == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            VoteDao targetDao = daoSession.getVoteDao();
            List<Vote> votesNew = targetDao._queryContact_Votes(number);
            synchronized (this) {
                if(votes == null) {
                    votes = votesNew;
                }
            }
        }
        return votes;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetVotes() {
        votes = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Participation> getParticipations() {
        if (participations == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ParticipationDao targetDao = daoSession.getParticipationDao();
            List<Participation> participationsNew = targetDao._queryContact_Participations(number);
            synchronized (this) {
                if(participations == null) {
                    participations = participationsNew;
                }
            }
        }
        return participations;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetParticipations() {
        participations = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
