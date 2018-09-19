import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.StringTokenizer;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.io.File;
import java.io.PrintWriter;

class WebCrawler{

  //To store the urls to be vistied
  public static PriorityQueue<String> url_queue;

  //To store the sub category name of url
  public static PriorityQueue<String> common_text_queue;

  //To keep count of previously visited urls
  public static int doc_id;

  //To keep track of already visited urls
  public static HashSet<String> urls_visited;

  // Extact the content of article from html
  public static void extract_content(String url , Document document) {
    try {
      // Extract meta keywords from meta tags
      Elements metaTags = document.getElementsByTag("meta");
      String meta_keywords = "";
      for (Element metaTag : metaTags) {
        if("keywords".equals( metaTag.attr("name").toLowerCase() )) {
          meta_keywords = metaTag.attr("content");
        }
      }

      // Extract title (first h1 tag of content)
      Element title_elt = document.select("div.content h1").first();
      String title = title_elt.text();
      title_elt.text("");

      // remove the text of unnecessaary buttons from text
      document.select("div.topgooglead").remove();
      document.select("div.tutorial-menu").remove();
      document.select("div.pre-btn").remove();
      document.select("div.print-btn").remove();
      document.select("div.pdf-btn").remove();
      document.select("div.nxt-btn").remove();
      document.select("div.bottomgooglead").remove();

      // get content fo article
      Element contentDiv = document.select("div.content").first();
      String content = contentDiv.text();

      // check if content has > 150 words
      StringTokenizer st = new StringTokenizer(content," ");
      if (st.countTokens() < 150 )
        return;

      doc_id++;

      String DateModified = "";
      DateModified = FindDate(document);
      while(DateModified=="tryagain"){
        Document newdocument = Jsoup.connect(url).timeout(0).get();
        DateModified = FindDate(newdocument);
      }
      String[] Date = DateModified.split("\":");
      StringTokenizer datewords = new StringTokenizer(Date[1]," \".");
      String ChangedDate =datewords.nextToken();
      while(datewords.hasMoreTokens()){
        ChangedDate = ChangedDate+"-"+datewords.nextToken();
      }
      MakeFile(title,ChangedDate,url,doc_id,meta_keywords,content);
      System.out.println(String.valueOf(doc_id) + " : "+ title );
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public static String FindDate(Document document){
    // Extract date from script
    Elements Scripts = document.select("script[type=application/ld+json]");
    String Script = Scripts.html();
    try{
      StringTokenizer sc = new StringTokenizer(Script,",");
      while (sc.hasMoreTokens()) {
        String checkDate =sc.nextToken();
        if(checkDate.contains("dateModified"))
          return checkDate;
      }
    } catch(Exception e){
      System.err.println(e.getMessage());
      return "\": fetching error try again";
    }
    return "tryagain";
  }

  public static void MakeFile(String title,String date,String url,int doc_id,String meta_keywords,String content) throws Exception{
    try{
      // create and print to file
      String fileName = "Crawled_data/Doc_Id-"+String.valueOf(doc_id)+".txt";
      PrintWriter writer = new PrintWriter(fileName, "UTF-8");
      writer.println(title + "\n");
      writer.println(url+"\n");
      writer.println(meta_keywords+"\n");
      writer.println(date+"\n");
      writer.println(content);
      writer.close();
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }

  //method to extract all urls within seed domain using bfs
  public static void extractSubLinks() throws Exception {
    String url;
    while((url = url_queue.poll()) != null){
      String common_text = common_text_queue.poll();
      try {
        Document document = Jsoup.connect(url).timeout(0).get();
        extract_content(url , document);
        Elements links = document.getElementsByTag("a");
        for (Element link : links) {
          String abs_link = link.attr("abs:href").toString();
          if(abs_link.contains("tutorialspoint.com/"+common_text) && abs_link.endsWith(".htm") && urls_visited.add(abs_link)) {
            url_queue.add(abs_link);
            common_text_queue.add(common_text);
          }
        }
      } catch(Exception e) {
        System.err.println(e.getMessage());
      }
    }
  }

  //method to extract all index urls from a subcategory with given id
  public static void extractLinks(String url,String id) throws Exception {
    try {
      Document document = Jsoup.connect(url).timeout(0).get();
      Element sampleDiv = document.getElementById(id);
      Elements links3 = sampleDiv.getElementsByTag("a");
      for (Element link : links3) {
        String abs_link = link.attr("abs:href").toString();
        if(abs_link.contains("tutorialspoint.com/") && abs_link.contains("/index.htm")) {
          StringTokenizer st = new StringTokenizer(abs_link,"/");
          if (st.countTokens() == 4 && urls_visited.add(abs_link)){
            st.nextToken(); st.nextToken();
            String common_text = st.nextToken();
            url_queue.add(abs_link);
            common_text_queue.add(common_text);
          }
        }
      }
      extractSubLinks();
    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public static void main(String[] args){
    try {
      //Creating directory for storing created files of extracted content.
      File directory = new File("Crawled_data");
      if (! directory.exists())
        directory.mkdir();

      url_queue = new PriorityQueue<String>();
      common_text_queue = new PriorityQueue<String>();
      urls_visited = new HashSet<String>();
      doc_id = 0;

      //Crawling urls belonging to databases and xml technologies in Tutorials Point.
      extractLinks("https://www.tutorialspoint.com/tutorialslibrary.htm","database_tutorials");
      extractLinks("https://www.tutorialspoint.com/tutorialslibrary.htm","xml_technologies");
      System.out.println("Total URLs visited : "+doc_id);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
};
