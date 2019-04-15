package com.st0rmtroop3r.telegramchart;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Ololo {

    ForkJoinPool forkJoinPool = new ForkJoinPool();

    public void lol() {
        forkJoinPool.invoke(new FooTask());
    }

    class FooTask extends RecursiveTask<Object> {

        @Override
        protected Object compute() {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
