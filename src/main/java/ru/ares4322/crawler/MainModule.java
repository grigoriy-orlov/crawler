package ru.ares4322.crawler;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

public class MainModule extends AbstractModule {

	private static final Logger log = getLogger(MainModule.class);
	private static final String crawlerProps = "crawler.properties";

	@Override
	protected void configure() {
		try {
			binder().bind(CrawlerProperties.class).toInstance(getProps());
		} catch (IOException ex) {
			throw new RuntimeException("main module configure error: " + ex);
		}
	}

	private static CrawlerProperties getProps() throws IOException {
		CrawlerProperties result = new CrawlerProperties();
		Properties props = new Properties();
		props.load(ClassLoader.getSystemResourceAsStream(crawlerProps));

		result.setStartUrls(getStartUrls(props.getProperty("crawler.startLinks")));
		result.setPageQueueSize(valueOf(props.getProperty("crawler.pageQueueSize")));
		result.setUrlQueueSize(valueOf(props.getProperty("crawler.urlQueueSize")));
		result.setLinkExtractorOfferTimeoutMs(valueOf(props.getProperty("crawler.linkExtractor.offerTimeoutMs")));
		result.setLinkExtractorPollTimeoutMs(valueOf(props.getProperty("crawler.linkExtractor.pollTimeoutMs")));
		result.setLinkExtractorThreads(valueOf(props.getProperty("crawler.linkExtractor.threads")));
		result.setPageRequesterOfferTimeoutMs(valueOf(props.getProperty("crawler.pageRequester.offerTimeoutMs")));
		result.setPageRequesterPollTimeoutMs(valueOf(props.getProperty("crawler.pageRequester.pollTimeoutMs")));
		result.setPageRequesterThreads(valueOf(props.getProperty("crawler.pageRequester.threads")));

		return result;
	}

	private static List<URL> getStartUrls(String str) throws MalformedURLException {
		List<URL> result = newArrayList();
		if (null != str && !str.isEmpty()) {
			String[] links = str.split(",");
			for (String link : links) {
				result.add(new URL(link));
			}
		}
		return result;
	}

}
