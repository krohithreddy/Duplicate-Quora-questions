import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.*;
import java.lang.*;
import org.tartarus.martin.*;

class InvertedIndex{
// To store words and there frequency
  public static HashMap<String, HashMap<Integer,Integer>> invertedIndex;
// To store Stop words
  public static HashSet<String> StopWords;

  public static int num_of_token, num_of_words_in_dict;

// print Maximum Length of Postings List
  public static void PrintMax(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
    try{
      int Max =1;
      HashMap<String, HashMap<Integer,Integer>> inverted = invertedindex;
      for (HashMap<Integer,Integer> list : inverted.values()) {
        if(list.size()>Max)
          Max=list.size();
      }
      System.out.println("Maximum Length of Postings List : "+Max);
    } catch (Exception e){
      System.out.println(e);
    }
  }
// print Minimum Length of Postings List
  public static void PrintMin(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
    try{
      int Min =1062;
      HashMap<String, HashMap<Integer,Integer>> inverted = invertedindex;
      for (HashMap<Integer,Integer> list : inverted.values()) {
        if(list.size()<Min)
          Min=list.size();
      }
      System.out.println("Minimum Length of Postings List : "+Min);
    } catch (Exception e){
      System.out.println(e);
    }
  }
// print Average Length of Postings List
  public static void PrintAvg(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
    try{
      double Avg =0;
      HashMap<String, HashMap<Integer,Integer>> inverted = invertedindex;
      for (HashMap<Integer,Integer> list : inverted.values()) {
        Avg=Avg+list.size();
      }
      System.out.println("Average Length of Postings List : "+Avg/inverted.size());
    } catch (Exception e){
      System.out.println(e);
    }
  }
// print Size of the file that stores the inverted index
  public static void PrintFile(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
    try{
      String fileName = "InvertedIndex.txt";
      PrintWriter writer = new PrintWriter(fileName, "UTF-8");
      List<Map.Entry<String, HashMap<Integer,Integer>>> list = new LinkedList<Map.Entry<String, HashMap<Integer,Integer>>>(invertedindex.entrySet());
      for(Map.Entry<String, HashMap<Integer,Integer>> entry : list) {
        writer.println(entry.getKey()+" "+entry.getValue().keySet());
      }
      File file =new File("InvertedIndex.txt");
      double bytes = file.length();
      System.out.println("Size of the file that stores the inverted index : "+bytes/(1024*1024)+" Mb");
    } catch (Exception e){
      System.out.println(e);
    }
  }
// Get Stop words from a file
  public static void GetStopWords()throws Exception{
    try{
      String fileName = "stopwords.txt";
      Scanner sc = new Scanner(new File(fileName));
      while(sc.hasNext()){
        String word = sc.next();
        StopWords.add(word);
      }
    } catch (Exception e){
      System.out.println(e);
    }
  }
// Form inverted index
  public static void FormIndex(String word,int doc_id)throws Exception{
    try{
      if (invertedIndex.get(word)==null){      //create a new word index
        HashMap<Integer,Integer>  list =new HashMap<Integer,Integer>();
        invertedIndex.put(word, list);
        num_of_words_in_dict++;
      }
      HashMap<Integer,Integer>  list =new HashMap<Integer,Integer>();
      list = invertedIndex.get(word);
      if(list.get(doc_id)==null)  // add a new doc_id to the word
        list.put(doc_id,0);
      list.put(doc_id, list.get(doc_id) + 1); // increment the existing doc_id
    } catch (Exception e){
      System.out.println(e);
    }
  }
// genrate words to be indexed by removing email, date and url (Step 1)
  public static void Regex(int doc_id)throws Exception{
    try{
      String Doc_Id = String.valueOf(doc_id);
      String fileName = "Crawled_data/Doc_Id-"+Doc_Id+".txt";
      Scanner sc = new Scanner(new File(fileName));
      while(sc.hasNext()){
        String word = sc.next();
        if(word.matches("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")){  //find url
          FormIndex(word,doc_id);
        }
        else if(word.matches("[a-zA-Z]+-\\d{2}-\\d{4}-\\d{2}:\\d{2}:\\d{2}")){  //find date
          FormIndex(word,doc_id);
        }
        else if(word.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")){  //find email
          FormIndex(word,doc_id);
        }
        else{
          StringTokenizer st = new StringTokenizer(word,"://.,;:''“”~`−`#!@$%^&|+=<>*-_?(){}[]\"/\\");
          while(st.hasMoreTokens()){
            String WordToToken = st.nextToken();
            FormIndex(WordToToken,doc_id);
          }
        }
      }
    } catch (Exception e){
      System.out.println(e);
    }
  }
  // Form inverted index by removing StopWords (Step 2)
    public static void StopWords(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
      try{
        List<Map.Entry<String, HashMap<Integer,Integer>>> list = new LinkedList<Map.Entry<String, HashMap<Integer,Integer>>>(invertedindex.entrySet());
        for (Map.Entry<String, HashMap<Integer,Integer>> entry : list) {
          if(StopWords.contains(entry.getKey().toLowerCase())||entry.getKey().length()<3)
            invertedIndex.remove(entry.getKey());
        }
      } catch (Exception e){
        System.out.println(e);
      }
    }
// genarate words to be indexed by performing stemming (Step 3)
  public static void SetWord(int doc_id)throws Exception{
    try{
      Stemmer ab = new Stemmer();
      String Doc_Id = String.valueOf(doc_id);
      String fileName = "Crawled_data/Doc_Id-"+Doc_Id+".txt";
      Scanner sc = new Scanner(new File(fileName));
      while(sc.hasNext()){
        String word = sc.next();
        if(word.matches("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")){  //find url
          num_of_token++;
          FormIndex(word,doc_id);
        }
        else if(word.matches("[a-zA-Z]+-\\d{2}-\\d{4}-\\d{2}:\\d{2}:\\d{2}")){  //find date
          num_of_token++;
          FormIndex(word,doc_id);
        }
        else if(word.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")){  //find email
          FormIndex(word,doc_id);
          num_of_token++;
        }
        else{
          StringTokenizer st = new StringTokenizer(word,"://.,;:''“”~`−`#!@$%^&|+=<>*-_?(){}[]\"/\\");
          while(st.hasMoreTokens()){
            String WordToToken = st.nextToken();
            WordToToken = WordToToken.toLowerCase();
            num_of_token++;
            if(StopWords.contains(WordToToken)|| WordToToken.length()<3){  // remove StopWords
              //do nothing
            }
            else{
              char[] c=WordToToken.toCharArray();  // perform stemming
              ab.add(c,WordToToken.length());
              ab.stem();
              String mod = ab.toString();
              FormIndex(mod,doc_id);
            }
          }
        }
      }
    } catch (Exception e){
      System.out.println(e);
    }
  }
  // Form inverted index by removing least frequent terms (Step 4)
    public static void LeastFrequent(HashMap<String, HashMap<Integer,Integer>> invertedindex)throws Exception{
      try{
        int least = 11;       // 1% of our document
        List<Map.Entry<String, HashMap<Integer,Integer>>> list = new LinkedList<Map.Entry<String, HashMap<Integer,Integer>>>(invertedindex.entrySet());
        for (Map.Entry<String, HashMap<Integer,Integer>> entry : list) {
          if(entry.getValue().size()<least)        //removing those values
            invertedIndex.remove(entry.getKey());
        }
      } catch (Exception e){
        System.out.println(e);
      }
    }
// print all stats of inverted Index list
  public static void Print()throws Exception{
    try{
      System.out.println("Number of Terms : "+invertedIndex.size());
      PrintMax(invertedIndex);
      PrintMin(invertedIndex);
      PrintAvg(invertedIndex);
      PrintFile(invertedIndex);
    }
    catch (Exception e){
      System.out.println(e);
    }
  }

/********** SECTION - II  BEGIN ***************/
 // Wrap function for section2
  public static void statistics_section2(){
  // Sort the inverted index in descending order based on document frequency of terms
    List<Map.Entry<String, HashMap<Integer,Integer>>> sorted_inverted_index = Sort_map_desc();
    System.out.println("**************  SECTION - II BEGIN ******************");
    print_stat_most_freq(sorted_inverted_index ,  20);
    print_stat_median_freq(sorted_inverted_index ,  20);
    print_stat_least_freq(sorted_inverted_index ,  20);
    System.out.println("\n**************  SECTION - II END ********************\n");
  }

// Print the Statstics of TOP K Frequent terms
  public static void print_stat_most_freq(List<Map.Entry<String, HashMap<Integer,Integer>>> sorted_inverted_index , int k) {
    int i;
    System.out.println("**********  TOP K MOST FREQUENT WORDS *********");
    for(i=0 ; i < k ; i++)
    print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
  }

// Print the Statstics of MEDIAN K  terms
  public static void print_stat_median_freq(List<Map.Entry<String, HashMap<Integer,Integer>>> sorted_inverted_index , int k) {
    int i;
    System.out.println("\n************  K MEDIAN FREQUENT WORDS *********");
    int n = sorted_inverted_index.size();
    if(n%2 == 0 && k%2 == 0) { // n is even and k is even
      for(i = n/2 - k/2 ; i <= n/2 + k/2 - 1 ; i++)
        print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
    } else if (n%2 == 0 && k%2 != 0) { // n is even and k is odd
        for(i = n/2 - (k-1)/2 ; i <= n/2 + (k-1)/2 ; i++)
          print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
    } else if (n%2 != 0 && k%2 == 0) { // n is odd and k is even
        for(i = (n-1)/2 - k/2 + 1 ;  i <= (n-1)/2 + k/2 ; i++)
          print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
    } else { // n is odd and k is odd
      for(i = (n-1)/2 - (k-1)/2 ; i <= (n-1)/2 + (k-1)/2 ; i++)
       print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
    }
  }

