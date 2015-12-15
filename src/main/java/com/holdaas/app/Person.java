package com.holdaas.app;

/**
 * Created by Lars on 12/13/2015.
 */
public class Person {
    String givenKanji, givenHiragana, familyKanji, familyHiragana;
    int birthYear;
    String profession;

    public Person(String input){
        parseInput(input);
    }
    private void parseInput(String input){
        /**
         * Wikipedia Articles vary wildly in format. Therefore, several regular expressions are used to capture the most
         * commonly occurring ones.
         */
        String temp = input;
        if (temp.matches("\\s.+"))
            temp = temp.substring(1);
        if (temp.charAt(0) == '\n')
            temp = temp.substring(1);
        if (temp.matches("\\d[代目].+")){
            temp = temp.substring(3);
        }
        if (temp.matches("\\S+[（(].+")){
            System.out.println("Step 1, Disqualified 1");
            return;
        }
        if (!(temp.contains("(") || temp.contains("（") || temp.contains(" "))) {
            System.out.println("Step 1, Disqualified 2");
            return;
        }

        /* NAMES BEFORE PARANTHESIS */
        System.out.println(temp);

        if (temp.matches("\\p{InCJKUnifiedIdeographs}+\\s\\S+[（(].+")){
            System.out.print("Step 1, Case 1. ");
            familyKanji = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyKanji.length()+1);
            givenKanji = temp.substring(0, firstParanthesisOpen(temp));
            temp = temp.substring(givenKanji.length() + 1);
        }
        else if (temp.matches("\\p{InCJKUnifiedIdeographs}+\\s\\S+\\s[（(].+")) {
            System.out.print("Step 1, Case 2. " + temp);
            familyKanji = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyKanji.length()+1);
            givenKanji = temp.substring(0, firstParanthesisOpen(temp)-1);
            temp = temp.substring(givenKanji.length()+2);
        }
        else if (temp.matches("\\p{InCJKUnifiedIdeographs}+\\s\\S+[、]\\S+\\s\\S+[（(].+")){
            System.out.print("Step 1, Case Alternate Kanjis. ");
            familyKanji = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyKanji.length()+1);
            givenKanji = temp.substring(0, temp.indexOf('、'));
            temp = temp.substring(firstParanthesisOpen(temp)+1);
        }

        /* AFTER FIRST PARANTHESIS */
        if (temp.matches("\\d\\d\\d\\d[年].+")) {
            System.out.print("Step 2, Case Hiragana-Only");
            familyHiragana = familyKanji;
            givenHiragana = givenKanji;
            familyKanji = "N/A";
            givenKanji = "N/A";
        }
        else if (temp.matches("\\S+[、]\\d.+")){
            System.out.println("Step 2, Disqualified 1");

        }
        else if (temp.matches("\\p{InHiragana}+\\s\\p{InHiragana}+[、].+")){
            System.out.println("Step 2, Case 1");
            familyHiragana = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyHiragana.length()+1);
            givenHiragana = temp.substring(0, temp.indexOf('、'));
            temp = temp.substring(givenHiragana.length()+1);
        }
        else if (temp.matches("\\p{InHiragana}+\\s\\p{InHiragana}+[)）].+")) {
            System.out.println("Step 2, Case 2");
            familyHiragana = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyHiragana.length() + 1);
            givenHiragana = temp.substring(0, firstParanthesisClose(temp));
            temp = temp.substring(givenHiragana.length() + 1);
        }
        else if (temp.matches("\\p{InHiragana}+\\s\\p{InHiragana}+\\s.+")){
            System.out.println("Step 2, Case 3");
            familyHiragana = temp.substring(0, temp.indexOf(' '));
            temp = temp.substring(familyHiragana.length()+1);
            if (temp.matches("[の]\\s.+")) {
                temp = temp.substring(2);
                if (temp.matches("\\p{InHiragana}+[、].+"))
                    givenHiragana = temp.substring(0, temp.indexOf('、'));
                else if (temp.matches("\\p{InHiragana}+[／].+") || temp.matches("\\p{InHiragana}+[/].+"))
                    givenHiragana = temp.substring(0, firstSlash(temp));
                else
                    givenHiragana = temp.substring(0, firstParanthesisClose(temp));
            }
            else{
                givenHiragana = temp.substring(0, temp.indexOf(' '));
            }
            temp = temp.substring(givenHiragana.length()+1);
        }
        else {
            System.out.println("Not valid: " + input);
            return;
        }


        if (temp.matches("\\d{4}[年].+")){
            birthYear = Integer.parseInt("" + temp.charAt(0) + temp.charAt(1) + temp.charAt(2) + temp.charAt(3));
        }
        else if (temp.matches(".+\\d{4}[年].+")) {
            String yearString = temp.substring(temp.split("\\d{4}[年]")[0].length());
            birthYear = Integer.parseInt("" + yearString.charAt(0) + yearString.charAt(1) + yearString.charAt(2) + yearString.charAt(3));
        }
    }

    public int firstParanthesisOpen(String input){
        int pos = -1;
        if (input.contains("(")){
            if (input.contains ("（")){
                pos = input.indexOf('(') < input.indexOf('（') ? input.indexOf('(') : input.indexOf('（');
            }
            else
                pos = input.indexOf('(');
        }
        else if (input.contains("（")){
            pos = input.indexOf('（');
        }
        else
            System.out.println("Fail: " + input);
        return pos;
    }

    public int firstParanthesisClose(String input){
        int pos = -1;
        if (input.contains(")")){
            if (input.contains ("）")){
                pos = input.indexOf(')') < input.indexOf('）') ? input.indexOf(')') : input.indexOf('）');
            }
            else
                pos = input.indexOf(')');
        }
        else if (input.contains("）")){
            pos = input.indexOf('）');
        }
        else
            System.out.println("Fail: " + input);
        return pos;
    }
    public int firstSlash(String input){
        int pos = -1;
        if (input.contains("/")){
            if (input.contains ("／")){
                pos = input.indexOf('/') < input.indexOf('／') ? input.indexOf('/') : input.indexOf('／');
            }
            else
                pos = input.indexOf('/');
        }
        else if (input.contains("／")){
            pos = input.indexOf('／');
        }
        else
            System.out.println("Fail: " + input);
        return pos;
    }

    public String toString(){
        return "Family: [" + familyKanji + "] [" + familyHiragana + "], Given: [" + givenKanji + "] [" + givenHiragana + "], Born: " + birthYear;
    }

    public boolean isNull(){
        if ((givenKanji == null || familyKanji == null) || (givenHiragana == null || familyHiragana == null))
            return true;
        return false;
    }
}