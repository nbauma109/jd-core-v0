package jd.core.test.fallbacks;

import java.io.IOException;

public class ReturnInFinally {
    volatile boolean closed;
    final Object needToPoll = new Object();

    int threadExit(Runnable r, boolean replaceMe) {
        return 0;
    }

    boolean isShutdown() {
        return closed;
    }

    void implClose() {
    }

    Object take() throws InterruptedException {
        return null;
    }

    Runnable pollTask() {
        return null;
    }

    class Task implements Runnable {
        Object poll() throws IOException {
            return null;
        }

        public void run() {
            Object group = Thread.currentThread().getThreadGroup();
            boolean isPooledThread = group != null;
            boolean replaceMe = false;
            Object ev;
            try {
                for (;;) {
                    if (isPooledThread)
                        System.gc();
                    try {
                        replaceMe = false;
                        ev = take();
                        if (ev == needToPoll) {
                            try {
                                ev = poll();
                            } catch (IOException x) {
                                x.printStackTrace();
                                return;
                            }
                        }
                    } catch (InterruptedException x) {
                        continue;
                    }
                    if (ev == group) {
                        Runnable task = pollTask();
                        if (task == null) {
                            return;
                        }
                        replaceMe = true;
                        task.run();
                        continue;
                    }
                    try {
                        ev.notify();
                    } catch (Error x) {
                        replaceMe = true;
                        throw x;
                    } catch (RuntimeException x) {
                        replaceMe = true;
                        throw x;
                    }
                }
            } finally {
                int remaining = threadExit(this, replaceMe);
                if (remaining == 0 && isShutdown()) {
                    implClose();
                }
            }
        }
    }
}
