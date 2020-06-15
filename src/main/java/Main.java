import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends WebCrawler {

    private static final String fileLocation = "D:\\WebPages\\Binary\\Training\\Negative";

    //RegEx Filters
    private static Pattern FILTER_REFERRALS;
    private static Pattern FILTER_CHECK_REFERRALS;
    //Lists of referral url's
    private static final String[] REFERRALS = {
            "https://www.tvnet.lv/section/4228",
            "https://puaro.lv/category/politika/"
    };
    private static final String[] CHECK_REFERRALS = {
            "https://www.tvnet.lv/",
            "https://puaro.lv/"
    };
    //Prepare RegEx filters
    public static void prepareReferralFilters(){
        FILTER_REFERRALS = Pattern.compile(Arrays.stream(REFERRALS)
                .map(s -> String.format("(%s)", s))
                .collect(Collectors.joining("|"))
        );
        FILTER_CHECK_REFERRALS = Pattern.compile(Arrays.stream(CHECK_REFERRALS)
                .map(s -> String.format("(%s\\S*)", s))
                .collect(Collectors.joining("|"))
        );
    }

    //RegEx Filters
    private static Pattern FILTER_URLS;
    //Lists of url's
    private static final String[] URLS = {
            "http://www.robertszile.lv/",
            "https://www.delfi.lv/sports/"
    };
    //Prepare RegEx filters
    public static void prepareUrlsFilter(){
        FILTER_URLS = Pattern.compile(Arrays.stream(URLS)
                .map(s -> String.format("(%s\\S*)", s))
                .collect(Collectors.joining("|"))
        );
    }

    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "/data/crawl/root";
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        Arrays.stream(URLS).forEach(controller::addSeed);
        Arrays.stream(REFERRALS).forEach(controller::addSeed);

        //Prepare filters
        prepareUrlsFilter();
        prepareReferralFilters();
        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<WebCrawler> factory = Main::new;

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return (FILTER_CHECK_REFERRALS.matcher(url.getURL().toLowerCase()).matches()
               && FILTER_REFERRALS.matcher(referringPage.getWebURL().getURL().toLowerCase()).matches())
               || FILTER_URLS.matcher(url.getURL().toLowerCase()).matches();
    }

    @Override
    public void visit(Page page) {
        if(page.getContentType().toLowerCase().contains("html")){
            try {
                String file = fileLocation + "\\" + page.getWebURL().getURL().replaceAll("[<>?/\\\\|*:]", "_") + ".html";
                FileUtils.writeByteArrayToFile(new File(file), page.getContentData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
