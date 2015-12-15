package com.holdaas.app;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//import org.atilika.kuromoji.Token;
//import org.atilika.kuromoji.Tokenizer;
//import org.atilika.kuromoji.Tokenizer.Mode;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

public class MasterProgram {
	Tokenizer tokenizer;

	static String helpmain = "oh please help", helpgen = "puriisu herupu";

	String basefolder;

	String datafilesPath;
	String tokensPath;
	String outputPath;
	String testsetPath;
	String bagoftokensPath;
	String extractPath;
	Analyser analyser;

	public MasterProgram(){
		tokenizer = new Tokenizer();

		basefolder = "D:/Japanese Wikipedia/";
		datafilesPath = basefolder + "target/";
		tokensPath = basefolder + "tokens2/";
		outputPath = basefolder + "output4/";
		testsetPath = basefolder + "testset/";
		bagoftokensPath = basefolder + "bagoftokens/";
		extractPath = basefolder + "extracted/";

		analyser = new Analyser(bagoftokensPath);
	}

	public static void main(String[] args) throws IOException{
		MasterProgram masterProgram = new MasterProgram();
		if (args[0].equals("")) {
			System.out.println("Invalid argument. -h for help");
			System.exit(0);
		}
		if (args[0].equals("-h")){
			if (args.length==1){
				System.out.println(helpmain);
				System.exit(0);
			}
			switch(args[1]){
				case "gen":
					System.out.println(helpgen);
					break;
			}
		}
		masterProgram.run(args);
	}

	public String run(String[] arg) throws IOException{
		switch(arg[0]){
			case "extractPersonArticles":
				try{
					extractPersonArticles(Integer.parseInt(arg[1]));
				}
				catch(Exception e){
					System.out.println("Invalid input. See -h extract");
				}
				extractPersonArticles(Integer.parseInt(arg[1]));
				break;
			case "-gen":
				/**
				 * -gen: Generates article and tokenized files from the target path
				 * @args[1] int range: Indicates the range for articles; Method will generate from 1 to range articles
				 * per target file.
				 */
				try{
					generate2(Integer.parseInt(arg[1]));
				}
				catch(Exception e){
					System.out.println("Invalid input. See -h gen");
				}
				break;
			case "generateSingleArticleSet":
				generateSingleArticleSet(2);
				break;
			case "tokenize":
				tokenizeArticles(2);
				break;
			case "populateBag":
				populateBagOfTokens();
				break;
			case "countArticles":
				countAllArticles();
				break;
			case "extractTestset":
				extractTestset();
				break;
			case "trainingSetTest":
				trainingSetTest(3);
				break;
			case "bagoftokensTest":
				botTest();
				break;
			case "fScoreTest":
				fScoreTest();
				break;
			case "testFetch":
				testFetch();
				break;

			default:
				System.out.println("invalid argument: " + arg);
				System.exit(0);
		}
		return "ok";
	}

	private void extractPersonArticles(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(extractPath);
		newdir.mkdirs();
		String articleStr = "";
		List<TokenPair> tokenPairs;
		List<String> articleList = new ArrayList<String>();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for (File file : files){
				for (int i = 0; i < range; i++){
					articleStr = fetchNthArticle(file, i);
					if (analyser.analyseArticleString(articleStr) == Analyser.Status.PASS){
						tokenPairs = tokenizeFirstSentenceToPairList(articleStr);
						if (analyser.analyseTokens(tokenPairs) == Analyser.Status.PASS){
							articleList.add(getFirstSentence(articleStr));
						}
					}
				}
			}
		}
		List<Person> personList = new ArrayList<Person>();
		for(String entry : articleList){
			Person person = new Person(entry);
			if (!person.isNull())
				personList.add(person);

		}
		for (Person entry : personList){
		}

		writer = new PrintWriter(extractPath + "/" + "bigfile" + ".txt", "UTF-8");
		articleList.forEach(writer::println);
		writer.close();

