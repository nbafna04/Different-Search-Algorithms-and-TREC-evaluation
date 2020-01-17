package assignment2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class SearchTRECtopics {
	static int flag=0;
	private static BufferedReader bf;
	public static void main(String args[]) throws ParseException, IOException
	{	
		
			String storeFileAsString = null;
			
			Similarity defaultSimilarity = new ClassicSimilarity();
		    		
			InputStream is = new FileInputStream("topics.51-100"); 
			BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
			try{
				
			StringBuilder sb = new StringBuilder();
			String line = br.readLine(); 
	
			while(line != null)
			{ 
				sb.append(line); 
				sb.append(" ");
				line = br.readLine();
			}
			
			storeFileAsString = sb.toString();
			}
			finally {
				br.close();
			}
			
	    	//Creating an Array to split the string on the basis of <top> keyword distinguishing the topics
	    	String[] splitFileTopArray=storeFileAsString.split("<top>");
	    		    	
		    System.out.println("Starting TREC building report for Title");	
		    topicTRECTitle(splitFileTopArray,defaultSimilarity);
	    	System.out.println("Completed for Title");
	    	
	    	System.out.println("Starting TREC building report for Desc");	
	    	topicTRECdesc(splitFileTopArray,defaultSimilarity);
	    	System.out.println("Completed for Desc");

	}
	    
	public static void topicTRECTitle(String[] splitFileTopArray,Similarity defaultSimilarity) throws IOException, ParseException
	{
		//for short query
	    	  		//Write the output of TREC Report to ShortQuery.txt    	
		    		File f = new File("ShortQuery.txt");
	    			FileWriter fileWriter = new FileWriter(f);
	    			
	    			//for each <top> in file
	    			for(int docIndex=1;docIndex<splitFileTopArray.length;docIndex++)
	    			{
	     				int rank=1;
	    				String s=splitFileTopArray[docIndex];
	    				String docNumFromFile[]=null;
	    				//Regular expression
	    				//https://www.geeksforgeeks.org/regular-expressions-in-java/
	    				//Group1 = num
	    				//Group2 = dom
	    				//Group0 = <num>(.*?)<dom>
	    				String docnumregex="<num>(.*?)<dom>";
	    				Matcher m = Pattern.compile(docnumregex).matcher(s);   
	    				if (m.find()) 
	    				{
	     					docNumFromFile = m.group(1).split(":"); 
	     				}
	    				
	    				String queryID = docNumFromFile[1];
	    				
	    				//For each top, take title field as short query
	    				String titleregex =  "<title>(.*?)<desc>";
	    				Matcher matcher = Pattern.compile(titleregex).matcher(s);   
	    				if (matcher.find()) 
	    				{		
	    					//extract title text
	    					String title_text[] = matcher.group(1).split(":"); 
		    		
		    				//Call to calculate relevance score
		    				Map<String, Float> score=easySearch.getQueryRelevanceScores(title_text[1],defaultSimilarity,0);

		    				Object[] a = score.entrySet().toArray();
		    				//https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
		    				Arrays.sort(a, new Comparator() {
		    				    public int compare(Object o1, Object o2) {
		    				        return ((Map.Entry<String, Float>) o2).getValue()
		    				                   .compareTo(((Map.Entry<String, Float>) o1).getValue());
		    				    }
		    				});
		    				for (Object e : a) 
		    				{
		    					
		    					fileWriter.append(Integer.toString(docIndex+50));
	    						fileWriter.append("\t" + "Q"+(docIndex));
	    						fileWriter.append("\t" + ((Map.Entry<String, Float>) e).getKey());
	    						fileWriter.append("\t" + rank);
	    						fileWriter.append("\t" + ((Map.Entry<String, Float >) e).getValue());
	    						fileWriter.append("\t" + "run-1\n");
	     						rank++;
		    				    if(rank>1000) 
		    				    {
	    				    		break;
	    				   		 }
		    				}
		    			}
		    		}
	    			fileWriter.flush();
	    			fileWriter.close();
	}
	   
	public static void topicTRECdesc(String[] splitFileTopArray,Similarity defaultSimilarity) throws IOException, ParseException    
	{
		 //for long query
	    	
	    		//Write the output of TREC Report to LongQuery.txt    	
		    		File f = new File("LongQuery.txt");
	    			FileWriter fileWriter = new FileWriter(f);
	    			
	    			//for each <top> in file
	    			for(int docIndex=1;docIndex<splitFileTopArray.length;docIndex++)
	    			{
	     				int rank=1;
	    				String s=splitFileTopArray[docIndex];
	    				String docNumFromFile[]=null;
	    				//Regular expression
	    				//https://www.geeksforgeeks.org/regular-expressions-in-java/
	    				//Group1 = num
	    				//Group2 = dom
	    				//Group0 = <num>(.*?)<dom>
	    				String docnumregex="<num>(.*?)<dom>";
	    				Matcher m = Pattern.compile(docnumregex).matcher(s);   
	    				if (m.find()) 
	    				{
	     					docNumFromFile = m.group(1).split(":"); 
	     				}
	    				
	    				String queryID = docNumFromFile[1];
	    				
	    				//For each top, take title field as short query
	    				String descregex =  "<desc>(.*?)<smry>";
	    				Matcher matcher = Pattern.compile(descregex).matcher(s);   
	    				if (matcher.find()) 
	    				{		
	    					//extract title text
	    					String desc_text[] = matcher.group(1).split(":"); 
		    		
		    				//Call to calculate relevance score
		    				Map<String, Float> score=easySearch.getQueryRelevanceScores(desc_text[1],defaultSimilarity,0);

		    				Object[] a = score.entrySet().toArray();
		    				//https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
		    				Arrays.sort(a, new Comparator() {
		    				    public int compare(Object o1, Object o2) {
		    				        return ((Map.Entry<String, Float>) o2).getValue()
		    				                   .compareTo(((Map.Entry<String, Float>) o1).getValue());
		    				    }
		    				});
		    				for (Object e : a) 
		    				{
		    					
		    					fileWriter.append(Integer.toString(docIndex+50));
	    						fileWriter.append("\t" + "Q"+(docIndex));
	    						fileWriter.append("\t" + ((Map.Entry<String, Float>) e).getKey());
	    						fileWriter.append("\t" + rank);
	    						fileWriter.append("\t" + ((Map.Entry<String, Float >) e).getValue());
	    						fileWriter.append("\t" + "run-1\n");
	     						rank++;
		    				    if(rank>1000) 
		    				    {
	    				    		break;
	    				   		 }
		    				}
		    			}
		    		}
	    			fileWriter.flush();
	    			fileWriter.close();
	}

}//End of Class
