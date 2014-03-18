package ru.ares4322.crawler;

import javax.inject.Singleton;
import java.net.URL;
import java.util.List;

//TODO improve
@Singleton
public class CrawlerProperties {

	private int urlQueueSize;
	private int pageQueueSize;
	private int pageRequesterThreads;
	private int linkExtractorThreads;
	private int pageRequesterPollTimeoutMs;
	private int pageRequesterOfferTimeoutMs;
	private int linkExtractorPollTimeoutMs;
	private int linkExtractorOfferTimeoutMs;
	private List<URL> startUrls;

	public int getUrlQueueSize() {
		return urlQueueSize;
	}

	public void setUrlQueueSize(int urlQueueSize) {
		this.urlQueueSize = urlQueueSize;
	}

	public int getPageQueueSize() {
		return pageQueueSize;
	}

	public void setPageQueueSize(int pageQueueSize) {
		this.pageQueueSize = pageQueueSize;
	}

	public int getPageRequesterThreads() {
		return pageRequesterThreads;
	}

	public void setPageRequesterThreads(int pageRequesterThreads) {
		this.pageRequesterThreads = pageRequesterThreads;
	}

	public int getLinkExtractorThreads() {
		return linkExtractorThreads;
	}

	public void setLinkExtractorThreads(int linkExtractorThreads) {
		this.linkExtractorThreads = linkExtractorThreads;
	}

	public int getPageRequesterPollTimeoutMs() {
		return pageRequesterPollTimeoutMs;
	}

	public void setPageRequesterPollTimeoutMs(int pageRequesterPollTimeoutMs) {
		this.pageRequesterPollTimeoutMs = pageRequesterPollTimeoutMs;
	}

	public int getPageRequesterOfferTimeoutMs() {
		return pageRequesterOfferTimeoutMs;
	}

	public void setPageRequesterOfferTimeoutMs(int pageRequesterOfferTimeoutMs) {
		this.pageRequesterOfferTimeoutMs = pageRequesterOfferTimeoutMs;
	}

	public int getLinkExtractorPollTimeoutMs() {
		return linkExtractorPollTimeoutMs;
	}

	public void setLinkExtractorPollTimeoutMs(int linkExtractorPollTimeoutMs) {
		this.linkExtractorPollTimeoutMs = linkExtractorPollTimeoutMs;
	}

	public int getLinkExtractorOfferTimeoutMs() {
		return linkExtractorOfferTimeoutMs;
	}

	public void setLinkExtractorOfferTimeoutMs(int linkExtractorOfferTimeoutMs) {
		this.linkExtractorOfferTimeoutMs = linkExtractorOfferTimeoutMs;
	}

	public List<URL> getStartUrls() {
		return startUrls;
	}

	public void setStartUrls(List<URL> startUrls) {
		this.startUrls = startUrls;
	}
}
