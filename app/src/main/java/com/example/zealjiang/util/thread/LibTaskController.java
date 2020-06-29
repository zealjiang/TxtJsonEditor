package com.example.zealjiang.util.thread;

import android.os.Handler;
import android.os.Looper;

public class LibTaskController {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final PriorityExecutor sDefaultExecutor = new PriorityExecutor();


    public static void autoPost(Runnable runnable) {
        if (runnable == null) return;
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    /**
     * run in UI thread
     *
     * @param runnable
     */
    public static void post(Runnable runnable) {
        if (runnable == null) return;
        handler.post(runnable);
    }

    /**
     * run in UI thread
     *
     * @param runnable
     * @param delayMillis
     */
    public static void postDelayed(Runnable runnable, long delayMillis) {
        if (runnable == null) return;
        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * run in background thread
     *
     * @param runnable
     */
    public static void run(Runnable runnable) {
        if (!sDefaultExecutor.isBusy()) {
            sDefaultExecutor.execute(runnable);
        } else {
            Thread t= new Thread(runnable);
            t.setName("TaskController_"+ System.currentTimeMillis());
            t.start();
        }
    }

    /**
     * 移除post或postDelayed提交的, 未执行的runnable
     *
     * @param runnable
     */
    public static void removeCallbacks(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

}
