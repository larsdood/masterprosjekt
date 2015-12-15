package com.holdaas.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Analyser {
	enum Status {PASS, FAIL}
	private static Set<String> keywords = new HashSet<String>(), suffixes = new HashSet<String>(), finalTokens = new HashSet<String>();
	private Map<String, Float> botPersonMap = new HashMap<String, Float>(), botNonpersonMap = new HashMap<String, Float>();
	private Article article;
	boolean printOutput = false;

	public Analyser(String bagoftokensPath){
		try{
			keywordsSetup();
			suffixesSetup();
			finalTokenSetup();
			bagoftokensSetup(bagoftokensPath);
		}
		catch(IOException e){
			System.exit(0);
		}

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
		if (printOutput) System.out.println("--= Article Analysis　Passed =--");
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
	public Status titleAnalysis(){
		return titleAnalysis(false);
	}
	public Status titleAnalysis(boolean printOutput)
	{
		this.printOutput = printOutput;
		if (titleLengthAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleCharactersetAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleKeywordAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleSuffixAnalysis()==Status.FAIL)
			return Status.FAIL;
		if (titleDateAnalysis()==Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}
	

	public Status titleCharactersetAnalysis(){
		boolean containsKanjiHiragana = false, containsIllegalCharacter=false;

		for (char c : article.getHead().toCharArray()){
			int cnum = (int)c;
			if (cnum>(Integer.parseInt("4e00", 16)) || cnum>12353 && cnum<12436)
				containsKanjiHiragana = true;
			if (c=='!')
				containsIllegalCharacter = true;
		}
		if (!containsKanjiHiragana || containsIllegalCharacter) {
			if (printOutput) System.out.println("Characterset fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	public Status titleLengthAnalysis(){
		if (article.getHead().length()-1>1 && article.getHead().length()-1<15)
			return Status.PASS;
		if (printOutput) System.out.println("Title length fail: " + (article.getHead().length()-1));
		return Status.FAIL;
	}
	public Status titleKeywordAnalysis(){
		String head = article.getHead();
		for (String word : keywords){
			if (head.contains(word)){
				if (printOutput) System.out.println("Keyword fail: " + word);
				return Status.FAIL;
			}
		}
		return Status.PASS;
	}
	public Status titleSuffixAnalysis(){
		String head = article.getHead();
		for (String word: suffixes){
			if (word.length()< head.length()) {
				if (head.substring(head.length() - word.length() - 1).contains(word)) {
					if (printOutput) System.out.println("Suffix fail: " + word);
					return Status.FAIL;
				}
			}
		}
		return Status.PASS;

	}
	public Status titleDateAnalysis(){
		String head = article.getHead();
		if (containsNumber(head) && (head.contains("年") || head.contains("月") || head.contains("日"))){
			if (printOutput) System.out.println("Title is date fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}

	public boolean containsNumber(String s){
		char[] chars = s.toCharArray();
		for(char c: chars){
			if (c>48 && c<58)
				return true;
		}
		return false;
	}
	
	/*
	 * BODY TESTS
	 */
	
	private Status bodyAnalysis() {
		if (emptyBodyAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (noFullStopAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (englishTranslationAnalysis() == Status.FAIL)
			return Status.FAIL;
		if (otherTranslationAnalysis() == Status.FAIL)
			return Status.FAIL;
		return Status.PASS;
	}

	private Status noFullStopAnalysis() {
		if (!article.getBody().contains("。")){
			if (printOutput) System.out.println("No full stop fail");
			return Status.FAIL;
		}
		return Status.PASS;

	}

	private Status emptyBodyAnalysis() {
		if (article.getBody().replace(System.lineSeparator(),"").equals("")) {
			if (printOutput) System.out.println("Empty body fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	private Status englishTranslationAnalysis() {
		if (article.getBody().contains("英：") || article.getBody().contains("英:")) {
			if (printOutput) System.out.println("English translation fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	private Status otherTranslationAnalysis() {
		if (article.getBody().contains("語：") || article.getBody().contains("語:")) {
			if (printOutput) System.out.println("Other language translation fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}
	/*
	 * TOKENIZED TESTS
	 */
	
	public Status analyseTokens(List<TokenPair> tokenPairs) {
		if (primarySentenceAnalysis(tokenPairsToStringArray(tokenPairs)) == Status.FAIL)
			return Status.FAIL;
		//if (bagOfTokensAnalysis(tokenPairs) == Status.FAIL)
		//		return Status.FAIL;
		return Status.PASS;
	}
	
	private Status primarySentenceAnalysis(String[] firstSentence) {
		if (firstSentence.length<2)
			return Status.FAIL;
		if (finalTokenAnalysis(firstSentence) == Status.FAIL){
			if (printOutput) System.out.println("Final token analysis fail");
			return Status.FAIL;
		}
		if (towaAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Towa token analysis fail");
			return Status.FAIL;
		}
		if (niaruAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Niaru token analysis fail");
			return Status.FAIL;
		}
		if (niattaAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Niatta token analysis fail");
			return Status.FAIL;
		}
		if (paranthesisRomajiKatakanaAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Paranthesis Romaji/Katakana analysis fail");
			return Status.FAIL;
		}
		if (workTitleAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Work title analysis fail");
			return Status.FAIL;
		}
		if (chineseKoreanAnalysis(firstSentence) == Status.FAIL) {
			if (printOutput) System.out.println("Chinese/Korean analysis fail");
			return Status.FAIL;
		}
		return Status.PASS;
	}

	private Status chineseKoreanAnalysis(String[] firstSentence) {
		for (int i = 0; i < firstSentence.length;i++){
			if (firstSentence[i].equals("中国") || firstSentence[i].equals("韓国"))
				return Status.FAIL;
		}
		return Status.PASS;
	}

	private Status towaAnalysis(String[] firstSentence) {
		for (int i =0; i < firstSentence.length;i++){
			if (firstSentence[i].equals("と"))
				if (firstSentence[i+1].equals("は")){
					return Status.FAIL;
				}
		}
		return Status.PASS;
	}

	private Status niaruAnalysis(String[] firstSentence) {
		for (int i =0; i < firstSentence.length;i++){
			if (firstSentence[i].equals("に"))
				if (firstSentence[i+1].equals("ある")){
					return Status.FAIL;
				}
		}
		return Status.PASS;
	}

	private Status niattaAnalysis(String[] firstSentence) {
		for (int i =0; i < firstSentence.length;i++){
			if (firstSentence[i].equals("に"))
				if (firstSentence[i+1].equals("あっ")){
					if (firstSentence[i+2].equals("た")){
						return Status.FAIL;
					}
				}
		}
		return Status.PASS;
	}

	private Status paranthesisRomajiKatakanaAnalysis(String[] firstSentence) {
		boolean firstParanthesis = false;
		for (int i = 0; i < firstSentence.length; i++) {
			if (firstSentence[i].contains("（") || firstSentence[i].contains("(")) {
				firstParanthesis = true;
			}
			if (firstParanthesis){
				if (firstSentence[i].contains("）") || firstSentence[i].contains(")")) {
					return Status.PASS;
				}
				if (firstSentence[i].matches("[a-zA-Z]+") || firstSentence[i].charAt(0) > 12449 && firstSentence[i].charAt(0) < 12539){
					return Status.FAIL;
				}

			}
		}
		return Status.PASS;
	}

	private Status workTitleAnalysis(String[] firstSentence){
		if (firstSentence[0].equals("『"))
			return Status.FAIL;
		return Status.PASS;
	}


	private Status finalTokenAnalysis(String[] firstSentence) {
		String tokenToCheck = firstSentence[firstSentence.length - 2];
		if (tokenToCheck.equals("ある"))
			tokenToCheck = firstSentence[firstSentence.length - 4];

		for (String word : finalTokens) {
			if (tokenToCheck.contains(word)) {
				if (printOutput) System.out.println("Final token fail: " + word);
				return Status.FAIL;
			}
		}
		return Status.PASS;
	}

	Status bagOfTokensAnalysis(List<TokenPair> tokenPairs) {
		float value = 0.5f;
		for (TokenPair tokenPair : tokenPairs){
			if (botPersonMap.containsKey(tokenPair.getWord()) && !botNonpersonMap.containsKey(tokenPair.getWord())){
				value += botPersonMap.get(tokenPair.getWord());
			}
			if (botNonpersonMap.containsKey(tokenPair.getWord()) && !botPersonMap.containsKey(tokenPair.getWord())){
				value -= botNonpersonMap.get(tokenPair.getWord());
			}
		}
		if (value>0.3f){
			System.out.println("Bag of Tokens Analysis: Yes - " + value);
			return Status.PASS;
		}
		else {
			System.out.println("Bag of Tokens Analysis: No - " + value);
			return Status.FAIL;
		}
	}


	private String[] tokenPairsToStringArray(List<TokenPair> tokenPairs) {
		String[] output = new String[tokenPairs.size()];
		for(int i = 0; i<output.length; i++){
			output[i] = tokenPairs.get(i).getWord();
		}
		return output;
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
	 * keywordsSetup list initialization (from text file titlekeywords.txt)
	 */
	void keywordsSetup() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("src/titlekeywords.txt"));
		String line;
		while ((line = br.readLine()) != null){
			keywords.add(line);
		}
	}

	private void suffixesSetup() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("src/titlesuffixkeywords.txt"));
		String line;
		while ((line = br.readLine()) != null){
			suffixes.add(line);
		}
	}

	private void finalTokenSetup() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("src/finaltokenkeywords.txt"));
		String line;
		while ((line = br.readLine()) != null){
			finalTokens.add(line);
		}
	}

	void bagoftokensSetup(String bagoftokensPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(bagoftokensPath + "/personsBoT.txt"));
		String line;
		while((line = br.readLine()) != null){
			botPersonMap.put(line, Float.parseFloat(br.readLine()));
		}
		br = new BufferedReader(new FileReader(bagoftokensPath + "/nonpersonsBoT.txt"));
		while((line = br.readLine()) != null){
			botNonpersonMap.put(line, Float.parseFloat(br.readLine()));
		}
		if (printOutput) System.out.println(botPersonMap);
		if (printOutput) System.out.println(botNonpersonMap);
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