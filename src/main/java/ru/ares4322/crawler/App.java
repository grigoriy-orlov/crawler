package ru.ares4322.crawler;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.Guice.createInjector;

public class App {
	private static final Logger log = LoggerFactory.getLogger(App.class);

	//TODO implements Daemon
	public static void main(String[] args) {
		log.debug("start app");

		Injector injector = createInjector();

		Crawler crawler = injector.getInstance(Crawler.class);
		try {
			crawler.startAsync().awaitTerminated();
		} catch (Exception e) {
			log.error("crawler starting error: {}", e);
		}
	}
}
