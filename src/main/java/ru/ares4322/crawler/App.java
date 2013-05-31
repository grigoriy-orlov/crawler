package ru.ares4322.crawler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    public static void main( String[] args ){
        //@todo Сделать разные парсеры командной строки и валидаторы ввода

        String startPath = args[0];

        BlockingQueue<String> pageQueue = new ArrayBlockingQueue<>(100, true);
        BlockingQueue<String> urlQueue = new ArrayBlockingQueue<>(100, true);

        HttpClient httpClient = new HttpClient(startPath, pageQueue);

        new Thread(new HttpClient(startPath, pageQueue)).start();  //одного потока хватит, так как NIO

        //ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        //fixedThreadPool.
    }
}
