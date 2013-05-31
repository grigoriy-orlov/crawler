package ru.ares4322.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Администратор
 */
public class HTMLParser implements Callable {

    private final BlockingQueue<String> pageQueue;
    //private final String page;
    private static final Logger logger = Logger.getLogger(HTMLParser.class.getName());

    public HTMLParser(BlockingQueue<String> pageQueue) {
        this.pageQueue = pageQueue;
    }

//    public HTMLParser(String page) {
//        this.page = page;
//    }

	@Override
    public List<TagCount> call() throws Exception {
        //@todo надо делать как SAX, чтоб память экономить
        List<TagCount> result = new ArrayList<TagCount>();  //можно сразу выделять больше
        Document doc = Jsoup.parse(null);
        Element body = doc.body();
        body.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                System.out.println("Entering tag: " + node.nodeName());
            }

            public void tail(Node node, int depth) {
                System.out.println("Exiting tag: " + node.nodeName());
            }
        });

        return result;
    }
}
