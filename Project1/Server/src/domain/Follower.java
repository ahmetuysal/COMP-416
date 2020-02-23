package domain;

import java.util.Date;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class Follower implements Correspondent {
    private Date lastUpdatedOn;

    /**
     * Initializes Follower, a Correspondent of Master Server.
     */
    public Follower() {
        this.lastUpdatedOn = new Date(Long.MIN_VALUE);
    }

    /**
     * Returns the last update time.
     *
     * @return lastUpdatedOn field, representing the last update time.
     */
    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }


    /**
     * Sets the last update time.
     *
     * @param lastUpdatedOn The value to set lastUpdatedOn field.
     */
    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

}
