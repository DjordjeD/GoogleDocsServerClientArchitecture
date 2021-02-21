/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientapp;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author praksa
 */
public class SyncRequests implements Comparable<SyncRequests>, Serializable {

    private static int sentFrom; //1 za klijenta //2 za server
    private static String fileName;

    public SyncRequests(int sentFrom, String dirname) {
        this.sentFrom = sentFrom;
        this.fileName = dirname;
    }

    public int getSentFrom() {
        return sentFrom;
    }

    public void setSentFrom(int sentFrom) {
        this.sentFrom = sentFrom;
    }

    public String getDirname() {
        return fileName;
    }

    public void setDirname(String dirname) {
        this.fileName = dirname;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentFrom, fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncRequests employee = (SyncRequests) o;
        return Double.compare(employee.sentFrom, sentFrom) == 0
                && Objects.equals(fileName, employee.fileName);
    }

    @Override
    public int compareTo(SyncRequests request) {
        if (this.getSentFrom() > request.getSentFrom()) {
            return 1;
        }
        if (this.getSentFrom() < request.getSentFrom()) {
            return -1;
        } else {
            return 0;
        }
    }

}