  // Print the Statstics of LEAST K Frequent terms
  public static void print_stat_least_freq(List<Map.Entry<String, HashMap<Integer,Integer>>> sorted_inverted_index , int k) {
    int i , n  = sorted_inverted_index.size() ;
    System.out.println("\n**********  TOP K LEAST FREQUENT WORDS *********");
    for(i = n-1; i > n-1-k ; i--)
      print_term_statistics( sorted_inverted_index.get(i).getKey() , sorted_inverted_index.get(i).getValue() );
    }

// Print the term and document frequency and avg gap for a term
  public static void print_term_statistics(String term , HashMap<Integer,Integer> postings) {
    float avg_gap;
    if(postings.size() <= 1)
      avg_gap = 0;
    else
      avg_gap = calculate_avg_gap(postings);
    System.out.println("TERM : "+term);
    System.out.println("DOCUMENT FREQUENCY : "+String.valueOf(postings.size())+" , "+"AVG GAP : "+String.valueOf(avg_gap));
  }

// Calculate avg gap for a term
  public static float calculate_avg_gap( HashMap<Integer,Integer> postings) {
    Set<Integer> keys = postings.keySet();
    int max = Collections.max(keys);
    int min = Collections.min(keys);
    return (float)( max - min ) / postings.size() ;
  }

// Sort the inverted index in descending order based on the document frequency of terms
  public static List<Map.Entry<String, HashMap<Integer,Integer>>> Sort_map_desc() {
    List<Map.Entry<String, HashMap<Integer,Integer>>> list = new LinkedList<Map.Entry<String, HashMap<Integer,Integer>>>(invertedIndex.entrySet());
    // sort based on size of posting list for each term in descending order
    Collections.sort(list, new Comparator<Map.Entry<String, HashMap<Integer,Integer>>>() {
      public int compare(Map.Entry<String, HashMap<Integer,Integer>> o1, Map.Entry<String, HashMap<Integer,Integer>> o2) {
        return (o2.getValue().size())-(o1.getValue().size());
      }
    });
    return list;
  }

/********** SECTION - II  END *****************/

