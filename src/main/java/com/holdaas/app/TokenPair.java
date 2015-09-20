package com.holdaas.app;

public class TokenPair {
	private String word;
	private String[] tokens;
	public TokenPair(String word, String[] tokens){
		this.word = word;
		this.tokens = tokens;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String[] getTokens() {
		return tokens;
	}
	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}
}
