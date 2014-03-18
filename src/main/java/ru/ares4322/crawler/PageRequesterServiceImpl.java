package ru.ares4322.crawler;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.apache.http.util.EntityUtils.consume;
import static org.slf4j.LoggerFactory.getLogger;

public class PageRequesterServiceImpl extends AbstractExecutionThreadService implements PageRequesterService {

	private static final Logger log = getLogger(PageRequesterServiceImpl.class);
	private BlockingQueue<URL> inputQueue;
	private BlockingQueue<String> outputQueue;

	private boolean mustStop = false;
	private Semaphore controlSemaphore;
	private ExecutorService executor;

	@Inject
	private CrawlerProperties props;

	@Override
	protected void startUp() throws Exception {
		log.debug("start up page requester");

		try {
			controlSemaphore.acquire(1);
		} catch (InterruptedException e) {
			log.error("control semaphore acquire interrupted exception: {}", e);
		}

		executor = newFixedThreadPool(props.getPageRequesterThreads(), new ThreadFactoryBuilder().setNameFormat("page-requester-%d").build());
	}

	@Override
	protected void run() throws Exception {
		log.debug("run page requester");

		while (!mustStop) {
			final URL url;
			try {
				url = inputQueue.poll(props.getPageRequesterPollTimeoutMs(), MILLISECONDS);
				if (null == url) {
					log.debug("input queue poll timeout exceed");
					controlSemaphore.release();
					return;
				}
				log.debug("get new link for request : {}", url.toString());
			} catch (InterruptedException e) {
				log.error("page getting from queue interrupted exception: {}", e);
				continue;
			}
			executor.submit(new Runnable() {
				@Override
				public void run() {
					log.debug("run page requesting task");
					try (CloseableHttpClient client = createDefault()) {
						try (CloseableHttpResponse response = client.execute(new HttpGet(url.toURI()))) {
							final HttpEntity entity = response.getEntity();
							final String page = new String(toByteArray(entity.getContent()));
							log.debug("try add page to queue");
							while (!outputQueue.offer(page)) {
								log.debug("output queue is full");
								try {
									sleep(props.getPageRequesterOfferTimeoutMs());
								} catch (InterruptedException e) {
									log.error("page putting to queue interrupted exception: {}", e);
									return;
								}
							}
							log.debug("page to queue added");
							consume(entity);
						}
					} catch (IOException e) {
						log.error("page getting error: {}", e);
					} catch (URISyntaxException e) {
						log.error("page uri error: {}", e);
					} finally {
						log.debug("finish  page requesting task");
					}
				}
			});
		}
	}


	@Override
	public void setInputQueue(@NotNull BlockingQueue<URL> inputQueue) {
		this.inputQueue = inputQueue;
	}

	@Override
	public void setOutputQueue(@NotNull BlockingQueue<String> outputQueue) {
		this.outputQueue = outputQueue;
	}

	@Override
	public void setControlSemaphore(@NotNull Semaphore semaphore) {
		this.controlSemaphore = semaphore;
	}

	@Override
	public void shutDown() {
		log.debug("shut down page requester");

		mustStop = true;
		executor.shutdown();
	}

}
