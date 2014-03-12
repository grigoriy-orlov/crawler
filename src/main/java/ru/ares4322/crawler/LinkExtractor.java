package ru.ares4322.crawler;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public interface LinkExtractor {

	@NotNull
	List<String> getLinks(@NotNull URL url);
}
