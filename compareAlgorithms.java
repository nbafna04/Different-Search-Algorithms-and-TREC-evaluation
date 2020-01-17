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
import java.nio.file.Paths;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms 
{
	static int flag=0;
	
	public static void main(String args[]) throws ParseException, IOException
	{	
		String storeFileAsString = null;
	    	
	    		//read the file topics.51-100 and convert it to string format
				//https://stackoverflow.com/questions/16027229/reading-from-a-text-file-and-storing-in-a-string
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
	    	
	    	
	    	System.out.println("Running different Algorithms on short query ie TITLE field");
	    	//ShortQuery
	    	//Calls using different algorithms
	    	//Function Call : FileAsString, Algorithm, AlgoName
	    	System.out.println("1. Vector Space Model");
			topicTRECTitle(splitFileTopArray,new ClassicSimilarity(), "VectorSpace");
			System.out.println("2. BM25");
			topicTRECTitle(splitFileTopArray,new BM25Similarity(),"BM25");
			System.out.println("3. Language model with Dirichlet Smoothing");
			topicTRECTitle(splitFileTopArray,new LMDirichletSimilarity(),"LMDirichlet");
			System.out.println("4. Language Model with Jelinek Mercer Smoothing having lambda value as 0.7");
			topicTRECTitle(splitFileTopArray,new LMJelinekMercerSimilarity((float)0.7),"LMJelinek");
			
	    	System.out.println("Completed running different algorithms on short query ie TITLE");
	    	
	    	System.out.println("Running different Algorithms on long query ie DESC field");
	    	//LongQuery
	    	//Calls using different algorithms
	    	//Function Call : FileAsString, Algorithm, AlgoName
	    	System.out.println("1. Vector Space Model");
	    	topicTRECDesc(splitFileTopArray,new ClassicSimilarity(), "VectorSpace");
	    	System.out.println("2. BM25");
	    	topicTRECDesc(splitFileTopArray,new BM25Similarity(),"BM25");
	    	System.out.println("3. Language model with Dirichlet Smoothing");
	    	topicTRECDesc(splitFileTopArray,new LMDirichletSimilarity(),"LMDirichlet");
	    	System.out.println("4. Language Model with Jelinek Mercer Smoothing having lambda value as 0.7");
	    	topicTRECDesc(splitFileTopArray,new LMJelinekMercerSimilarity((float)0.7),"LMJelinek");
	    	
	    	System.out.println("Completed running different algorithms on long query ie DESC");
	}
	
	public static void topicTRECTitle(String[] splitFileTopArray,Similarity similarity,String AlgoName) throws IOException, ParseException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Nikita\\Sem1\\Search\\Assignment2\\index\\index")));
		IndexSearcher searcher = new IndexSearcher(reader);
    		
    		
    			File f = new File(AlgoName+"ShortQuery.txt");
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

    					//use models to calculate relevance score.
    					searcher.setSimilarity(similarity);
    					
    					Analyzer analyzer = new StandardAnalyzer();
    					QueryParser parser = new QueryParser("TEXT", analyzer);
    					Query query = parser.parse(QueryParser.escape(title_text[1]));
    					
    					TopDocs topDocs = searcher.search(query, 1000);
    					//get relevance score
    					ScoreDoc[] docs = topDocs.scoreDocs;
    					for (int i = 0; i < docs.length; i++) 
    					{
    						Document doc = searcher.doc(docs[i].doc);
    						fileWriter.append(Integer.toString(docIndex+50));
    						fileWriter.append("\t" + "Q"+(docIndex));
    						fileWriter.append("\t" + doc.get("DOCNO"));
    						fileWriter.append("\t" + rank);
    						fileWriter.append("\t" + docs[i].score);
    						fileWriter.append("\t" + "run-1\n");
     						//pw.write((docNumFromFile[1]+"\tQ"+(j-1)+"\t"+ doc.get("DOCNO") + "\t"+rank +"\t"+ docs[i].score+"\t"+"run-"+j+"\n"));
     						rank++;
    					}
    				}
    			}
    			fileWriter.flush();
    			fileWriter.close();
    	}		
		//for long query of each model
	
	public static void topicTRECDesc(String[] splitFileTopArray,Similarity similarity,String AlgoName) throws IOException, ParseException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Nikita\\Sem1\\Search\\Assignment2\\index\\index")));
		IndexSearcher searcher = new IndexSearcher(reader);
    		
    		
    			File f = new File(AlgoName+"LongQuery.txt");
    			FileWriter fileWriter = new FileWriter(f);
    			//PrintWriter pw = new PrintWriter(fos);
    		
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
    				Matcher m1 = Pattern.compile(docnumregex).matcher(s);   
    				if (m1.find()) 
    				{
     					docNumFromFile = m1.group(1).split(":"); 
     				}
    				
    				String queryID = docNumFromFile[1];
    				
    				//For each top, take title field as short query
    				String descregex =  "<desc>(.*?)<smry>";
    				Matcher matcher1 = Pattern.compile(descregex).matcher(s);   
    				if (matcher1.find()) 
    				{		
    					//extract desc text
    					String desc_text[] = matcher1.group(1).split(":"); 

    					//use models to calculate relevance score.
    					searcher.setSimilarity(similarity);
    					
    					Analyzer analyzer = new StandardAnalyzer();
    					QueryParser parser = new QueryParser("TEXT", analyzer);
    					Query query = parser.parse(QueryParser.escape(desc_text[1]));
    					
    					TopDocs topDocs = searcher.search(query, 1000);
    					//get relevance score
    					ScoreDoc[] docs = topDocs.scoreDocs;
    					for (int i = 0; i < docs.length; i++) 
    					{
    						Document doc = searcher.doc(docs[i].doc);
    						fileWriter.append(Integer.toString(docIndex+50));
    						fileWriter.append("\t" + "Q"+(docIndex));
    						fileWriter.append("\t" + doc.get("DOCNO"));
    						fileWriter.append("\t" + rank);
    						fileWriter.append("\t" + docs[i].score);
    						fileWriter.append("\t" + "run-1\n");
     						//pw.write((docNumFromFile[1]+"\tQ"+(j-1)+"\t"+ doc.get("DOCNO") + "\t"+rank +"\t"+ docs[i].score+"\t"+"run-"+j+"\n"));
     						rank++;
    					}
    				}
    			}
    			fileWriter.flush();
    			fileWriter.close();
    	}
	 
   	}
