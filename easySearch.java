package assignment2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {




	public static void main(String args[]) throws Exception
	{
		//Get search query from user
		Scanner myObj;
		myObj = new Scanner(System.in);
    	System.out.print("Enter query: ");
    	String s = myObj.nextLine(); 
    	
    	//Set Similairty
    	ClassicSimilarity dSimi = new ClassicSimilarity();
    	Map<String, Float> score = getQueryRelevanceScores(s,dSimi,1);
    	
    	//File to write the relevance score
    	File f = new File("score.txt");
		FileWriter fileWriter = new FileWriter(f);
		for (String key:score.keySet())
		{
			fileWriter.write(key+"\t"+score.get(key)+"\n");
		}
		fileWriter.flush();
		fileWriter.close();
   	
	}
	
	 static Map<String, Float> getQueryRelevanceScores(String queryString, Similarity similarity,int flag)
			throws  IOException, ParseException {
		 
		 if (flag==1) {
	   	
    	//Hashmap to store docid - doc length and docid - score respectively
		HashMap<String,Float> doc_length=new HashMap<String,Float>();  
		HashMap<String,Float> score=new HashMap<String,Float>();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
		IndexSearcher searcher = new IndexSearcher(reader);
	
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		searcher.setSimilarity(similarity);
		
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(QueryParser.escape(queryString));

		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		
		//Print all the terms in the query
		System.out.println("Terms in the query: ");
		for (Term t : queryTerms) {
			System.out.println(t.text());
		}
		
		System.out.println();
		
		//for each term in a query
		for (Term t : queryTerms)
		{	
			//File to write relevance of each term in the query
			File f = new File("score_"+t.text()+".txt");
			FileWriter fileWriter = new FileWriter(f);
			float sum=0;
			
		    	//Calcualte k(t)
		    	int kt=reader.docFreq(new Term("TEXT", t.text()));
		    	
		    	System.out.println("Number of documents containing the term "+t.text()+ " for field \"TEXT\": "+kt);
		    	
		    	//if term not present in that document skip rest and move to next term
		    	if (kt == 0) 
		    	{
		      	continue;
		    	}
		    	
		    	//Calculate N
		    	int totalno_docs=reader.maxDoc();
		    	
		    	System.out.println("Total number of documents: "+totalno_docs);
		    	
		    	
		    	List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		    	for (int i = 0; i < leafContexts.size(); i++)
		    	{
		    	
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				for (int docId = 0; docId < numberOfDoc; docId++) 
				{
					float normDocLeng = ((ClassicSimilarity) similarity).decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
					float docLeng = 1 / (normDocLeng * normDocLeng);
					
					//length(doc)
					doc_length.put(searcher.doc(docId +startDocNo).get("DOCNO"),docLeng);
				
				}
				

				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				if (de != null) 
				{
					while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) 
					{
						//c(t,doc)
						String doc_id=searcher.doc(de.docID() +startDocNo).get("DOCNO");
						if(doc_length.keySet().contains(doc_id) )
						{
							//calculate tf idf
							sum=(float) ((de.freq())/(doc_length).get(doc_id)*Math.log(1+(float)(totalno_docs/kt)));
							
			            	//for each query term append in file the relevance score
									    			
							fileWriter.write("For the term - "+t.text()+ " in doc id- "+doc_id+" the relevance score is - " +sum+"\n");
							
						}
						if(score.keySet().contains(doc_id))
				        	{    
				        		//if already present in score hashmap then add in score  
				            		score.put(doc_id, score.get(doc_id)+sum);
				        	}
				       		else
				       		{
				            		//create a new tuple with new score
				            		score.put(doc_id, sum);
				       		}
					
						System.out.println(t.text()+ " occurs " + de.freq() + " time(s) in doc" + doc_id);
					}

				}
			}

		    	fileWriter.flush();
		    	fileWriter.close();
		}
		 return score;
	}
		 else {
			   	
		    	//Hashmap to store docid - doc length and docid - score respectively
				HashMap<String,Float> doc_length=new HashMap<String,Float>();  
				HashMap<String,Float> score=new HashMap<String,Float>();
				
				IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Nikita\\Sem1\\Search\\Assignment2\\index\\index")));
				IndexSearcher searcher = new IndexSearcher(reader);
			
				// Get the preprocessed query terms
				Analyzer analyzer = new StandardAnalyzer();
				searcher.setSimilarity(similarity);
				
				QueryParser parser = new QueryParser("TEXT", analyzer);
				Query query = parser.parse(QueryParser.escape(queryString));

				Set<Term> queryTerms = new LinkedHashSet<Term>();
				searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
				
				
				//for each term in a query
				for (Term t : queryTerms)
				{	
	
					float sum=0;
					
				    	//Calcualte k(t)
				    	int kt=reader.docFreq(new Term("TEXT", t.text()));
				    	
				    	//if term not present in that document skip rest and move to next term
				    	if (kt == 0) 
				    	{
				      	continue;
				    	}
				    	
				    	//Calculate N
				    	int totalno_docs=reader.maxDoc();		    	
				    	
				    	List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
				    	for (int i = 0; i < leafContexts.size(); i++)
				    	{
				    	
						LeafReaderContext leafContext = leafContexts.get(i);
						int startDocNo = leafContext.docBase;
						int numberOfDoc = leafContext.reader().maxDoc();
						for (int docId = 0; docId < numberOfDoc; docId++) 
						{
							float normDocLeng = ((ClassicSimilarity) similarity).decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
							float docLeng = 1 / (normDocLeng * normDocLeng);
							
							//length(doc)
							doc_length.put(searcher.doc(docId +startDocNo).get("DOCNO"),docLeng);
						
						}
						

						PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
						if (de != null) 
						{
							while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) 
							{
								//c(t,doc)
								String doc_id=searcher.doc(de.docID() +startDocNo).get("DOCNO");
								if(doc_length.keySet().contains(doc_id) )
								{
									//calculate tf idf
									sum=(float) ((de.freq())/(doc_length).get(doc_id)*Math.log(1+(float)(totalno_docs/kt)));
									
					            	//for each query term append in file the relevance score
									
								}
								if(score.keySet().contains(doc_id))
						        	{    
						        		//if already present in score hashmap then add in score  
						            		score.put(doc_id, score.get(doc_id)+sum);
						        	}
						       		else
						       		{
						            		//create a new tuple with new score
						            		score.put(doc_id, sum);
						       		}
							
							}

						}
					}

				}
				 return score; 
		 }	 
	 }
	 }
