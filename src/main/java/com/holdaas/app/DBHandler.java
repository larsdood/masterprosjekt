package com.holdaas.app;

/**
 * Created by Lars on 12/21/2015.
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DBHandler {
    int counter;
    String nameDBURL, personDBURL;
    private DBHandler(){
        this("C:/database/NameBase.db", "C:/database/PersonBase.db");
    }
    public DBHandler(String nameBasePath, String personBasePath){
        counter=0;
        nameDBURL = "jdbc:sqlite:" + nameBasePath;
        personDBURL = "jdbc:sqlite:" + personBasePath;
    }

    public void insertNameList(List<Name> list){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(nameDBURL);

            if (conn != null){
                Statement st = conn.createStatement();
                for (Name name : list){
                    try {st.executeUpdate("INSERT INTO Name " + "VALUES (" + counter+", '"+name.getKanji()+"', '" + name.getHiragana()+"')");}
                    catch (SQLException e){
                        conn.close();
                        System.out.println("Error on: " + name.getKanji() + ", " + name.getHiragana());
                        e.printStackTrace();
                        System.exit(0);
                    }
                    counter++;
                }
                conn.close();
            }
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void insertPersonList(List<Person> list){

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(personDBURL);

            if (conn != null){
                Statement st = conn.createStatement();
                for (Person person : list){
                    try {st.executeUpdate("INSERT INTO Person " + "VALUES (" +counter+", '"+person.getFamilyKanji()+"', '"+person.getFamilyHiragana()+
                            "', '"+person.getGivenKanji()+"', '"+person.getGivenHiragana()+"', "+person.getBirthYear()+")");}
                    catch (SQLException e){
                        System.out.println("Error on: " + person.getFamilyKanji() + " " + person.getGivenKanji() + ", " + person.getFamilyHiragana() + " " + person.getGivenHiragana() + person.getBirthYear());
                        e.printStackTrace();
                    }
                    counter++;
                }
                conn.close();

            }
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPerson(Person person){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(personDBURL);

            if (conn != null){
                Statement st = conn.createStatement();
                st.executeUpdate("INSERT INTO Person " + "VALUES (" +counter+", '"+person.getFamilyKanji()+"', '"+person.getFamilyHiragana()+
                        "', '"+person.getGivenKanji()+"', '"+person.getGivenHiragana()+"', "+person.getBirthYear()+")");
                conn.close();
                counter++;
            }
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertName(String kanjiText, String hiraganaText){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(nameDBURL);

            if (conn != null){
                Statement st = conn.createStatement();
                st.executeUpdate("INSERT INTO Name " + "VALUES (" + counter + ", '" + kanjiText + "', '" + hiraganaText + "')");
                conn.close();
            }
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
