package com.lsmsdbgroup.pisaflix.pisaflixservices;

import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WikiScraper extends Task<String> {
   
    private final String url;
    private String posterLink;
            
    public WikiScraper(String url){
       this.url = url;
    }
    
    public String scrapePosterLink(){
        Document doc;
        String link = null;
        try {
            doc = Jsoup.connect(url).get();
            if(!doc.body().getElementsByClass("summary").isEmpty()){
                link = doc.body().getElementsByClass("summary").first().parent().parent().getElementsByTag("img").attr("src");
            }        
        } catch (Exception ex) {
            System.out.println("Scraper Error: " + ex.getMessage());
        }
        return link;
    }
    
    public String getPosterLink(){
        return posterLink;
    }

    @Override
    protected String call() throws Exception {
        posterLink = scrapePosterLink();
        return scrapePosterLink();
    }
}
