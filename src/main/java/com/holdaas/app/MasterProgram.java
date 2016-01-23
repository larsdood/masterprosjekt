package com.holdaas.app;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

public class MasterProgram {
	Tokenizer tokenizer;

	static String helpmain = "-extractNames Creates a name database based on the input data \n" +
			"-extractPersons & Creates a person database based on the input data\n" +
			"-genArticleSets # & Generates a set of text files containing article \\\\&and tokenized texts from 0 to the range #\n" +
			"-genSingleArticleSet # & Generates text files for the #th article per input file\n" +
			"-populateBag & Populates the bag of tokens *\n" +
			"-countArticles & Outputs number of articles per input files\n" +
			"-extractTestSet & Extracts a test set based on the input data\n" +
			"-trainingSetTest & Tests the system accuracy against the training sets *\n" +
			"-fScoreTest & Tests the system accuracy against the test set *\n" +
			"-resetConf & Resets the configuration file to default";

	String basefolder;

	String datafilesPath,tokensPath,outputPath,testsetPath, bagoftokensPath, nameDBPath, personDBPath;
	Analyser analyser;
	DBHandler dbHandler;
	Properties prop;
	String confFileName;

	public MasterProgram(){
		tokenizer = new Tokenizer();
		prop = new Properties();
		confFileName="config.properties";
		getConfig();
		analyser = new Analyser(bagoftokensPath);
		dbHandler = new DBHandler(nameDBPath, personDBPath);
	}

	public static void main(String[] args) throws Exception{

		if (args[0].equals("")) {
			System.out.println("Error: No argument. -h for help");
			System.exit(0);
		}
		/* HELP */
		if (args[0].equals("-h")){
			System.out.println(helpmain);
			System.exit(0);
		}
		if (args[0].equals("-config")){
			switch(args[1]) {
				case "reset":
					resetConfig();
					System.exit(0);
					break;
			}
		}
		MasterProgram masterProgram = new MasterProgram();
		masterProgram.run(args);
	}

