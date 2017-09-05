import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {
    public static void main(String[] args) throws InterruptedException {
        final AtomicInteger INDEX = new AtomicInteger(0);
        ExecutorService service = Executors.newFixedThreadPool(10, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        for (int i = 0; i < 15; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.currentThread().setName("fay-" + INDEX.addAndGet(1));
                        Thread.sleep(10000);
                        System.out.println(String.format("我%s幸存了", Thread.currentThread().getName()));
                    } catch (InterruptedException e) {
                        System.out.println(String.format("我%s被杀了，帮我报仇", Thread.currentThread().getName()));
                    }
                }
            });
        }
        service.shutdown();
        while (!service.isTerminated()) {
            printThread();
            Thread.sleep(1000);
        }
        printThread();
    }

    private static void printThread() {
        Thread[] threads = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
        Thread.currentThread().getThreadGroup().enumerate(threads);
        for (Thread thread : threads) {
            if (thread.getName().contains("fay-")) {
                thread.interrupt();
                break;
            }
        }
        System.out.println(threads.length + ":" + Arrays.toString(threads));
    }

    @Test
    public void test() {
        Thread[] threads = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
        Thread.currentThread().getThreadGroup().enumerate(threads);
        System.out.println(Arrays.toString(threads));
        System.out.println(Thread.currentThread().getThreadGroup().getParent());
        threads = new Thread[Thread.currentThread().getThreadGroup().getParent().activeCount()];
        Thread.currentThread().getThreadGroup().getParent().enumerate(threads);
        System.out.println(Arrays.toString(threads));
    }
}
