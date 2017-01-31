package com.magnifis.parking.metaphone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


// import Metaphone3;

public class ResultRanking
{
	private String searchString;
	private List<String> candidates = new ArrayList<String>();
	private List<String> resultList = new ArrayList<String>();
	private SortedMap<Integer, List<String>> workingList = new TreeMap<Integer, List<String>>();
	private int spellingMultiplier = 1;
	private int phoneticMultiplier = 1; 

	Metaphone3 m3 = new Metaphone3();

	public ResultRanking()
	{
		m3.SetEncodeExact(true);
		m3.SetEncodeVowels(true);
	}

	public void setSearchStringAndCandidates(String searchString, List<String> candidates)
	{
		this.searchString = searchString;
		this.candidates = candidates;
	}

	public List<String> getRankedResultList()
	{
		rankResultList();
		Iterator<Integer> iterator = workingList.keySet().iterator();
		while(iterator.hasNext())
		{
			Object key = iterator.next();
			List<String> tempList = workingList.get(key);
			for(String word : tempList)
			{
				resultList.add(word);
			}
		}
		return resultList;
	}
	
	public SortedMap<Integer, List<String>> rankResultList()
	{
		workingList.clear();
		m3.SetWord(searchString);
		m3.Encode();
		String searchStringEncoded = m3.GetMetaph();
		for(String candidate : candidates)
		{
			m3.SetWord(candidate);
			m3.Encode();
			String candidateEncoded = m3.GetMetaph();
			
			int phoneticDistance = 0;
			int spellingDistance = 0;
			if(phoneticMultiplier > 0)
			{
				phoneticDistance = LevenshteinDistance(searchStringEncoded, candidateEncoded);				
			}
			if(spellingMultiplier > 0)
			{
				spellingDistance = LevenshteinDistance(searchString, candidate);
			}
			int distance = (phoneticDistance  * phoneticMultiplier) + (spellingDistance * spellingMultiplier);
			
			List<String> tempList = workingList.get(distance);
			if(tempList == null)
			{
				tempList = new ArrayList<String>();
			}
			tempList.add(candidate);
			workingList.put(distance, tempList);
		}
		return workingList;
	}

	public static int LevenshteinDistance(String str1, String str2)
	{
		int[][] distance = new int[64][64];
		int len1 = str1.length();
		int len2 = str2.length();
		int cost = 0;

		for(int i = 0; i <= len1; i++)
		{
			distance[i][0] = i;
		}

		for(int j = 0; j <= len2; j++)
		{
			distance[0][j] = j;
		}

		for(int i = 1; i <= len1; i++)
		{
			for(int j = 1; j <= len2; j++)
			{
				if(str1.charAt(i - 1) == str2.charAt(j - 1))
				{
					cost = 0;
				} 
				else
				{
					cost = 1;
				}

				// ///////////////////////////////////////////////////
				// d[i][j] = minimum(d[i-1, j] + 1, // deletion, //
				// d[i, j-1] + 1, // insertion, //
				// d[i-1, j-1] + cost) // substitution //
				// ///////////////////////////////////////////////////

				int del = distance[i - 1][j] + 1;
				int ins = distance[i][j - 1] + 1;
				int subs = distance[i - 1][j - 1] + cost;
				int min = del;

				if(ins < min)
				{
					min = ins;
				}

				if(subs < min)
				{
					min = subs;
				}

				distance[i][j] = min;
			}
		}

		return distance[len1][len2];
	}
	
	public int getSpellingMultiplier()
	{
		return spellingMultiplier;
	}

	public void setSpellingMultiplier(int spellingMultiplier)
	{
		this.spellingMultiplier = spellingMultiplier;
	}

	public int getPhoneticMultiplier()
	{
		return phoneticMultiplier;
	}

	public void setPhoneticMultiplier(int phoneticMultiplier)
	{
		this.phoneticMultiplier = phoneticMultiplier;
	}


	public static void main(String[] args)
	{
		/*
		 * ALFRT ALFORD ALFRT ALFRED ALFRT ALFREDA ALFRT ALFREDIA ALFRT ALFREDO
		 * ALFRT ALVARADO ALFRT ALVARDO ALFRT ALVERTA ALFRT ALVORD ALFRT ELFREDA
		 * ALFRT ELFRIEDA ALFRT ELFRIEDE ALFRT WILFORD ALFRT WILFRED ALFRT
		 * WILFREDO ALFRT WILLIFORD ALFRT (FLFRT) WILLEFORD ALFRT (FLFRT)
		 * WOLFORD ALFRT (FLFRT) WOOLFORD
		 */

		List<String> list = new ArrayList<String>();
		list.add("ALFORD");
		list.add("ALFRED");
		list.add("ALFREDA");
		list.add("ALFREDIA");
		list.add("ALFREDO");
		list.add("ALVARADO");
		list.add("ALVARDO");
		list.add("ALVERTA");
		list.add("ALVORD");
		list.add("ELFREDA");
		list.add("ELFRIEDA");
		list.add("ELFRIEDE");
		list.add("WILFORD");
		list.add("WILFRED");
		list.add("WILFREDO");
		list.add("WILLIFORD");
		list.add("WILLEFORD");
		list.add("WOLFORD");
		list.add("WOOLFORD");

		ResultRanking rr = new ResultRanking();
		rr.setSearchStringAndCandidates("ALFRED", list);
		SortedMap<Integer, List<String>> rankedList = rr.rankResultList();

		Iterator<Integer> iterator = rankedList.keySet().iterator();
		while(iterator.hasNext())
		{
			Object key = iterator.next();
			List<String> tempList = rankedList.get(key);
			for(String word : tempList)
			{
				System.out.println("key : " + key + " value :" + word);
			}
		}
		List<String> rankedStrList = rr.getRankedResultList();
		for(String listStr : rankedStrList) 
		{
			System.out.println(listStr);
		}
		
		System.out.println("///// RANK BY SPELLING ALONE////////////");
		rr.setPhoneticMultiplier(0);
		rankedStrList.clear();
		rankedStrList = rr.getRankedResultList();
		for(String listStr : rankedStrList) 
		{
			System.out.println(listStr);
		}
		
		System.out.println("///// RANK BY PHONETIC SIMILARITY ALONE////////////");
		rr.setPhoneticMultiplier(1);
		rr.setSpellingMultiplier(0);
		rankedStrList.clear();
		rankedStrList = rr.getRankedResultList();
		for(String listStr : rankedStrList) 
		{
			System.out.println(listStr);
		}

	}
}