  //To sort a Collection and return as list
  public static List<Integer> asSortedList(Collection<Integer> c) {
    List<Integer> list = new ArrayList<Integer>(c);
    java.util.Collections.sort(list);
    return list;
  }

  //Constructing bins of gaps for Section III part3
  public static HashMap<Integer,Integer> gaps_bin(HashMap<String, HashMap<Integer,Integer>> invertedIndex) {
    HashMap<Integer,Integer> gaps = new HashMap<Integer,Integer>();
    try{
      PrintWriter writer1 = new PrintWriter("postings_gaps.txt", "UTF-8");
      writer1.println("Term_id\t Gaps_List");
      int i = 0;
      for(HashMap<Integer, Integer> hMap: invertedIndex.values()){
        Collection<Integer> unsorted = hMap.keySet();
        List<Integer> sorted = asSortedList(unsorted);
        i++;
        writer1.print(i+"\t");
        for(int j=0; j< sorted.size()-1; j++){
          int diff = sorted.get(j+1) - sorted.get(j);
          writer1.print(" "+diff);
          diff = (diff - diff%20)/20;
          if (gaps.get(diff)==null){
            gaps.put(diff, 1);
          }
          else{
            gaps.put(diff, gaps.get(diff)+1);
          }
        }
        writer1.print("\n");
      }
      writer1.close();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return gaps;
  }

  public static void main(String[] args){
    try {
      invertedIndex = new HashMap<String, HashMap<Integer,Integer>>();
      StopWords = new HashSet<String>();
      GetStopWords();
      // genrate words to be indexed by removing email, date and url (Step 1)
      for(int i=1;i<1063;i++)
          Regex(i);
      System.out.println("Step1 : ");
      Print();
    // Form inverted index by removing StopWords (Step 2)
      StopWords(invertedIndex);
      System.out.println("Step2 : ");
      Print();
      // genarate words to be indexed by performing stemming (Step 3)
      invertedIndex.clear();
      try{
        PrintWriter writer = new PrintWriter("section3_part1.dat", "UTF-8");
        num_of_token=0;
        num_of_words_in_dict=0;
        for(int i=1;i<1063;i++){
          SetWord(i);
          writer.println(Math.log10(num_of_token)+"\t"+Math.log10(num_of_words_in_dict));
        }
        writer.close();
        List<Map.Entry<String, HashMap<Integer,Integer>>> sorted_inverted_index = Sort_map_desc();
        writer = new PrintWriter("section3_part2.dat", "UTF-8");
        for(int i=0 ; i < sorted_inverted_index.size() ; i++)
          writer.println(Math.log10(i+1)+"\t"+Math.log10(sorted_inverted_index.get(i).getValue().size()));
        writer.close();
        writer = new PrintWriter("section3_part3.dat", "UTF-8");
        HashMap<Integer, Integer> gaps = gaps_bin(invertedIndex);
        for (Map.Entry<Integer, Integer> entry : gaps.entrySet()){
          writer.println((entry.getKey()*20)+"-"+ (entry.getKey()*20+20) + "\t" + entry.getValue());
        }
        writer.close();
        Runtime commandPrompt = Runtime.getRuntime();
        commandPrompt.exec("gnuplot -p auxfile.gp");
        commandPrompt.exec("gnuplot -p auxfile2.gp");
        commandPrompt.exec("gnuplot -p auxfile3.gp");
        try{
          commandPrompt.wait();
        }catch(Exception e){
          System.out.println(e.getMessage());
        }
      } catch (Exception e) {
      }
      System.out.println("Step3 : ");
      Print();
      // Form inverted index by removing least frequent terms (Step 4)
      LeastFrequent(invertedIndex);
      System.out.println("Step4 : ");
      Print();
      statistics_section2();  // Entry point for Section II
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
};
