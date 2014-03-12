package ru.ares4322.crawler;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import static com.google.common.collect.Queues.newArrayBlockingQueue;

@Singleton
public class CrawlerImpl implements Crawler {

	private static final Logger log = LoggerFactory.getLogger(CrawlerImpl.class);

	@Inject
	private PageRequesterService requesterService;
	@Inject
	private LinkExtractorService extractorService;
	@Inject
	private Injector injector;
	private final static int URL_QUEUE_SIZE = 10;
	private final static int PAGE_QUEUE_SIZE = 10;

	@Override
	public void start() throws Exception {
		BlockingQueue<URL> urlQueue = newArrayBlockingQueue(URL_QUEUE_SIZE);
		BlockingQueue<String> pageQueue = newArrayBlockingQueue(PAGE_QUEUE_SIZE);

		requesterService.setInputQueue(urlQueue);
		requesterService.setOutputQueue(pageQueue);
		extractorService.setInputQueue(pageQueue);
		extractorService.setOutputQueue(urlQueue);

		requesterService.start();
		extractorService.start();

		urlQueue.add(null);    //TODO add start url
	}

	@Override
	public void stop() throws Exception {
		requesterService.stop();
		extractorService.stop();
	}
}
