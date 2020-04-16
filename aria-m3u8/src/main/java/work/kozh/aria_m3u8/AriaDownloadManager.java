package work.kozh.aria_m3u8;

import android.content.Context;
import android.util.Log;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;

import java.util.List;

/**
 * 全局的Aria管理器
 */
public class AriaDownloadManager {

//    -1 Entity.STATE_OTHER  未知
//    0 IEntity.STATE_FAIL  失败
//     1 IEntity.STATE_COMPLETE  成功
//      2 IEntity.STATE_PAUSE 停止
//      3 IEntity.STATE_WAIT   等待
//      4 IEntity.STATE_RUNNING  执行中
//      5 IEntity.STATE_PRE 预处理
//      6 IEntity.STATE_POST_PRE 预处理完成
//      7 IEntity.STATE_CANCEL 删除任务

    public static final int STATE_OTHER = -1;
    public static final int STATE_FAIL = 0;
    public static final int STATE_COMPLETE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_WAIT = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_PRE = 5;
    public static final int STATE_POST_PRE = 6;
    public static final int STATE_CANCEL = 7;

    /**
     * 与下面的用法可以配合着使用
     * <p>
     * 选一个即可
     *
     * @param context
     */
    public static void init(Context context) {
        Aria.init(context);
    }

    /**
     * 注册下载
     *
     * @param context
     */
    public static void register(Context context) {
        Aria.download(context).register();
    }

    /**
     * 对于非Context类型的注册
     * 如 adapter中 ，需要配合init（）一起使用以初始化
     *
     * @param object
     */
    public static void register(Object object) {
        Aria.download(object).register();
        Log.i("TAG", "注册Aria：" + object.getClass());
    }

    /**
     * 注销下载
     *
     * @param context
     */
    public static void unRegister(Context context) {
        Aria.download(context).unRegister();
    }

    /**
     * 对于非Context类型的注销
     * 如 adapter中
     *
     * @param object
     */
    public static void unRegister(Object object) {
        Aria.download(object).unRegister();
        Log.i("TAG", "注销Aria：" + object.getClass());
    }

    /**
     * 创建并启动一个下载任务，创建任务完成后，可获取到该任务的id。
     *
     * @param context
     * @param url
     * @param path
     * @return
     */
    public static long createTask(Context context, String url, String path) {
        long taskId = Aria.download(context)
                .load(url)     //读取下载地址
                .setFilePath(path) //设置文件保存的完整路径
                .create();   //启动下载
        return taskId;
    }

    /**
     * 使用通过创建任务获取的任务id，可对任务进行停止、恢复等操作
     *
     * @param context
     * @param taskId
     */
    public static void stopTask(Context context, long taskId) {
        Aria.download(context)
                .load(taskId)
                .stop();
    }

    /**
     * 使用通过创建任务获取的任务id，可对任务进行停止、恢复等操作
     *
     * @param context
     * @param taskId
     */
    public static void resumeTask(Context context, long taskId) {
        Aria.download(context)
                .load(taskId)
                .resume();
    }

    /**
     * 获取所有普通下载任务，包括已完成和为完成的普通任务
     *
     * @param context
     * @return
     */
    public static List<DownloadEntity> getTaskList(Context context) {
        return Aria.download(context).getTaskList();
    }

    /**
     * 获取所有未完成的普通下载任务
     *
     * @param context
     * @return
     */
    public static List<DownloadEntity> getAllNotCompleteTask(Context context) {
        return Aria.download(context).getAllNotCompleteTask();
    }

    /**
     * 获取所有已经完成的普通任务
     *
     * @param context
     * @return
     */
    public static List<DownloadEntity> getAllCompleteTask(Context context) {
        return Aria.download(context).getAllCompleteTask();
    }


    /**
     * 获取执行中的任务
     *
     * @param context
     * @return
     */
    public static List<DownloadEntity> getRunningTask(Context context) {
        return Aria.download(context).getDRunningTask();
    }


    /**
     * 停止所有任务的命令，并清空所有等待队列
     *
     * @param context
     */
    public static void stopAllTask(Context context) {
        Aria.download(context).stopAllTask();
    }

    /**
     * 恢复所有停止的任务
     * <p>
     * 如果执行队列没有满，则开始下载任务，直到执行队列满
     * 如果队列执行队列已经满了，则将所有任务添加到等待队列中
     *
     * @param context
     */
    public static void resumeAllTask(Context context) {
        Aria.download(context).resumeAllTask();
    }


    /**
     * 删除所有任务
     *
     * @param context
     * @param removeFile 是否删除下载的本地文件
     */
    public static void removeAllTask(Context context, boolean removeFile) {
        Aria.download(context).removeAllTask(removeFile);
    }


    /**
     * 删除单个任务
     *
     * @param context
     * @param taskId
     * @param deleteFile true 不仅删除任务数据库记录，还会删除已经删除完成的文件
     *                   false 如果任务已经完成，只删除任务数据库记录，
     */
    public static void cancelTask(Context context, long taskId, boolean deleteFile) {
        Aria.download(context).load(taskId).cancel(deleteFile);
    }

    /**
     * 删除下载记录
     *
     * @param context
     * @param taskId
     */
    public static void removeRecord(Context context, long taskId) {
        Aria.download(context).load(taskId).removeRecord();
    }


    /**
     * 获取当前任务的状态
     * 结果可以通过常量来辨识
     *
     * @param context
     * @param taskId
     * @return -1 Entity.STATE_OTHER  未知
     * 0 IEntity.STATE_FAIL  失败
     * 1 IEntity.STATE_COMPLETE  成功
     * 2 IEntity.STATE_PAUSE 停止
     * 3 IEntity.STATE_WAIT   等待
     * 4 IEntity.STATE_RUNNING  执行中
     * 5 IEntity.STATE_PRE 预处理
     * 6 IEntity.STATE_POST_PRE 预处理完成
     * 7 IEntity.STATE_CANCEL 删除任务
     */
    public static int getTaskState(Context context, long taskId) {
        int taskState = Aria.download(context)
                .load(taskId)
                .getTaskState();
        return taskState;
    }

}
