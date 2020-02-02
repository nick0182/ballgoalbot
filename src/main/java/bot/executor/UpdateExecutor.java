package bot.executor;

import java.util.concurrent.ExecutorService;

public abstract class UpdateExecutor {

    protected ExecutorService cachedExecutorService;

    public UpdateExecutor(ExecutorService cachedExecutorService) {
        this.cachedExecutorService = cachedExecutorService;
    }

    public ExecutorService getCachedExecutorService() {
        return cachedExecutorService;
    }

    protected abstract void shutdownExecutor();
}
