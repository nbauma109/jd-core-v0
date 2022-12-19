package jd.core.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/*
 * Simplified from org.apache.commons.lang3.concurrent.BackgroundInitializer.InitializationTask<T>
 */
public abstract class InitializationTask<T> implements Callable<T> {
    private final ExecutorService execFinally;

    InitializationTask(final ExecutorService exec) {
        execFinally = exec;
    }

    @Override
    public T call() throws Exception {
        try {
            return initialize();
        } finally {
            if (execFinally != null) {
                execFinally.shutdown();
            }
        }
    }

    abstract T initialize();
}