	public static void resetConfig(){
		Properties prop = new Properties();
		OutputStream output = null;

		try{
			output = new FileOutputStream("config.properties");

			prop.setProperty("basefolder", "D:/Japanese Wikipedia/");
			prop.setProperty("datafilesPath", "target/");
			prop.setProperty("tokensPath", "tokens/");
			prop.setProperty("outputPath", "output/");
			prop.setProperty("testsetPath", "testset/");
			prop.setProperty("bagoftokensPath", "bagoftokens/");
			prop.setProperty("nameDBPath", "C:/database/NameBase.db");
			prop.setProperty("personDBPath", "C:/database/PersonBase.db");

			prop.store(output, null);
			System.out.println("Config file reset. Please restart program.");
		}
		catch(Exception e){
			System.exit(1);
		}
		finally{
			try {
				if (output != null) output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void getConfig(){
		try{
			InputStream is = new FileInputStream(confFileName);

			prop.load(is);
		}
		catch(Exception e){
			System.out.println("Config file failed load");
			System.exit(0);
		}
		basefolder = prop.getProperty("basefolder");
		datafilesPath = basefolder + prop.getProperty("datafilesPath");
		tokensPath = basefolder + prop.getProperty("tokensPath");
		outputPath = basefolder + prop.getProperty("outputPath");
		testsetPath = basefolder + prop.getProperty("testsetPath");
		bagoftokensPath = basefolder + prop.getProperty("bagoftokensPath");
		nameDBPath = prop.getProperty("nameDBPath");
		personDBPath = prop.getProperty("personDBPath");
	}


	public String run(String[] arg) throws Exception{
		switch(arg[0]){
			case "-extractNames":
				extractNames();
				break;
			case "-extractPersons":
				extractPersonArticles();
				break;
			case "-genArticleSets":
				/**
				 * -gen: Generates article and tokenized files from the target path
				 * @args[1] int range: Indicates the range for articles; Method will generate from 1 to range articles
				 * per target file.
				 */
				try{
					generate2(Integer.parseInt(arg[1]));
				}
				catch(Exception e){
					System.out.println("Invalid input.");
				}
				break;

			case "-genSingleArticleSet":
				generateSingleArticleSet(Integer.parseInt(arg[1]));
				break;
			case "-populateBag":
				populateBagOfTokens();
				break;
			case "-countArticles":
				countAllArticles();
				break;
			case "-extractTestset":
				extractTestset();
				break;
			case "-trainingSetTest":
				trainingSetTest(3);
				break;
			case "-fScoreTest":
				fScoreTest();
				break;
			case "-resetConf":
				resetConf();
				break;

			default:
				System.out.println("invalid argument: " + Arrays.toString(arg));
				System.exit(0);
		}
		return "ok";
	}

	private void resetConf(){

	}

	private void extractNames() throws IOException {
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		List<TokenPair> tokenPairs;
		List<String> articleList = new ArrayList<>(), tempList;
		List<Name> nameList = new ArrayList<>();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for (File file : files){
				tempList = fetchAllArticles(file);
				for (String articleStr : tempList){
					if (analyser.analyseArticleString(articleStr) == Analyser.Status.PASS){
						tokenPairs = tokenizeFirstSentenceToPairList(articleStr);
						if (analyser.analyseTokens(tokenPairs) == Analyser.Status.PASS){
							articleList.add(getFirstSentence(articleStr));
						}
					}
				}
			}
		}
		Name tempName;
		for(String entry : articleList){
			Person person = new Person(entry);
			if (!person.isNull()){
				if (person.getFamilyKanji().matches("\\p{InCJKUnifiedIdeographs}+") && person.getFamilyHiragana().matches("\\p{InHiragana}+")){
					tempName = new Name(person.getFamilyKanji(), person.getFamilyHiragana());
					boolean uniqueName = true;

					for (Name name : nameList){
						if (Name.IdenticalNames(name, tempName)){
							uniqueName = false;
							break;
						}
					}
					if (uniqueName){
						nameList.add(tempName);
					}
				}

				if (person.getGivenKanji().matches("\\p{InCJKUnifiedIdeographs}+") && person.getGivenHiragana().matches("\\p{InHiragana}+")){
					tempName = new Name(person.getGivenKanji(), person.getGivenHiragana());
					boolean uniqueName = true;

					for (Name name : nameList){
						if (Name.IdenticalNames(name, tempName)){
							uniqueName = false;
							break;
						}
					}
					if (uniqueName){
						nameList.add(tempName);
					}
				}
			}

		}

		System.out.println(nameList);

		dbHandler.insertNameList(nameList);
	}

	private void extractPersonArticles() throws IOException {
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		List<TokenPair> tokenPairs;
		List<String> articleList = new ArrayList<>(), tempList;
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for (File file : files){
				tempList = fetchAllArticles(file);
				for (String articleStr : tempList){
					if (analyser.analyseArticleString(articleStr) == Analyser.Status.PASS){
						tokenPairs = tokenizeFirstSentenceToPairList(articleStr);
						if (analyser.analyseTokens(tokenPairs) == Analyser.Status.PASS){
							articleList.add(getFirstSentence(articleStr));
						}
					}
				}
			}
			List<Person> personList = new ArrayList<>();
			for(String entry : articleList){
				Person person = new Person(entry);
				if (!person.isNull())
					personList.add(person);

			}
			dbHandler.insertPersonList(personList);
		}
		List<Person> personList = new ArrayList<>();
		for(String entry : articleList){
			Person person = new Person(entry);
			if (!person.isNull())
				personList.add(person);

		}
		dbHandler.insertPersonList(personList);
	}

	private void generateSingleArticleSet(int range) throws IOException{
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(outputPath);
		//noinspection ResultOfMethodCallIgnored
		newdir.mkdirs();
		String articletext;
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for(File file : files){
				writer = new PrintWriter(outputPath + "/" + counter + "_article" + ".txt", "UTF-8");
				articletext = fetchNthArticle(file, range);
				writer.println(articletext);
				writer.close();
				System.out.println("Article #" + counter + ": " + Article.generateArticle(articletext).getHead());
				counter++;
			}
		}
	}

