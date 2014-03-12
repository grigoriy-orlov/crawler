package ru.ares4322.crawler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

public interface PageRequester {

	@NotNull
	String getPage(@NotNull URL url) throws IOException;
}
