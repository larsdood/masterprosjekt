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

	String basefolder;

	String datafilespath;
	String tokenspath;
	String outputpath;
	String testsetpath;
	Analyser analyser;

	public MasterProgram(){
		tokenizer = new Tokenizer();

		basefolder = "D:/Japanese Wikipedia/";
		datafilespath = basefolder + "target/";
		tokenspath = basefolder + "tokens2/";
		outputpath = basefolder + "output4/";
		testsetpath = basefolder + "testset/";

		analyser = new Analyser();
	}

	public static void main(String[] args) throws IOException{
		MasterProgram masterProgram = new MasterProgram();
		if (args[0].equals(""))
			masterProgram.run();
		masterProgram.run(args[0]);
	}

	public String run(String arg) throws IOException{
		switch(arg){
			case "generateRelevantFiles":
				generateRelevantFiles();
				break;
			case "generate2":
				generate2(2);
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
				trainingSetTest(1);
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

	private void trainingSetTest(int n) throws IOException{
		List<Integer> personIndex = personIndexToList("src/" + "isperson" + n + ".txt");
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;
		File[] directories = new File(outputpath).listFiles(File::isDirectory);
		for (int i = 0;i<directories.length;i++){
			File article = new File(outputpath + "/File" + i + "/" + (n-1) + "_article.txt");
			isPerson = (personIndex.contains((Integer)i));
			analyserJudgement = analyser.analysePath(article.getAbsolutePath()) == Analyser.Status.PASS;
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
		double presicion = ((double)truePositives/ (truePositives + falsePositives));
		double recall = ((double)truePositives / (truePositives + falseNegatives));
		System.out.println("Presicion: " + String.format("%.2f", presicion * 100) + "%, Recall: " + String.format("%.2f", recall * 100) + "%.");
		/*
		for (int i=0;i<n;i++){
			File article = new File(testsetpath + i + "_article.txt");
			File tokenized = new File(testsetpath + i + "_tokenized.txt");
			//System.out.println("Article #" + i);
			//System.out.println(analyser.analysePath(article.getAbsolutePath()));
			isPerson = (personIndex.contains((Integer)i));
			analyserJudgement = analyser.analysePath(article.getAbsolutePath()) == Analyser.Status.PASS;
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
		double presicion = ((double)truePositives/ (truePositives + falsePositives));
		double recall = ((double)truePositives / (truePositives + falseNegatives));
		System.out.println("Presicion: " + String.format("%.2f", presicion*100) + "%, Recall: " + String.format("%.2f", recall*100) + "%.");
		*/
	}

	private void generate2(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
		File newdir = new File(outputpath);
		File filedir;
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				filedir = new File(outputpath + "/File" + counter + "/");
				filedir.mkdirs();

				for (int i=0;i<range;i++){
					writer = new PrintWriter(filedir + "/" + i + "_article" + ".txt", "UTF-8");
					writer.println(fetchNthArticle(file, i));
					writer.close();
					writer = new PrintWriter(filedir + "/" + i + "_tokenized" + ".txt", "UTF-8");
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
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
		File newdir = new File(testsetpath);
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();

			for(File file : files){
				System.out.print("Article number: " + counter);

				String output = fetchNthArticle(file, 48);
				writer = new PrintWriter(newdir + "/" + counter + "_" +  "article.txt" , "UTF-8");
				writer.println(output.trim());
				writer.close();

				String tokenized = tokenize(output);
				writer = new PrintWriter(newdir + "/" + counter + "_"  + "tokenized.txt", "UTF-8");
				writer.println(tokenized);
				writer.close();

				counter++;
			}
		}
	}

	/* TODO: Skriv om dette i oppgaven. Relevant å beskrive datasettet jeg startet med og hvordan det er konstruert. */
	private void countAllArticles() throws IOException {
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
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
	private void fScoreTest(int n) throws IOException {
		List<Integer> personIndex = personIndexToList(testsetpath + "isperson.txt");
		boolean isPerson, analyserJudgement;
		int falseNegatives=0, falsePositives=0, truePositives=0, trueNegatives=0;

		for (int i=0;i<n;i++){
			File article = new File(testsetpath + i + "_article.txt");
			File tokenized = new File(testsetpath + i + "_tokenized.txt");
			//System.out.println("Article #" + i);
			//System.out.println(analyser.analysePath(article.getAbsolutePath()));
			isPerson = (personIndex.contains((Integer)i));
			analyserJudgement = analyser.analysePath(article.getAbsolutePath()) == Analyser.Status.PASS;
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
		double presicion = ((double)truePositives/ (truePositives + falsePositives));
		double recall = ((double)truePositives / (truePositives + falseNegatives));
		System.out.println("Presicion: " + String.format("%.2f", presicion * 100) + "%, Recall: " + String.format("%.2f", recall * 100) + "%.");

	}
	private void fScoreTest() throws IOException {
		fScoreTest(378);
	}

	public void populateBagOfTokens() throws IOException {
		BagPopulator populator = new BagPopulator(tokenspath);
		File[] directories = new File(tokenspath).listFiles(File::isDirectory);

		List<Integer> personindex1 = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader("src/isperson1.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			personindex1.add(Integer.parseInt(line));
		}

		int counter =0;
		for (File subdir : directories){
			BagOfTokens tempbag = new BagOfTokens();
			List<TokenPair> tokenPairList = readTokensFile(new File(subdir.getAbsolutePath() + "/tokenized0.txt"));
			tempbag.addSet(tokenPairList);

			populator.add(tempbag, personindex1.contains((Integer)counter));
			counter++;
		}
		System.out.println(populator);
	}

	public void tokenizeArticles(int range) throws IOException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
		File newdir = new File(tokenspath);
		File filedir;
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				filedir = new File(tokenspath + "/File" + counter + "/");
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

	public void generateRelevantFiles() throws IOException{
		PrintWriter writer;
		int counter=0, successcounter=0;
		long timerstart, timerstop;
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
		File newdir = new File(outputpath);
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
	
	public void generateAllFiles() throws Exception{
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
	public static List<Integer> personIndexToList (String filepath) throws IOException{
		List<Integer> personIndex = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line;
		while ((line = br.readLine()) != null) {
			//line = line.replaceAll("\\D+","");
			//personIndex.add(Integer.parseInt(line));
			if (line.matches("\\d+"))
				personIndex.add(Integer.parseInt(line));
		}
		return personIndex;
	}

	/* METHODS USED DURING TESTING OF DEVELOPMENT: CAN BE DELETED AT LATER TIME. NOT NECESSARY FOR FINAL PRODUCT. */
	private void testFetch() throws IOException {
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
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