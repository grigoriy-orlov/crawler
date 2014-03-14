package ru.ares4322.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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

public class PageRequesterServiceImpl implements PageRequesterService {

	private static final Logger log = getLogger(PageRequesterServiceImpl.class);
	private static final int THREADS = 2;
	private static final int OUTPUT_QUEUE_READD_TIMEOUT_MS = 5000;
	private static final int INTPUT_QUEUE_TAKE_TIMEOUT_MS = 1000;
	private BlockingQueue<URL> inputQueue;
	private BlockingQueue<String> outputQueue;

	private boolean mustStop = false;
	private Semaphore controlSemaphore;

	@Override
	public void start() {
		try {
			controlSemaphore.acquire();
		} catch (InterruptedException e) {
			log.error("control semaphore acquire interrupted exception: {}", e);
		}

		ExecutorService executor = newFixedThreadPool(THREADS);

		while (!mustStop) {
			final URL url;
			try {
				url = inputQueue.poll(INTPUT_QUEUE_TAKE_TIMEOUT_MS, MILLISECONDS);
				if (null == url) {
					log.debug("input queue poll timeout exceed");
					controlSemaphore.release();
					return;
				}
			} catch (InterruptedException e) {
				log.error("page getting from queue interrupted exception: {}", e);
				continue;
			}
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try (CloseableHttpClient client = createDefault()) {
						try (CloseableHttpResponse response = client.execute(new HttpGet(url.toURI()))) {
							final HttpEntity entity = response.getEntity();
							final String page = new String(toByteArray(entity.getContent()));
							while (!outputQueue.add(page)) {
								log.debug("output queue is full");
								try {
									sleep(OUTPUT_QUEUE_READD_TIMEOUT_MS);
								} catch (InterruptedException e) {
									log.error("page putting to queue interrupted exception: {}", e);
									return;
								}
							}
							consume(entity);
						}
					} catch (IOException e) {
						log.error("page getting error: {}", e);
					} catch (URISyntaxException e) {
						log.error("page uri error: {}", e);
					}
				}
			});
		}
	}

	@Override
	public void stop() {
		mustStop = true;
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

}
