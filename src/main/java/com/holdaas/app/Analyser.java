package com.holdaas.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Analyser {
	enum Status {PASS, FAIL};
	static Set<String> keywords = new HashSet<String>();
	String path, articlestring;
	Article article;
	/*public static void main(String[] args) throws IOException{
		Keywords();
		//System.out.println(keywords);
		//System.out.println(ReadFromFile("D:/Japanese Wikipedia/output/AA/wiki_00/article.txt"));
		//System.out.println(ReadFromFile("D:/Japanese Wikipedia/output/AA/wiki_00/tokenized.txt"));
		
		//System.out.println(AnalyseThisPath("D:/Japanese Wikipedia/output/AA/wiki_00/article.txt"));
	}*/
	
	public Analyser(String path){
		Keywords();
		this.path = path;
	}
	
	public Status AnalysePath(){
		article = GenerateArticle(ReadFromFile(path));
		System.out.print(article);
		if (ArticleAnalysis()==Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
		/*String tokenizedstring = ReadFromFile(path);
		Tokenized tokenized = GenerateTokenized(tokenizedstring);
		return (TokenizedAnalysis(tokenizedstring));*/
	}
	public Status AnalyseArticleString(String articlestring){
		Keywords();
		article = GenerateArticle(articlestring);
		System.out.println(article.getHead());
		if (ArticleAnalysis()==Status.FAIL)
			return Status.FAIL;
		System.out.println("--= Article Analysis　Passed =--");
		return Status.PASS;
	}

	public Status ArticleAnalysis(){
		if (TitleAnalysis() == Status.FAIL || BodyAnalysis() == Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	
	/*
	 * TITLE TESTS
	 */
	
	public Status TitleAnalysis()
	{
		if (TitleCharactersetAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (TitleLengthAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (TitleKeywordAnalysis()==Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	

	public Status TitleCharactersetAnalysis(){
		boolean containsKanji = false;
		for (char c : article.getHead().toCharArray()){
			if ((int)c>(Integer.parseInt("4e00", 16)))
				containsKanji = true;
		}
		if (!containsKanji) {
			System.out.println("Characterset fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	public Status TitleLengthAnalysis(){
		if (article.getHead().length()-1>1 && article.getHead().length()-1<8)
			return Status.PASS;
		System.out.println("Title length fail: " + (article.getHead().length()-1));
		return Status.FAIL;
	}
	public Status TitleKeywordAnalysis(){
		for (String word : keywords){
			if (article.getHead().contains(word)){
				System.out.println("Keyword fail: " + word);
				return Status.FAIL;
			}
		}
		return Status.PASS;
	}
	
	/*
	 * BODY TESTS
	 */
	
	private Status BodyAnalysis() {
		return Status.PASS;
	}
	
	/*
	 * TOKENIZED TESTS
	 */
	
	public Status AnalyseTokens(List<TokenPair> tokenPairs) {
		String[] firstSentence = getPrimarySentenceWords(tokenPairs);
		if (PrimarySentenceAnalysis(firstSentence) == Status.FAIL)
			return Status.FAIL;
		if (BagOfWordsAnalysis(tokenPairs) == Status.FAIL)
				return Status.FAIL;
		System.out.println(firstSentence.length);
		return Status.PASS;
	}
	
	private Status PrimarySentenceAnalysis(String[] firstSentence) {
		return Status.PASS;
	}

	private Status BagOfWordsAnalysis(List<TokenPair> tokenPairs) {
		BagOfTokens bag = new BagOfTokens();
		for (TokenPair tokenPair: tokenPairs){
			
			bag.add(tokenPair.getWord());
		}
		System.out.println("Bag of words:\n" + bag);
		return Status.PASS;
	}

	private String[] getPrimarySentenceWords(List<TokenPair> tokenPairs) {
		int periodPosition = 0;
		int start=0, end=0, count=0;
		boolean record = false;
		for(TokenPair tokenPair : tokenPairs){
			if (record){
				if (tokenPair.getWord().equals("。")){
					count++;
					end = count;
					break;
				}
			}
			if (tokenPair.getTokens()[0].equals("記号") && record == false){
				record = true;
				start=count;
				count=0;
			}
			count++;
		}
		String[] output = new String[end-start];
		for(int i = start; i<end; i++){
			output[i-start] = tokenPairs.get(i+1).getWord();
			System.out.println(output[i-start]);
		}
		return output;
	}

	/*
	 * Keywords list initialization (from text file titlekeywords.txt)
	 */
	void Keywords(){
		try (BufferedReader br = new BufferedReader(new FileReader("src/titlekeywords.txt"))){
			String line;
			while ((line = br.readLine()) != null){
				keywords.add(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Can't find list of keywords for title analysis");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Article GenerateArticle(String articlestring) {
		String head = articlestring.split("\n")[0];
		String body = articlestring.substring(head.length()+3);
		return new Article(head, body);
	}
	
	public static String ReadFromFile(String path){
		File file = new File(path);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			return new String(data, "UTF-8");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String getHead() {
		return article.getHead();
	}
	public String getBody() {
		return article.getBody();
	}
}
