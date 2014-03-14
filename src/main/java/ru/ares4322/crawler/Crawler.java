package ru.ares4322.crawler;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;

@ImplementedBy(CrawlerImpl.class)
public interface Crawler extends Service {
}
