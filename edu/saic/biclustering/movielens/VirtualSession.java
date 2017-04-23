/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.saic.biclustering.movielens;

import java.io.*;
import java.util.*;
/**
 *
 * @author jhyeh
 */
public class VirtualSession {
    private HashMap vshash;

    public VirtualSession() {
        this("data.obj");
    }

    public VirtualSession(String str) {
        this.vshash = this.loadVirtualSession(str);
    }

    private HashMap loadVirtualSession(String str) {
        HashMap vs = new HashMap();
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(str));
            Object obj = ois.readObject();
            if (obj instanceof HashMap)
                vs = (HashMap)obj;
            ois.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return vs;
    }

    public int getScore(String uid, String mid) {
        if (this.vshash == null) {
            System.err.println("Null vsession");
            return -1;
        }

        HashMap local = (HashMap)this.vshash.get(uid);
        if (local == null) {
            System.err.println("Null local vsession for uid="+uid);
            return -1;
        }

        Integer ii = (Integer)local.get(mid);
        if (ii == null) {
            //System.err.println("No score for uid="+uid+", mid="+mid);
            return -1;
        }

        return ii.intValue();
    }
}
