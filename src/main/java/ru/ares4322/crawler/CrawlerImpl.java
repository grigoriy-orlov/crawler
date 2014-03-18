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
	private final static int CONTROL_SEMAPHORE_PERMITS = 2;
	@Inject
	private PageRequesterService requesterService;
	@Inject
	private LinkExtractorService extractorService;
	@Inject
	private CrawlerProperties props;

	@Override
	public void run() throws Exception {
		log.debug("run crawler");

		BlockingQueue<URL> urlQueue = newArrayBlockingQueue(props.getUrlQueueSize());
		BlockingQueue<String> pageQueue = newArrayBlockingQueue(props.getPageQueueSize());
		Semaphore controlSemaphore = new Semaphore(CONTROL_SEMAPHORE_PERMITS);

		requesterService.setInputQueue(urlQueue);
		requesterService.setOutputQueue(pageQueue);
		requesterService.setControlSemaphore(controlSemaphore);
		extractorService.setInputQueue(pageQueue);
		extractorService.setOutputQueue(urlQueue);
		extractorService.setControlSemaphore(controlSemaphore);

		for (URL startUrl : props.getStartUrls()) {
			urlQueue.add(startUrl);
		}

		requesterService.startAsync().awaitRunning();
		extractorService.startAsync().awaitRunning();

		while (true) {
			try {
				controlSemaphore.acquire(2);
				log.debug("control semaphore acquire");
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
