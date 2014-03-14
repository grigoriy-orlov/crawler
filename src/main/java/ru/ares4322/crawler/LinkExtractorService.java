package ru.ares4322.crawler;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

@ImplementedBy(LinkExtractorServiceImpl.class)
public interface LinkExtractorService extends Service {

	void setInputQueue(@NotNull BlockingQueue<String> inputQueue);

	void setOutputQueue(@NotNull BlockingQueue<URL> outputQueue);

	void setControlSemaphore(@NotNull Semaphore semaphore);
}
