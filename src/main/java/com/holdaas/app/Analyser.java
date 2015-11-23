package com.holdaas.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Analyser {
	enum Status {PASS, FAIL}
	static Set<String> keywords = new HashSet<String>(), suffixes = new HashSet<String>();
	Article article;

	public Analyser(){
		keywords();
		suffixes();
	}
	
	public Status analysePath(String path){
		article = generateArticle(ReadFromFile(path));
		if (articleAnalysis()==Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	public Status analyseArticleString(String articlestring){
		article = generateArticle(articlestring);
		if (articleAnalysis()==Status.FAIL)
			return Status.FAIL;
		System.out.println("--= Article Analysis　Passed =--");
		return Status.PASS;
	}

	public Status articleAnalysis(){
		if (titleAnalysis() == Status.FAIL || bodyAnalysis() == Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	
	/*
	 * TITLE TESTS
	 */
	
	public Status titleAnalysis()
	{
		if (titleLengthAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleCharactersetAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleKeywordAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleSuffixAnalysis()==Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	

	public Status titleCharactersetAnalysis(){
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
	public Status titleLengthAnalysis(){
		if (article.getHead().length()-1>1 && article.getHead().length()-1<15)
			return Status.PASS;
		System.out.println("Title length fail: " + (article.getHead().length()-1));
		return Status.FAIL;
	}
	public Status titleKeywordAnalysis(){
		String head = article.getHead();
		for (String word : keywords){
			if (head.contains(word)){
				System.out.println("Keyword fail: " + word);
				return Status.FAIL;
			}
		}
		return Status.PASS;
	}
	public Status titleSuffixAnalysis(){
		String head = article.getHead();
		for (String word: suffixes){
			if (head.substring(word.length() - word.length()).contains(word)){
				System.out.println("Suffix fail: " + word);
				return Status.FAIL;
			}
		}
		return Status.PASS;
	}
	
	/*
	 * BODY TESTS
	 */
	
	private Status bodyAnalysis() {
		if (emptyBodyAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (englishTranslationAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (otherTranslationAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (otherTranslationAnalysis() == Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	private Status emptyBodyAnalysis() {
		if (article.getBody().equals("")) {
			System.out.println("Empty body fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	private Status englishTranslationAnalysis() {
		if (article.getBody().contains("英：") || article.getBody().contains("英:")) {
			System.out.println("English translation fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	private Status otherTranslationAnalysis() {
		if (article.getBody().contains("語：") || article.getBody().contains("語:")) {
			System.out.println("Other language translation fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	/*
	 * TOKENIZED TESTS
	 */
	
	public Status analyseTokens(List<TokenPair> tokenPairs) {
		String[] firstSentence = getPrimarySentenceWords(tokenPairs);
		if (primarySentenceAnalysis(firstSentence) == Status.FAIL)
			return Status.FAIL;
		if (BagOfWordsAnalysis(tokenPairs) == Status.FAIL)
				return Status.FAIL;
		return Status.PASS;
	}
	
	private Status primarySentenceAnalysis(String[] firstSentence) {
		return Status.PASS;
	}

	private Status BagOfWordsAnalysis(List<TokenPair> tokenPairs) {
		BagOfTokens bag = new BagOfTokens();
		for (TokenPair tokenPair: tokenPairs){
			
			bag.addSingle(tokenPair.getWord());
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
			if (tokenPair.getTokens()[0].equals("記号") && !record){
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

	private List<TokenPair> getPrimarySentenceTokens(List<TokenPair> tokenPairs){
		int periodPosition = 0;

		int start=0,end=0,count=0;
		boolean record = false;

		for(TokenPair tokenPair : tokenPairs){
			if (record){
				if (tokenPair.getWord().equals("。")){
					count++;
					end = count;
					break;
				}
			}
			if (tokenPair.getTokens()[0].equals("記号") && !record){
				record = true;
				start=count;
				count=0;
			}
			count++;
		}
		List<TokenPair> output = new ArrayList<TokenPair>();
		for (int i = start; i<end;i++){
			output.add(i-start, tokenPairs.get(i));
			System.out.println(output.get(i-start));
		}
		return output;
	}

	/*
	 * keywords list initialization (from text file titlekeywords.txt)
	 */
	void keywords(){
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

	/* TODO: Under testing med training set 1 ga å legge til ordene i KEYWORDS istedetfor SUFFIXES
	* TODO: precision på 80% istedetfor 64%. Vurder om ordene burde ligge i titlekeywords istedet.
	* TODO: Hvis false negatives ikke går opp for noen training set/test set så kan det ligge noe i det. */
	void suffixes(){
		try (BufferedReader br = new BufferedReader(new FileReader("src/titlesuffixkeywords.txt"))){
			String line;
			while ((line = br.readLine()) != null){
				suffixes.add(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Can't find list of keywords for title analysis");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Article generateArticle(String articlestring) {
		String head = articlestring.split("\n")[0];
		String body = "";
		try{
			body = articlestring.substring(head.length()+3);
		}
		catch(Exception e){
		}

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