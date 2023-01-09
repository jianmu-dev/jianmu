package dev.jianmu.infrastructure.worker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Daihw
 * @class DeferredResultService
 * @description DeferredResultService
 * @create 2022/5/20 9:57 上午
 */
@Service
public class DeferredResultService {

    private final static Long pullTimeout = 1000L * 60 * 30;
    private final static Long watchTimeout = 1000L * 60;

    private final Map<String, List<DeferredResult<ResponseEntity<?>>>> pullDeferredResults = new ConcurrentHashMap<>();
    private final Map<String, Map<String, DeferredResult<ResponseEntity<?>>>> watchDeferredResults = new ConcurrentHashMap<>();

    /**
     * 创建拉取任务的DeferredResult
     *
     * @param workerId
     * @return
     */
    public DeferredResult<ResponseEntity<?>> newPullDeferredResult(String workerId) {
        var deferredResult = new DeferredResult<ResponseEntity<?>>(pullTimeout, null);
        this.pullDeferredResults.putIfAbsent(workerId, new CopyOnWriteArrayList<>());
        this.pullDeferredResults.get(workerId).add(deferredResult);

        deferredResult.onError(Throwable -> deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("error")));
        deferredResult.onTimeout(() -> deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body("timeout")));
        deferredResult.onCompletion(() -> this.pullDeferredResults.get(workerId).remove(deferredResult));
        return deferredResult;
    }

    public void clearWorker(String workerId) {
        var list = this.pullDeferredResults.get(workerId);
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(deferredResult -> deferredResult.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body("retry")));
        this.pullDeferredResults.remove(workerId);
    }

    /**
     * 是否存在监视任务
     */
    public boolean existWatchDeferredResult(String workerId, String businessId) {
        if (!this.watchDeferredResults.containsKey(workerId)) {
            return false;
        }
        return this.watchDeferredResults.get(workerId).containsKey(businessId);
    }

    /**
     * 创建获取终止任务的DeferredResult
     *
     * @param workerId
     * @param businessId
     * @return
     */
    public DeferredResult<ResponseEntity<?>> newWatchDeferredResult(String workerId, String businessId) {
        var deferredResult = new DeferredResult<ResponseEntity<?>>(watchTimeout, null);
        this.watchDeferredResults.putIfAbsent(workerId, new ConcurrentHashMap<>());
        this.watchDeferredResults.get(workerId).putIfAbsent(businessId, deferredResult);

        deferredResult.onError(Throwable -> deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("error")));
        deferredResult.onTimeout(() -> deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body("timeout")));
        deferredResult.onCompletion(() -> this.watchDeferredResults.get(workerId).remove(businessId));
        return deferredResult;
    }

    public void terminateDeferredResult(String workerId, String businessId) {
        var map = this.watchDeferredResults.get(workerId);
        if (map == null) {
            return;
        }
        var deferredResult = map.get(businessId);
        if (deferredResult != null) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.OK).body(businessId));
            map.remove(businessId);
        }
    }
}