		writer = new PrintWriter(extractPath + "/" + "personFile" + ".txt", "UTF-8");
		personList.forEach(writer::println);
		writer.close();
	}

	private void generateSingleArticleSet(int range) throws IOException{
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(outputPath);
		newdir.mkdirs();
		String articletext;
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				writer = new PrintWriter(outputPath + "/" + counter + "_article" + ".txt", "UTF-8");
				articletext = fetchNthArticle(file, range);
				writer.println(articletext);
				;
				writer.close();
				System.out.println("Article #" + counter + ": " + Article.generateArticle(articletext).getHead());
				counter++;
			}
		}
	}

	private void botTest() throws IOException {
		/* TODO: Trenger jeg denne? */
	}

	private void trainingSetTest(int n) throws IOException{
		List<Integer> personIndex = personIndexToList("src/" + "isperson" + n + ".txt", 2);
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;
		File[] directories = new File(outputPath).listFiles(File::isDirectory);
		for (int i = 0;i<directories.length;i++){
			File article = new File(outputPath + "/File" + i + "/" + (n) + "_article.txt");
			isPerson = (personIndex.contains((Integer)i));
			analyserJudgement = analyser.analysePath(article.getAbsolutePath()) == Analyser.Status.PASS &&
					analyser.analyseTokens(readTokensFile(new File(outputPath + "File" + i + "/" + (n) + "_tokenized.txt")) ) == Analyser.Status.PASS;
			if (isPerson)
				if (analyserJudgement)
					truePositives++;
				else{
					falseNegatives++;
					System.out.println("Article #" + i + ": FALSE NEGATIVE");
				}

			else
			if (analyserJudgement){
				falsePositives++;
				System.out.println("Article #" + i + ": FALSE POSITIVE");
			}

			else
				trueNegatives++;
			System.out.println("Article #" + i + ", Person index: " + isPerson + ", Analyser: " + analyserJudgement);
			if (isPerson && !analyserJudgement)
				System.out.println(i);
		}
		System.out.println("True Positives: " + truePositives + ", False Negatives: " + falseNegatives +
				"\nFalse Positives: " + falsePositives + ", True Negatives: " + trueNegatives);
		double precision = ((double)truePositives/ (truePositives + falsePositives));
		double recall = ((double)truePositives / (truePositives + falseNegatives));
		System.out.println("Presicion: " + String.format("%.2f", precision * 100) + "%, Recall: " + String.format("%.2f", recall * 100) + "%.");
		System.out.println("F-score: " + String.format("%.2f",(2*((precision*recall)/(precision+recall)))));
	}

	private void generate2(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(outputPath);
		File filedir;
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				filedir = new File(outputPath + "/File" + counter + "/");
				filedir.mkdirs();

				for (int i=0;i<range;i++){
					writer = new PrintWriter(filedir + "/" + (i+1) + "_article" + ".txt", "UTF-8");
					writer.println(fetchNthArticle(file, i));
					writer.close();
					writer = new PrintWriter(filedir + "/" + (i+1) + "_tokenized" + ".txt", "UTF-8");
					writer.println(tokenizeFirstSentence(fetchNthArticle(file, i)));
					writer.close();
				}
				counter++;
			}
		}
	}

	public String run() throws IOException{
		return "No valid input argument";
	}

	/* 49th article of each file */
	private void extractTestset() throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(testsetPath);
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();

			for(File file : files){
				System.out.print("Article number: " + counter);

				String output = fetchNthArticle(file, 48);
				writer = new PrintWriter(newdir + "/" + counter + "_" +  "article.txt" , "UTF-8");
				writer.println(output.trim());
				writer.close();

				String tokenized = tokenizeFirstSentence(output);
				writer = new PrintWriter(newdir + "/" + counter + "_"  + "tokenized.txt", "UTF-8");
				writer.println(tokenized);
				writer.close();

				counter++;
			}
		}
	}

	/* TODO: Skriv om dette i oppgaven. Relevant å beskrive datasettet jeg startet med og hvordan det er konstruert. */
	private void countAllArticles() throws IOException {
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		int fileCounter = 0, articlesCounter = 0, tempArticles=0, max=100, min=100;
		for (File directory: directories){
			File[] files = new File(directory.getPath()).listFiles();
			for (File file : files){
				tempArticles = countArticles(file);
				if (tempArticles>max)
					max = tempArticles;
				if (tempArticles<min)
					min = tempArticles;
				System.out.println("File #" + fileCounter + ", # of articles: " + tempArticles);
				articlesCounter+=tempArticles;
				fileCounter++;
			}
		}
		System.out.println("# of files: " + fileCounter + ", # of articles: " + articlesCounter + ", avg. # of articles per file: " + (double)articlesCounter/(double)fileCounter);
		System.out.println("Lowest # of articles in file: " + min + ", highest # of articles in file: " + max);

	}

	/* TODO: Gjør lasting av personindex til en generell statisk metode */
	private void fScoreTest(int n, boolean testBagOnly) throws IOException {
		List<Integer> personIndex = personIndexToList(testsetPath + "isperson.txt", 2);
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;

		for (int i=0;i<n;i++){
			File article = new File(testsetPath + i + "_article.txt");
			File tokenized = new File(testsetPath + i + "_tokenized.txt");
			//System.out.println("Article #" + i);
			//System.out.println(analyser.analysePath(article.getAbsolutePath()));
			isPerson = (personIndex.contains((Integer)i));
			if (!testBagOnly) analyserJudgement = analyser.analysePath(article.getAbsolutePath()) == Analyser.Status.PASS &&
					analyser.analyseTokens(readTokensFile(new File(tokenized.getAbsolutePath()))) == Analyser.Status.PASS;
			else	analyserJudgement = analyser.bagOfTokensAnalysis(readTokensFile(new File(tokenized.getAbsolutePath()))) == Analyser.Status.PASS;
			if (isPerson)
				if (analyserJudgement)
					truePositives++;
				else
					falseNegatives++;
			else
				if (analyserJudgement)
					falsePositives++;
				else
					trueNegatives++;
			System.out.println("Article #" + i + ", Person index: " + isPerson + ", Analyser: " + analyserJudgement);
			if (isPerson && !analyserJudgement)
				System.out.println(i);
		}

		System.out.println("True Positives: " + truePositives + ", False Negatives: " + falseNegatives +
				"\nFalse Positives: " + falsePositives + ", True Negatives: " + trueNegatives);
		double precision = ((double)truePositives/ (truePositives + falsePositives));
		double recall = ((double)truePositives / (truePositives + falseNegatives));
		System.out.println("Presicion: " + String.format("%.2f", precision * 100) + "%, Recall: " + String.format("%.2f", recall * 100) + "%.");
		System.out.println("F-score: " + String.format("%.2f",(2*((precision*recall)/(precision+recall)))));

	}
	private void fScoreTest() throws IOException {
		fScoreTest(378, true);
	}

	public void populateBagOfTokens() throws IOException {
		BagPopulator populator = new BagPopulator();
		File[] directories = new File(outputPath).listFiles(File::isDirectory);

		List<Integer> personindex1 = personIndexToList("src/isperson1.txt", 2),
		personindex2 = personIndexToList("src/isperson2.txt", 2),
		personindex3 = personIndexToList("src/isperson3.txt", 2);
		BagOfTokens tempbag1, tempbag2, tempbag3;
		List<TokenPair> tokenPairList1, tokenPairList2, tokenPairList3;
		int counter =0;
		for (File subdir : directories){
			tempbag1 = new BagOfTokens();
			tempbag2 = new BagOfTokens();
			tempbag3 = new BagOfTokens();
			tokenPairList1 = readTokensFile(new File(subdir.getAbsolutePath() + "/1_tokenized.txt"));
			tokenPairList2 = readTokensFile(new File(subdir.getAbsolutePath() + "/2_tokenized.txt"));
			tokenPairList3 = readTokensFile(new File(subdir.getAbsolutePath() + "/3_tokenized.txt"));
			tempbag1.addSet(tokenPairList1);
			tempbag2.addSet(tokenPairList2);
			tempbag3.addSet(tokenPairList3);

			populator.add(tempbag1, personindex1.contains((Integer) counter));
			populator.add(tempbag2, personindex2.contains((Integer)counter));
			populator.add(tempbag3, personindex3.contains((Integer) counter));
			counter++;
		}
		System.out.println(populator);
		populator.calculatePoints();
		populator.writeToFile(bagoftokensPath);
	}

	public void tokenizeArticles(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(tokensPath);
		File filedir;
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				filedir = new File(tokensPath + "/File" + counter + "/");
				filedir.mkdirs();

				for (int i=0;i<range;i++){
					writer = new PrintWriter(filedir + "/tokenized" + i + ".txt", "UTF-8");
					//System.out.println(tokenizeFirstSentence(fetchFirstArticle(file), tokenizer));
					//writer.println(tokenizeFirstSentence(fetchFirstArticle(file), tokenizer));
					writer.println(tokenizeFirstSentence(fetchNthArticle(file, i)));
					writer.close();
				}
				counter++;
			}
		}
	}
	/*
	public void generateRelevantFiles() throws IOException{
		PrintWriter writer;
		int counter=0, successcounter=0;
		long timerstart, timerstop;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(outputPath);
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				
				String output = fetchFirstArticle(file);
				System.out.print("Article number: " + counter + ", "); 
				if (analyser.analyseArticleString(output)==Analyser.Status.PASS){
					List<TokenPair> tokenPairs = tokenizeToPairList(output);
					
					if(analyser.analyseTokens(tokenPairs) == Analyser.Status.PASS){
						timerstart = System.currentTimeMillis();
						System.out.println(file.getName());
						writer = new PrintWriter(newdir + "/" + counter + "_" +  " article.txt" , "UTF-8");
						writer.println(output.trim());
						writer.close();
						String tokenized = tokenize(output);

						writer = new PrintWriter(newdir + "/" + counter + "_"  + " tokenized.txt", "UTF-8");
						
						writer.println(tokenized);
						
						writer.close();
						timerstop = System.currentTimeMillis();
						System.out.println("Files generated, tokenized, in " + (timerstop-timerstart) + " ms.");
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
	*/
	public void generateAllFiles() throws Exception{
		PrintWriter writer;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		for (File directory : directories){
			File newdir = new File(outputPath + directory.getName());
			newdir.mkdirs();
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				File filedir = new File(newdir + "/" + file.getName() + "/");
				filedir.mkdirs();
				
				writer = new PrintWriter(filedir + "/article.txt" , "UTF-8");
				String output = fetchFirstArticle(file);
				writer.println(output);
				
				writer.close();
				writer = new PrintWriter(filedir + "/tokenized.txt", "UTF-8");
				writer.println(tokenize(output));
				writer.close();
			}
		}
	}
	
	public String tokenize(File file){
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
		return tokenize(output);
	}
	
	public String tokenize(String input)
	{
		input = input.replace("\n", "");
		input = input.replace(" ", "");
		
		List<Token> result = tokenizer.tokenize(input);
		String output = "";
		
		for(Token token : result){
			output += token.getSurface() + "\t" + token.getAllFeatures() + System.getProperty("line.separator");
		}
		output.replace("記号,一般,*,*,*,*,*", "");
		output.replace("記号,空白,*,*,*,*,*", "");
		return output;
	}

	private String getFirstSentence(String s) {
		String input;
		//String input = s.substring(s.charAt("\n"), s.charAt("。"));
		if (s.contains("。")) {
			input = s.substring(s.indexOf(("\n")) + 2);
			input = input.substring(0, input.indexOf("。") + 1);
			//input = s.substring(s.indexOf("\n") + 2, s.indexOf("。") + 1);
			input = input.replace("\n", "");
			//input = input.replace(" ", "");
		}
		else{
			input = s.substring(s.indexOf("\n"));
			input = input.replace("\n", "");
		}
		return input;
	}

	private String tokenizeFirstSentence(String s) {

		/* TODO: Sjekke om første punktum ligger inne i en parantes, som f.eks. FIL 213, Artikkel 0. */
		String input;
		//String input = s.substring(s.charAt("\n"), s.charAt("。"));
		if (s.contains("。")) {
			input = s.substring(s.indexOf(("\n")) + 2);
			input = input.substring(0, input.indexOf("。") + 1);
			//input = s.substring(s.indexOf("\n") + 2, s.indexOf("。") + 1);
			input = input.replace("\n", "");
			//input = input.replace(" ", "");
		}
		else{
			input = s.substring(s.indexOf("\n"));
			input = input.replace("\n", "");
		}

		List<Token> result = tokenizer.tokenize(input);
		String output = "";

		for(Token token : result){
			output += token.getSurface() + "\t" + token.getAllFeatures() + System.getProperty("line.separator");
		}
		output.replace("記号,一般,*,*,*,*,*", "");
		output.replace("記号,空白,*,*,*,*,*", "");
		return output;
	}
	private List<TokenPair> tokenizeFirstSentenceToPairList(String s) {
		String input;
		//String input = s.substring(s.charAt("\n"), s.charAt("。"));
		if (s.contains("。")) {
			input = s.substring(s.indexOf(("\n")) + 2);
			input = input.substring(0, input.indexOf("。") + 1);
			input = input.replace("\n", "");
		}
		else{
			input = s.substring(s.indexOf("\n"));
			input = input.replace("\n", "");
		}

		List<Token> result = tokenizer.tokenize(input);

		List<TokenPair> output = new ArrayList<TokenPair>();

		for(Token token : result){
			output.add(new TokenPair(token.getSurface(), token.getAllFeaturesArray()));
		}
		return output;
	}
	
	public List<TokenPair> tokenizeToPairList(String input)
	{
		input = input.replace("\n", "");
		input = input.replace(" ", "");
		
		List<Token> result = tokenizer.tokenize(input);
		
		List<TokenPair> output = new ArrayList<TokenPair>();
		
		for(Token token : result){
			output.add(new TokenPair(token.getSurface(), token.getAllFeaturesArray()));
		}
		
		return output;
	}

	/* Static methods that are independent of the instance */
	public static String fetchFirstArticle(File file) throws IOException{
		return fetchNthArticle(file, 0);
	}
	public static String fetchNthArticle(File file, int n) throws IOException {
		String output = "";
		int counter = 0;
		boolean record = false;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String newline = System.getProperty("line.separator");
		while ((line = br.readLine() ) != null) {
			if (line.contains("</doc>") && record)
				break;
			else if (line.contains("<doc")) {
				if (counter == n)
					record = true;
				else
					counter++;
			} else if (record)
				output += line + newline;
			}
		return output;
	}
	public static List<TokenPair> readTokensFile(File file) throws IOException {
		String output = "";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		List<TokenPair> tokenPairs = new ArrayList<TokenPair>();
		while ((line = br.readLine() ) != null){
			if (line.contains("記号,一般,*,*,*,*,*,*,*") || line.equals(""))
				continue;
			String[] temp = line.split("\t");

			tokenPairs.add(new TokenPair(temp[0], temp[1].split(",")));
		}
		return tokenPairs;
	}
	public static int countArticles(File file) throws IOException{
		int counter = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine() ) != null) {
			if (line.contains("<doc"))
				counter++;
		}
		return counter;
	}

	/* Support class for setting up a person index object that contains information of which files in a folder
	* are articles about persons. */
	// 1 = ALL PERSONS
	// 2 = JAPANESE, KOREAN, CHINESE etc.
	// 3 = ONLY JAPANESE
	public static List<Integer> personIndexToList (String filepath, int type) throws IOException{
		List<Integer> personIndex = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line;
		switch(type){
			// CASE 1: All persons are added to personIndex
			case 1: while ((line = br.readLine()) != null) {
				line = line.replaceAll("\\D+","");
				personIndex.add(Integer.parseInt(line));
			}
				break;
			// CASE 2: All Chinese/Korean/Japanese/etc. persons are added to personIndex
			case 2: while ((line = br.readLine()) != null) {
				if (!line.contains("NJ")){
					line = line.replaceAll("\\D+","");
					personIndex.add(Integer.parseInt(line));
				}
			}
				break;
			// CASE 3: Japanese persons only are added to personIndex
			case 3: while ((line = br.readLine()) != null) {
				if (line.matches("\\d+"))
					personIndex.add(Integer.parseInt(line));
			}
				break;
		}
		return personIndex;
	}

	/* METHODS USED DURING TESTING OF DEVELOPMENT: CAN BE DELETED AT LATER TIME. NOT NECESSARY FOR FINAL PRODUCT. */
	private void testFetch() throws IOException {
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		int counter = 0;
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				System.out.println("NTH");
				System.out.println(fetchNthArticle(file, 1));
				System.out.println("FIRST");
				System.out.println(fetchFirstArticle(file));
				if (counter==0)
					return;
			}
		}
	}
}