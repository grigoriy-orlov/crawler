package ru.ares4322.crawler;

import com.ning.http.client.*;
import java.io.ByteArrayOutputStream;
import com.ning.http.client.HttpResponseHeaders;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class HttpClient implements Runnable {

	private final String startPath;
	private final BlockingQueue<String> pageQueue;
	private static final Logger logger = Logger.getLogger(HttpClient.class.getName());
	private final Queue<String> linkQueue = new ArrayDeque<>();
	private final Map<String, Integer> tagMap = new HashMap<>();

	public HttpClient(String startPath, BlockingQueue<String> pageQueue) {
		this.startPath = startPath;
		this.pageQueue = pageQueue;
		linkQueue.add(startPath);
	}

	@Override
	public void run() {
		try {

			AsyncHttpClient httpClient = new AsyncHttpClient();
			AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder(httpClient.getConfig()).setExecutorService(null).build();
			int availableProcessors = Runtime.getRuntime().availableProcessors();
			if (availableProcessors < 1) {
				availableProcessors = 1;
			}
			//ExecutorService pool = Executors.newFixedThreadPool(availableProcessors);

			while (!linkQueue.isEmpty()) {
				String url = linkQueue.poll();

				System.out.println("--->get " + url);
				Future<String> f = httpClient.prepareGet(url).execute(new AsyncHandler<String>() {
					private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

					@Override
					public AsyncHandler.STATE onStatusReceived(HttpResponseStatus status) throws Exception {
						AsyncHandler.STATE result = AsyncHandler.STATE.CONTINUE;
						int statusCode = status.getStatusCode();
						if (statusCode != 200) {
							result = AsyncHandler.STATE.ABORT;
						}
						return result;
					}

					@Override
					public AsyncHandler.STATE onHeadersReceived(HttpResponseHeaders h) throws Exception {
						AsyncHandler.STATE result = AsyncHandler.STATE.ABORT;
						FluentCaseInsensitiveStringsMap headers = h.getHeaders();
						List<String> contentTypes = headers.get("content-type");
						if (contentTypes != null) {
							for (Iterator<String> it = contentTypes.iterator(); it.hasNext();) {
								String contentType = it.next();
								if (contentType.contains("text/html")) {
									result = AsyncHandler.STATE.CONTINUE;
								}
							}
						}
						return result;
					}

					@Override
					public AsyncHandler.STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
						bytes.write(bodyPart.getBodyPartBytes());
						return AsyncHandler.STATE.CONTINUE;
					}

					@Override
					public String onCompleted() throws Exception {
						String result = bytes.toString("UTF-8");

						Document doc = Jsoup.parse(result, startPath);
						Element body = doc.body();

						body.traverse(new NodeVisitor() {
							@Override
							public void head(Node node, int depth) {
								String name = node.nodeName();
								if (!name.equals("#text")) {
									Integer count = tagMap.get(name);
									if (count == null) {
										tagMap.put(name, Integer.valueOf(1));
									} else {
										tagMap.put(name, count + 1);
									}
									if (name.equals("a")) {
										boolean addResult = linkQueue.add(node.absUrl("href"));
										if (addResult == false) {
											System.out.println("duplicate link: " + name);
										}
									}
								}
							}

							@Override
							public void tail(Node node, int depth) {
								//System.out.println("Exiting tag: " + node.nodeName());
							}
						});
						return result;
					}

					@Override
					public void onThrowable(Throwable t) {
					}
				});
				f.get();
//                System.out.println("HTTP bodyResponse -----> ");
//                System.out.println(bodyResponse);
//                System.out.println("---->");

				//pool.submit(new HTMLParser(bodyResponse));
				//pageQueue.put(bodyResponse);
				for (Iterator<Map.Entry<String, Integer>> it = tagMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry<String, Integer> tm = it.next();
					System.out.println("tag: " + tm.getKey() + ", count: " + tm.getValue());
				}
			}
		} catch (IOException | InterruptedException | ExecutionException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
}
