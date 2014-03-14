package ru.ares4322.crawler;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import static com.google.common.collect.Queues.newArrayBlockingQueue;

@Singleton
public class CrawlerImpl extends AbstractExecutionThreadService implements Crawler {

	private static final Logger log = LoggerFactory.getLogger(CrawlerImpl.class);
	private final static int URL_QUEUE_SIZE = 10;
	private final static int PAGE_QUEUE_SIZE = 10;
	private final static int CONTOL_SEMAPHORE_PERMITS = 2;
	@Inject
	private PageRequesterService requesterService;
	@Inject
	private LinkExtractorService extractorService;

	@Override
	public void run() throws Exception {
		log.debug("run crawler");

		BlockingQueue<URL> urlQueue = newArrayBlockingQueue(URL_QUEUE_SIZE);
		BlockingQueue<String> pageQueue = newArrayBlockingQueue(PAGE_QUEUE_SIZE);
		Semaphore controlSemaphore = new Semaphore(CONTOL_SEMAPHORE_PERMITS);

		requesterService.setInputQueue(urlQueue);
		requesterService.setOutputQueue(pageQueue);
		requesterService.setControlSemaphore(controlSemaphore);
		extractorService.setInputQueue(pageQueue);
		extractorService.setOutputQueue(urlQueue);
		extractorService.setControlSemaphore(controlSemaphore);

		urlQueue.add(new URL("http://navtelecom.ru"));    //TODO add start url

		extractorService.startAsync();
		requesterService.startAsync();

		while (true) {
			try {
				controlSemaphore.acquire(2);
				break;
			} catch (InterruptedException e) {
				log.error("interrupt on controlSemaphore acquiring: {}", e);
			}
		}
		log.debug("stop crawler");
	}

	@Override
	protected void shutDown() {
		log.debug("shut down crawler");

		requesterService.stopAsync();
		extractorService.stopAsync();
	}
}
