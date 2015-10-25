package com.holdaas.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Lars on 9/30/2015.
 */

public class BagPopulator {
    String samplepath;
    BagOfTokens personbag, nonpersonbag;
    int countperson=0, countnonperson=0;
    List<TokenPair> tokenList;
    public enum Mode{
        SUBDIR,MAINDIR;
    }
    Mode mode;

    public BagPopulator(String path) {
        this(path, Mode.SUBDIR);
    }
    public BagPopulator(String path, Mode mode){
        personbag = new BagOfTokens();
        nonpersonbag = new BagOfTokens();
        samplepath = path;
        this.mode = mode;
    }

    public void add(BagOfTokens inbag, boolean isPerson) {
        if (isPerson) {
            countperson++;
            personbag.merge(inbag);
        }
        else {
            countnonperson++;
            nonpersonbag.merge(inbag);
        }
    }

    public String toString(){
        personbag.sort(0);
        nonpersonbag.sort(0);
        return ("Total number of Persons: " + countperson + "\nPerson bag:\n" + personbag +
                "Total number of Nonpersons: " + countnonperson + "\nNonperson bag:\n" + nonpersonbag);
    }
}
