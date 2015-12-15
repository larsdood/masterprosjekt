package com.holdaas.app;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lars on 9/30/2015.
 */

public class BagPopulator {
    //String samplepath;
    int persontags, nonpersontags;
    BagOfTokens personbag, nonpersonbag;
    int countperson=0, countnonperson=0;
    List<TokenPair> tokenList;
    private Map<String, Float> personPointMap, nonpersonPointMap;
    public enum Mode{
        SUBDIR,MAINDIR;
    }
    Mode mode;

    public BagPopulator() { this("", Mode.SUBDIR); }

    public BagPopulator(String path) {
        this(path, Mode.SUBDIR);
    }
    public BagPopulator(String path, Mode mode){
        persontags=0;
        nonpersontags=0;
        personbag = new BagOfTokens();
        nonpersonbag = new BagOfTokens();
        personPointMap = new HashMap<String, Float>();
        nonpersonPointMap = new HashMap<String, Float>();
        //samplepath = path;
        this.mode = mode;
    }

    public void add(BagOfTokens inbag, boolean isPerson) {
        if (isPerson) {
            countperson++;
            persontags+=inbag.size();
            personbag.merge(inbag);
        }
        else {
            countnonperson++;
            nonpersontags+=inbag.size();
            nonpersonbag.merge(inbag);
        }
    }

    public void calculatePoints(){
        for (Map.Entry<String, Integer> entry : personbag.getMap().entrySet()){
            personPointMap.put(entry.getKey(), 100*(float)entry.getValue() / persontags);
        }
        for (Map.Entry<String, Integer> entry : nonpersonbag.getMap().entrySet()){
            nonpersonPointMap.put(entry.getKey(), 100*(float)entry.getValue() / nonpersontags);
        }
    }

    public void writeToFile(String bagoftokensPath) throws IOException {
        PrintWriter writer;
        File directory = new File(bagoftokensPath);
        directory.mkdirs();
        writer = new PrintWriter(bagoftokensPath + "/personsBoT.txt", "UTF-8");
        for (Map.Entry<String, Float> entry : personPointMap.entrySet()){
            writer.println(entry.getKey());
            writer.println(entry.getValue());
        }
        writer.close();
        writer = new PrintWriter(bagoftokensPath + "nonpersonsBoT.txt", "UTF-8");
        for (Map.Entry<String, Float> entry : nonpersonPointMap.entrySet()){
            writer.println(entry.getKey());
            writer.println(entry.getValue());
        }
        writer.close();
    }

    public String toString(){
        personbag.sort(0);
        nonpersonbag.sort(0);
        return ("Total number of Persons: " + countperson + "\nTotal number of person tags: " + persontags + "\nPerson bag:\n" + personbag +
                "Total number of Nonpersons: " + countnonperson + "\nTotal number of nonperson tags: " + nonpersontags + "\nNonperson bag:\n" + nonpersonbag);
    }
}
