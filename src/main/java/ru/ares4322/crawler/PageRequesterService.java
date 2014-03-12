package ru.ares4322.crawler;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

public interface PageRequesterService {
	void start();

	void stop();

	void setInputQueue(@NotNull BlockingQueue<URL> in);

	void setOutputQueue(@NotNull BlockingQueue<String> out);
}
