
package multithread;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Erik
 */
public class Resource {
    private int resourceID;
    private String Name;
    private boolean isOwned;
    private String owner;

    public Resource(int resourceID, String Name, boolean isOwned, String owner) {
        this.resourceID = resourceID;
        this.Name = Name;
        this.isOwned = isOwned;
        this.owner = owner;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getName() {
        return Name;
    }

    public boolean getIsOwned() {
        return isOwned;
    }

    public String getOwner() {
        return owner;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setIsOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Resource{" + "resourceID=" + resourceID + ", Name=" + Name + ", isOwned=" + isOwned + ", owner=" + owner + '}';
    }
    
    
    
}
