package com.holdaas.app;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BagOfTokens {
	private Map<String, Integer> list;
	public BagOfTokens(){
		list = new HashMap<String, Integer>();
	}
	
	public void add(String word){
		if (isValid(word)){
			if (list.containsKey(word)){
				list.replace(word, list.get(word)+1);
			}
			else list.put(word, 1);
		}
	}
	public String toString(){
		//valueLimit(2);
		sort();
		String output = "";
		for (Map.Entry<String, Integer> entry : list.entrySet()){
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
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		Map<String, Integer> copy = list;
		int highest;
		String word;
		while(sortedMap.size()<list.size()){
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
		list = sortedMap;
	}
	/* Recommended to do before sorting */
	public void valueLimit(int limit){
		Map<String, Integer> limitedList = new HashMap<String, Integer>();
		for(Map.Entry<String, Integer> entry : list.entrySet()){
			if (entry.getValue().intValue()>limit)
				limitedList.put(entry.getKey(), entry.getValue());
		}
		list = limitedList;
	}
}
