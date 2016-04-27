import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by dovile on 2016-04-19.
 */
public class Crawler extends WebCrawler

{
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");

    private int fileIndex = 1;
    private int komentFileIndex = 1;
    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("http://www.15min.lt/");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();




            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());

            try {
                if(!url.contains("?"))
                    parseHTML(url);
                parseComment(url + "?comments");

            }
            catch (IOException e){
                e.printStackTrace();
            }
//            try {
//                parseComment(url + "?comments");
//
//            }
//            catch (IOException e){
//                e.printStackTrace();
//            }



        }
    }

    private void writeText(String text) throws IOException {
        Writer writer = null;
        File dir = new File("./data/"+fileIndex+"/");
        dir.mkdir();
        String fileName = "./data/"+fileIndex+"/"+fileIndex+".txt";
        fileIndex++;
        File yourFile = new File(fileName);

        yourFile.createNewFile();
        writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(yourFile), "utf-8"));

        writer.write(text);
        writer.flush();
        writer.close();
    }

    private void writeText(String textToWrite, String fileName) throws IOException {
        Writer writer = null;
        File yourFile = new File(fileName);

        yourFile.createNewFile();
        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(yourFile), "utf-8"));

        writer.write(textToWrite);
        writer.flush();
        writer.close();
    }

    private void parseComment(String url) throws IOException {

        Document doc = Jsoup.connect(url).get();
        komentFileIndex=1;
        /*STRAIPSNIS*/
        Writer writer = null;
        int fileIndexForComment = fileIndex-1;
        Elements scriptElements = doc.getElementsByTag("script");
        String script="";
        for (Element element :scriptElements ){
            for (DataNode node : element.dataNodes()) {
                if(node.getWholeData().contains("div.comments-placeholder")){
                    script = node.getWholeData();
                    script = script.substring(41);
                    script = script.substring(0,script.length()-1);
                }
            }
        }


        JSONObject obj = new JSONObject(script);

        JSONArray comments = obj.getJSONArray("comments");
        for (int i = 0; i < comments.length(); i++)
        {
            String commentInfo = "";
            String comment = comments.getJSONObject(i).getString("comment");
            String user_name = comments.getJSONObject(i).getString("user_name");
            String user_ip = comments.getJSONObject(i).getString("user_ip");
            String date = comments.getJSONObject(i).getString("date");
            JSONArray replies = comments.getJSONObject(i).getJSONArray("replies");
            commentInfo =url+"\nNumeris: " + i +
                    "\nAutorius: " + user_name +
                    "\nIP: "+ user_ip +
                    "\nData: " + date +
                    "\nKomentaras: " + comment+"\n";
            String fileName = "./data/"+fileIndexForComment+"/"+fileIndexForComment+"koment"+komentFileIndex+".txt";
            File yourFile = new File(fileName);
                yourFile.createNewFile();
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(yourFile), "utf-8"));
            writer.write(commentInfo);
            writer.flush();
            writer.close();
            getReplies(replies,url);
            System.out.println(i + " " +comment);
            komentFileIndex++;
        }
    }

    private void getReplies(JSONArray replies, String url) throws IOException {
        Writer writer = null;
        int fileIndexForComment = fileIndex-1;
        for (int i = 0; i < replies.length(); i++)
        {
            komentFileIndex++;
            String comment = replies.getJSONObject(i).getString("comment");
            String user_name = replies.getJSONObject(i).getString("user_name");
            String user_ip = replies.getJSONObject(i).getString("user_ip");
            String date = replies.getJSONObject(i).getString("date");
            String commentInfo = url+"\nNumeris: " + i +
                    "\nAutorius: " + user_name +
                    "\nIP: "+ user_ip +
                    "\nData: " + date +
                    "\nKomentaras: " + comment+"\n";
            System.out.println(i + " reply " +comment);

            String fileName = "./data/"+fileIndexForComment+"/"+fileIndexForComment+"koment"+komentFileIndex+".txt";
            File yourFile = new File(fileName);
                yourFile.createNewFile();
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(yourFile), "utf-8"));
            writer.write(commentInfo);
            writer.flush();
            writer.close();
        }
    }


    private void parseHTML(String url) throws IOException{

        Document doc = Jsoup.connect(url).get();
        String authorName = "";
        String date = "";
        /*STRAIPSNIS*/
        //Tema
       //Autorius
        Element authorDIV = doc.select("div.author-info").first();
        if (authorDIV!=null){
            authorName = authorDIV.tagName("a.name").text();
        }


        //Pavadinimas
        String h1 = doc.select("h1").text();
        String articalName = h1.substring(0,20)+doc.select("div").attr("data-id");

        //Data

        //get meta description content
        //String description = doc.select("meta[itemprop=datePublished]").get(0).attr("content");
        //System.out.println("Meta description : " + description);

        //get meta keyword content
//        String keywords = doc.select("meta[itemprop=datePublished]").first().attr("content");
//        System.out.println("Meta keyword : " + keywords);

        Elements dateMeta = doc.select("div.article-info");

        if (dateMeta!=null){
            date = dateMeta.attr("content");
        }

        //Tema
        String temma = doc.select("div.category-path a").last().text();

        //Laikas
        String time = doc.select("span.published").text();
        //Tekstas
        String articalText = doc.select("div.article_content p").text();
        /*Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");*/


        if(articalText != null){//straipsnis
            File dir = new File("./data/"+fileIndex+"/");
            dir.mkdir();
            String fileName = "./data/"+fileIndex+"/"+fileIndex+".txt";
            String articleInfo = url+"\nTema: "+temma+"\nAutorius: "+authorName+"\n"+"Pavadinimas: "+
                    h1+"\n"+"Laikas: "+time+"\n"+"Data: "+date+"\n"+"Straipsnio tekstas: "+articalText+"\n";
            writeText(articleInfo,fileName);
            fileIndex++;
        }

    }





        }








