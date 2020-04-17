package work.kozh.aria_m3u8.simple;

import android.content.Context;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;

import java.util.List;
import java.util.Map;

import work.kozh.aria_m3u8.AriaDownloadManager;

/**
 * 下载普通任务
 */
public class SimpleDownloader {


    /**
     * 开启一个下载任务
     *
     * @param context
     * @param url
     * @param path
     * @return 任务ID
     */
    public static long createTask(Context context, String url, String path) {

        HttpOption option = new HttpOption();
        option.useServerFileName(true)
                .setFileLenAdapter(new FileLenAdapter());

        long taskId = Aria.download(context)
                .load(url) // 设置文件下载地址
                .setFilePath(path, true) // 设置点播文件保存路径
                .create();

        return taskId;
    }

    /**
     * 继续下载任务
     *
     * @param context
     * @param taskId
     */
    public static void resumeTask(Context context, long taskId, String url) {
        Aria.download(context)
                .load(taskId)
                .resume();
    }

    /**
     * 取消下载任务
     * 默认会删除已经下载的文件
     *
     * @param context
     * @param taskId
     */
    public static void cancelTask(Context context, long taskId) {
        AriaDownloadManager.cancelTask(context, taskId, true);
    }

    /**
     * 停止任务
     * 暂停任务
     *
     * @param context
     * @param taskId
     */
    public static void pauseTask(Context context, long taskId) {
        AriaDownloadManager.stopTask(context, taskId);
    }


    static class FileLenAdapter implements IHttpFileLenAdapter {
        @Override
        public long handleFileLen(Map<String, List<String>> headers) {

            List<String> sLength = headers.get("Content-Length");
            if (sLength == null || sLength.isEmpty()) {
                return -1;
            }
            String temp = sLength.get(0);

            return Long.parseLong(temp);
        }
    }

}
