package com.holdaas.app;

/**
 * Created by Lars on 12/26/2015.
 */
public class Name {
    String kanji;
    String hiragana;

    public Name(String kanji, String hiragana){
        this.kanji = kanji;
        this.hiragana = hiragana;
    }

    public String getKanji(){
        return kanji;
    }
    public String getHiragana(){
        return hiragana;
    }
    public void setKanji(String kanji){
        this.kanji = kanji;
    }
    public void setHiragana(String hiragana){
        this.hiragana = hiragana;
    }

    public static boolean IdenticalNames(Name n1, Name n2){
        if (n1.getKanji().equals(n2.getKanji()) && n1.getHiragana().equals(n2.getHiragana()))
            return true;
        return false;
    }
}