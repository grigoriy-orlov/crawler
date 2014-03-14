package ru.ares4322.crawler;

import com.google.inject.ImplementedBy;

@ImplementedBy(CrawlerImpl.class)
public interface Crawler {
	void start() throws Exception;

	void stop() throws Exception;
}
