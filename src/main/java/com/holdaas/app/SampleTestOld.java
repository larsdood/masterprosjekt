/*

package com.holdaas.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import org.junit.rules.Stopwatch;

public class SampleTest {

    public static void main(String[] args) throws IOException{

        Tokenizer tokenizer;
        Mode mode = Mode.NORMAL;
        tokenizer = Tokenizer.builder().mode(mode).build();


        String datafilespath = "D:/Japanese Wikipedia/target/";
        Analyser analyser = new Analyser(datafilespath);

        String outputpath = "D:/Japanese Wikipedia/output3/";
        int counter = 0;

        try {
            GenerateRelevantFiles(datafilespath, outputpath, tokenizer, analyser);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void GenerateRelevantFiles(String datafilespath, String outputpath, Tokenizer tokenizer, Analyser analyser) throws Exception{
        PrintWriter writer;
        int counter=0, successcounter=0;
        long timerstart, timerstop;
        File[] directories = new File(datafilespath).listFiles(File::isDirectory);
        File newdir = new File(outputpath);
        newdir.mkdirs();
        System.out.println((int)'°');
        System.out.println((int)'±');
        System.out.println((int)'・');
        System.out.println((int)'（');
        System.out.println((int)'）');
        for (File directory : directories){
            File[] files = new File(directory.getPath()).listFiles();
            for(File file : files){

                String output = FetchFirstArticle(file);
                System.out.print("Article number: " + counter + ", ");
                if (analyser.AnalyseArticleString(output)==Analyser.Status.PASS){
                    List<TokenPair> tokenPairs = new ArrayList<TokenPair>();
                    tokenPairs = TokenizeToPairList(output, tokenizer);

                    if(analyser.AnalyseTokens(tokenPairs) == Analyser.Status.PASS){
                        timerstart = System.currentTimeMillis();
                        System.out.println(file.getName());
                        writer = new PrintWriter(newdir + "/" + counter + "_" +  " article.txt" , "UTF-8");
                        writer.println(output.trim());
                        writer.close();
                        String tokenized = Tokenize(output, tokenizer);




                        writer = new PrintWriter(newdir + "/" + counter + "_"  + " tokenized.txt", "UTF-8");

                        writer.println(tokenized);

                        writer.close();
                        timerstop = System.currentTimeMillis();
                        System.out.println("Files generated, tokenized, in " + (long)(timerstop-timerstart) + " ms.");
                        successcounter++;
                    }
                }
                counter++;
                System.out.println("");
            }
        }
        System.out.println("Total amount of files processed: " + counter +
                "\nArticles deemed possibly relevant: " + successcounter +
                "\nArticles deemed not relevant: " + (counter-successcounter));
    }

    public void GenerateAllFiles(String datafilespath, String outputpath, Tokenizer tokenizer) throws Exception{
        PrintWriter writer;
        File[] directories = new File(datafilespath).listFiles(File::isDirectory);
        for (File directory : directories){
            File newdir = new File(outputpath + directory.getName());
            newdir.mkdirs();
            File[] files = new File(directory.getPath()).listFiles();
            for(File file : files){
                File filedir = new File(newdir + "/" + file.getName() + "/");
                filedir.mkdirs();

                writer = new PrintWriter(filedir + "/article.txt" , "UTF-8");
                String output = FetchFirstArticle(file);
                writer.println(output);

                writer.close();
                writer = new PrintWriter(filedir + "/tokenized.txt", "UTF-8");
                writer.println(Tokenize(output, tokenizer));
                writer.close();
            }
        }
    }

    public String Tokenize(File file){
        String output = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine() ) != null){
                if (line.contains("</doc>"))
                    break;
                if (!line.contains("<doc")){
                    output += line;
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Tokenize(output);
    }

    public String Tokenize(String input)
    {
        return Tokenize(input, Tokenizer.builder().mode(Mode.NORMAL).build());
    }

    public static String Tokenize(String input, Tokenizer tokenizer)
    {
        input = input.replace("\n", "");
        input = input.replace(" ", "");

        List<Token> result = tokenizer.tokenize(input);
        String output = "";

        for(Token token : result){
            output += token.getSurfaceForm() + "\t" + token.getAllFeatures() + System.getProperty("line.separator");
        }
        output.replace("記号,一般,*,*,*,*,*", "");
        output.replace("記号,空白,*,*,*,*,*", "");
        return output;
    }

    public static List<TokenPair> TokenizeToPairList(String input, Tokenizer tokenizer)
    {
        input = input.replace("\n", "");
        input = input.replace(" ", "");

        List<Token> result = tokenizer.tokenize(input);

        List<TokenPair> output = new ArrayList<TokenPair>();

        for(Token token : result){
            output.add(new TokenPair(token.getSurfaceForm(), token.getAllFeaturesArray()));
        }

        return output;
    }

    public static String FetchFirstArticle(File file){
        String output = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String newline = System.getProperty("line.separator");
            while ((line = br.readLine() ) != null){
                if (line.contains("</doc>"))
                    break;
                if (!line.contains("<doc")){
                    output += line + newline;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output;
    }
}
*/