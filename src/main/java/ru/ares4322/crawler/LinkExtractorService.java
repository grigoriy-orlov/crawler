package ru.ares4322.crawler;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

public interface LinkExtractorService {
	void start();

	void stop();

	void setInputQueue(@NotNull BlockingQueue<String> in);

	void setOutputQueue(@NotNull BlockingQueue<URL> out);
}
