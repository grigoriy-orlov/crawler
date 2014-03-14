package ru.ares4322.crawler;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

@ImplementedBy(PageRequesterServiceImpl.class)
public interface PageRequesterService extends Service {

	void setInputQueue(@NotNull BlockingQueue<URL> inputQueue);

	void setOutputQueue(@NotNull BlockingQueue<String> outputQueue);

	void setControlSemaphore(@NotNull Semaphore semaphore);

}
