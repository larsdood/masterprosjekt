package com.holdaas.app;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//import org.atilika.kuromoji.Token;
//import org.atilika.kuromoji.Tokenizer;
//import org.atilika.kuromoji.Tokenizer.Mode;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

public class SampleTest {
	
	public static void main(String[] args) throws IOException{
		
		Tokenizer tokenizer = new Tokenizer();
		//Mode mode = Mode.NORMAL;
		

		String datafilespath = "D:/Japanese Wikipedia/target/";
		Analyser analyser = new Analyser(datafilespath);
		String tokenspath = "D:/Japanese Wikipedia/tokens/";
		String outputpath = "D:/Japanese Wikipedia/output3/";
		if (args.length==0)
			{
			try {
				generateRelevantFiles(datafilespath, outputpath, tokenizer, analyser);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (args[0].equals("tokenize")){
			tokenizeArticles(datafilespath, tokenspath, tokenizer);
		}
		else if (args[0].equals("populateBag")){
			populateBagOfTokens(tokenspath);
		}
		else{
			System.out.println("invalid argument: " + args[0]);
			System.exit(0);
		}
	}

	public static void populateBagOfTokens(String tokenspath) throws IOException {
		BagPopulator populator = new BagPopulator(tokenspath);
		File[] directories = new File(tokenspath).listFiles(File::isDirectory);
		for (File subdir : directories){
			BagOfTokens tempbag = new BagOfTokens();
			List<TokenPair> tokenPairList = readTokensFile(new File(subdir.getAbsolutePath() + "/tokenized.txt"));
			tempbag.addSet(tokenPairList);
			populator.add(tempbag, false);
		}
		System.out.println(populator);
	}

	public static void tokenizeArticles(String datafilespath, String tokenspath, Tokenizer tokenizer) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer;
		int counter=0;
		File[] directories = new File(datafilespath).listFiles(File::isDirectory);
		File newdir = new File(tokenspath);
		File filedir;
		newdir.mkdirs();
		for (File directory : directories){
			File[] files = new File(directory.getPath()).listFiles();
			for(File file : files){
				filedir = new File(tokenspath + "/" + counter + "/");
				filedir.mkdirs();

				writer = new PrintWriter(filedir + "/tokenized.txt", "UTF-8");
				writer.println(tokenize(fetchFirstArticle(file), tokenizer));
				writer.close();
				counter++;
			}
		}
	}

	public static void generateRelevantFiles(String datafilespath, String outputpath, Tokenizer tokenizer, Analyser analyser) throws Exception{
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
					List<TokenPair> tokenPairs = tokenizeToPairList(output, tokenizer);
					
					if(analyser.analyseTokens(tokenPairs) == Analyser.Status.PASS){
						timerstart = System.currentTimeMillis();
						System.out.println(file.getName());
						writer = new PrintWriter(newdir + "/" + counter + "_" +  " article.txt" , "UTF-8");
						writer.println(output.trim());
						writer.close();
						String tokenized = tokenize(output, tokenizer);

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
	
	public void generateAllFiles(String datafilespath, String outputpath, Tokenizer tokenizer) throws Exception{
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
				writer.println(tokenize(output, tokenizer));
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
		return tokenize(input, new Tokenizer());
	}
	
	public static String tokenize(String input, Tokenizer tokenizer)
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
	
	public static List<TokenPair> tokenizeToPairList(String input, Tokenizer tokenizer)
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
	
	public static String fetchFirstArticle(File file){
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
			e.printStackTrace();
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
}