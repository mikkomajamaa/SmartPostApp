/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author mikko
 */
public class PostPackage {
    private int id;
    private int cl;
    private double weight = 0;
    private double size = 0;
    private int fromSP;
    private int toSP;
    private boolean sent = false;
    private double length = 0;

    public double getLength() {
        return length;
    }

    public void setLength(double l) {
        length = l;
    }

    private ArrayList<Thing> thingsInPackage = null;
    
    public boolean isPackageEmpty() {
        if (thingsInPackage.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public PostPackage (int i) {
        id = i;
    }
    public void setToSP(int i) {
        toSP = i;
    }
    
    public int getToSP() {
        return toSP;
    }
    
    public void setFromSP(int i) {
        fromSP = i;
    }
    
    public int getFromSP() {
        return fromSP;
    }
   
    public void setClass(int c) {
        cl = c;
    }
    
    public int getPostalClass() {
        return cl;
    }
    
    public int getId() {
        return id;
    }
    
    public void setSent() {
        sent = true;
    }
    
    public boolean isSent() {
        return sent;
    }
    
    public void insertThingToPackage(Thing t){
        if (thingsInPackage == null) {
            thingsInPackage = new ArrayList<>();
            thingsInPackage.add(t);
        } else {
            thingsInPackage.add(t);
        }
    }
    
    public void removeThingFromPackage(int thingId) {
        Iterator <Thing> it = this.getThingsInPackage().iterator();
            while (it.hasNext()) {
                if (it.next().getThingId() == thingId) {
                    it.remove();
                }
            }
    }
    
    public ArrayList<Thing> getThingsInPackage() {
        return thingsInPackage;
    }
     
    public double getWeight() {
        weight = 0;
        if (thingsInPackage.isEmpty()) {
            return 0;
        }
        for (Thing t: thingsInPackage) {
            weight += t.getWeight();
        }
        return weight;
    }
    
    public double getSize() {
        size = 0;
        if (thingsInPackage.isEmpty()) {
            return 0;
        }
        for (Thing t: thingsInPackage) {
            size += t.getSize();
        }
        return size;
    }
}
