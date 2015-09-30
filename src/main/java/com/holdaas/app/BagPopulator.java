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
        if (isPerson)
            personbag.merge(inbag);
        else
            nonpersonbag.merge(inbag);
    }

    public String toString(){
        return ("Person bag:\n" + personbag + "\nNonperson bag:\n" + nonpersonbag);
    }
}
