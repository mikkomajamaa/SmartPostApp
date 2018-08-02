/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ht;

/**
 *
 * @author mikko
 */
public class Thing {
    private int thingId;
    private String name;
    private double size;
    private double weight;
    private boolean breakable;
    
    public Thing(int tid, String n, double s, double w, boolean b) {
        thingId = tid;
        name = n;
        size = s;
        weight = w;
        breakable = b;
    }
    
    public int getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }

    public double getSize() {
        return size;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isBreakable() {
        return breakable;
    }
}
