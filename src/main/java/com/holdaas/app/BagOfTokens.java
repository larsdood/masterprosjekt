package com.holdaas.app;

import java.util.*;

public class BagOfTokens {
	private Map<String, Integer> map;
	public BagOfTokens(){
		map = new HashMap<String, Integer>();
	}
	private List<String> excludeset = new ArrayList<>();
	public void addSingle(String word){
		if (isValid(word)){
			if (map.containsKey(word)){
				map.replace(word, map.get(word)+1);
			}
			else map.put(word, 1);
		}
	}
	public void addSet(List<TokenPair> tokenPairs){
		for (TokenPair tokenPair: tokenPairs){
			addSingle(tokenPair.getWord());
		}
	}
	public String toString(){
		sort();
		String output = "";
		for (Map.Entry<String, Integer> entry : map.entrySet()){
			output += entry.getKey() + ": " + entry.getValue() + "\n";
		}
		return output;
	}
	
	public boolean isValid(String word){
		
		int asciiv = (int)word.charAt(0);
		if (asciiv < 64 || (asciiv >= 91 && asciiv<=96) || (asciiv >= 123 && asciiv<= 11900 ) || ( asciiv >= 12289 && asciiv <= 12350)
				|| asciiv == 65288 || asciiv == 65289)
			return false;
		if(word.length()==1 && asciiv>=12353 && asciiv<=12585)
			return false;
		return true;
	}
	public void sort(){
		sort(0);
	}
	public void sort(int limit){
		if (limit!=0){
			valueLimit(limit);
		}
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		Map<String, Integer> copy = map;
		int highest;
		String word;
		while(sortedMap.size()< map.size()){
			word = "";
			highest = 0;
			
			for(Map.Entry<String, Integer> entry : copy.entrySet()){
				if (entry.getValue() > highest){
					highest = entry.getValue();
					word = entry.getKey();
				}	
			}
			copy.remove(word);
			sortedMap.put(word, highest);
		}
		map = sortedMap;
	}
	/* Recommended to do before sorting */
	public void valueLimit(int limit){
		Map<String, Integer> limitedList = new HashMap<String, Integer>();
		for(Map.Entry<String, Integer> entry : map.entrySet()){
			if (entry.getValue().intValue()>limit)
				limitedList.put(entry.getKey(), entry.getValue());
		}
		map = limitedList;
	}

	public void merge(BagOfTokens inbag){
		for (Map.Entry<String, Integer> entry : inbag.getMap().entrySet()){
			addSingle(entry.getKey());
		}
	}

	public Map<String, Integer> getMap(){
		return map;
	}

	private void PopulateExcludeSet(){
		excludeset.add("ある");
	}
}
