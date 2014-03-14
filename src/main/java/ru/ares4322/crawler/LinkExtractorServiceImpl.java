package ru.ares4322.crawler;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class LinkExtractorServiceImpl implements LinkExtractorService {

	private static final Logger log = getLogger(LinkExtractorServiceImpl.class);
	private static final int OUTPUT_QUEUE_READD_TIMEOUT_MS = 5000;
	private static final int INTPUT_QUEUE_TAKE_TIMEOUT_MS = 1000;
	private BlockingQueue<String> inputQueue;
	private BlockingQueue<URL> outputQueue;
	private boolean mustStop = false;
	private Semaphore controlSemaphore;

	@Override
	public void start() {
		try {
			controlSemaphore.acquire();
		} catch (InterruptedException e) {
			log.error("control semaphore acquire interrupted exception: {}", e);
		}

		while (!mustStop) {
			final String page;
			try {
				page = inputQueue.poll(INTPUT_QUEUE_TAKE_TIMEOUT_MS, MILLISECONDS);
				if (null == page) {
					log.debug("input queue poll timeout exceed");
					controlSemaphore.release();
					return;
				}
			} catch (InterruptedException e) {
				log.error("page getting from queue interrupted exception: {}", e);
				continue;
			}
			Document doc = Jsoup.parse(page);
			Element body = doc.body();
			body.traverse(new NodeVisitor() {
				public void head(Node node, int depth) {
					if ("link".equals(node.nodeName())) {    //TODO add other links node parsing
						String href = node.attr("href");
						if (null != href) {
							try {
								while (!outputQueue.add(new URL(href))) {
									log.debug("output queue is full");
									try {
										sleep(OUTPUT_QUEUE_READD_TIMEOUT_MS);
									} catch (InterruptedException e) {
										log.error("link putting to queue interrupted exception: {}", e);
										return;
									}
								}
							} catch (MalformedURLException e) {
								log.error("link {} has wrong href attribute", node.toString());
							}
						}
					}
				}

				public void tail(Node node, int depth) {
				}
			});
		}
	}

	@Override
	public void stop() {
		mustStop = true;
	}

	@Override
	public void setInputQueue(@NotNull BlockingQueue<String> inputQueue) {
		this.inputQueue = inputQueue;
	}

	@Override
	public void setOutputQueue(@NotNull BlockingQueue<URL> outputQueue) {
		this.outputQueue = outputQueue;
	}

	@Override
	public void setControlSemaphore(@NotNull Semaphore semaphore) {
		this.controlSemaphore = semaphore;
	}
}