	private void trainingSetTest(int n) throws IOException{
		List<Integer> personIndex = personIndexToList("src/" + "isperson" + n + ".txt", 2);
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;
		File[] directories = new File(outputPath).listFiles(File::isDirectory);
		for (int i = 0;i<directories.length;i++){
			File article = new File(outputPath + "/File" + i + "/" + (n) + "_article.txt");
			isPerson = (personIndex.contains(i));
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
		System.out.println("F-score: " + String.format("%.2f", (2 * ((precision * recall) / (precision + recall)))));
	}

	private void generate2(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(outputPath);
		File filedir;
		//noinspection ResultOfMethodCallIgnored
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for(File file : files){
				filedir = new File(outputPath + "/File" + counter + "/");
				//noinspection ResultOfMethodCallIgnored
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

	/* 49th article of each file */
	private void extractTestset() throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		File newdir = new File(testsetPath);
		//noinspection ResultOfMethodCallIgnored
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
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

	private void countAllArticles() throws IOException {
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		int fileCounter = 0, articlesCounter = 0, tempArticles, max=100, min=100;
		for (File directory: directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
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

	private void fScoreTest(int n, boolean testBagOnly) throws IOException {
		List<Integer> personIndex = personIndexToList(testsetPath + "isperson.txt", 2);
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;

		for (int i=0;i<n;i++){
			File article = new File(testsetPath + i + "_article.txt");
			File tokenized = new File(testsetPath + i + "_tokenized.txt");
			isPerson = (personIndex.contains(i));
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
		System.out.println("F-score: " + String.format("%.2f", (2 * ((precision * recall) / (precision + recall)))));

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

			populator.add(tempbag1, personindex1.contains(counter));
			populator.add(tempbag2, personindex2.contains(counter));
			populator.add(tempbag3, personindex3.contains(counter));
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
		//noinspection ResultOfMethodCallIgnored
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for(File file : files){
				filedir = new File(tokensPath + "/File" + counter + "/");

				//noinspection ResultOfMethodCallIgnored
				filedir.mkdirs();

				for (int i=0;i<range;i++){
					writer = new PrintWriter(filedir + "/tokenized" + i + ".txt", "UTF-8");
					writer.println(tokenizeFirstSentence(fetchNthArticle(file, i)));
					writer.close();
				}
				counter++;
			}
		}
	}
	public void generateAllFiles() throws Exception{
		PrintWriter writer;
		File[] directories = new File(datafilesPath).listFiles(File::isDirectory);
		for (File directory : directories){
			File newdir = new File(outputPath + directory.getName());
			//noinspection ResultOfMethodCallIgnored
			newdir.mkdirs();
			File[] files = new File(directory.getPath()).listFiles();
			assert files != null;
			for(File file : files){
				File filedir = new File(newdir + "/" + file.getName() + "/");
				//noinspection ResultOfMethodCallIgnored
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
		} catch (IOException e) {
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
		if (s.contains("。")) {
			input = s.substring(s.indexOf(("\n")) + 2);
			input = input.substring(0, input.indexOf("。") + 1);
			input = input.replace("\n", "");
		}
		else{
			input = s.substring(s.indexOf("\n"));
			input = input.replace("\n", "");
		}
		return input;
	}

	private String tokenizeFirstSentence(String s) {
		String input;
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

		List<TokenPair> output = new ArrayList<>();

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
		
		List<TokenPair> output = new ArrayList<>();

		for(Token token : result){
			output.add(new TokenPair(token.getSurface(), token.getAllFeaturesArray()));
		}

		return output;
	}

	/* Static methods  */
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
	public static List<String> fetchAllArticles(File file) throws IOException {
		List<String> output = new ArrayList<>();
		boolean record = false;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line, temp="";
		String newline = System.getProperty("line.separator");
		while ((line = br.readLine() ) != null) {
			if (line.contains("</doc>"))
				output.add(temp);
			else if (line.contains("<doc")) {
				temp = "";
				record = true;
				} else if (record)
					temp += line + newline;

		}
		return output;
	}
	public static List<TokenPair> readTokensFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		List<TokenPair> tokenPairs = new ArrayList<>();
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
}