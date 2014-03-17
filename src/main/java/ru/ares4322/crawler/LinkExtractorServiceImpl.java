package ru.ares4322.crawler;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class LinkExtractorServiceImpl extends AbstractExecutionThreadService implements LinkExtractorService {

	private static final Logger log = getLogger(LinkExtractorServiceImpl.class);
	private static final int OUTPUT_QUEUE_READD_TIMEOUT_MS = 5000;
	private static final int INTPUT_QUEUE_TAKE_TIMEOUT_MS = 10000;
	private static final int THREADS = 4;
	private BlockingQueue<String> inputQueue;
	private BlockingQueue<URL> outputQueue;
	private boolean mustStop = false;
	private Semaphore controlSemaphore;
	private ExecutorService executor;

	@Override
	protected void startUp() throws Exception {
		log.debug("start up link extractor");

		try {
			controlSemaphore.acquire(1);
		} catch (InterruptedException e) {
			log.error("control semaphore acquire interrupted exception: {}", e);
		}

		executor = newFixedThreadPool(THREADS, new ThreadFactoryBuilder().setNameFormat("link-extractor-%d").build());
	}

	@Override
	public void run() {
		log.debug("run link extractor");

		while (!mustStop) {
			final String page;
			try {
				page = inputQueue.poll(INTPUT_QUEUE_TAKE_TIMEOUT_MS, MILLISECONDS);
				if (null == page) {
					log.debug("input queue poll timeout exceed");
					controlSemaphore.release();
					return;
				}
				log.debug("get new page for extracting");
			} catch (InterruptedException e) {
				log.error("page getting from queue interrupted exception: {}", e);
				continue;
			}
			executor.submit(new Runnable() {
				@Override
				public void run() {
					log.debug("start link extractor task");

					Document doc = Jsoup.parse(page);
					Element body = doc.body();
					body.traverse(new NodeVisitor() {
						public void head(Node node, int depth) {
							if ("a".equals(node.nodeName())) {    //TODO add other links node parsing
								String href = node.attr("href");
								if (null != href) {
									try {
										log.debug("try add link to queue");
										while (!outputQueue.offer(new URL(href))) {
											log.debug("output queue is full");
											try {
												sleep(OUTPUT_QUEUE_READD_TIMEOUT_MS);
											} catch (InterruptedException e) {
												log.error("link putting to queue interrupted exception: {}", e);
												return;
											}
										}
										log.debug("link to queue added");
									} catch (MalformedURLException e) {
										log.error("link {} has wrong href attribute", node.toString());
									}
								}
							}
						}

						@Override
						public void tail(Node node, int depth) {
						}
					});

					log.debug("finish link extractor task");
				}
			});
		}
	}

	@Override
	public void shutDown() {
		log.debug("shut down link extractor");

		mustStop = true;
		executor.shutdown();
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
