package work.kozh.aria_m3u8.m3u8;

import android.content.Context;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;

import work.kozh.aria_m3u8.AriaDownloadManager;

public class M3U8Downloader {

    //同时下载ts文件的数量
    private static int sMaxTsQueueNum = 40;

    /**
     * 开启一个下载任务
     *
     * @param context
     * @param url
     * @param path
     * @return 任务ID
     */
    public static long createTask(Context context, String url, String path) {

        M3U8VodOption option = new M3U8VodOption(); // 创建m3u8点播文件配置

        option.setVodTsUrlConvert(new VodTsUrlConverter()); //ts文件的地址获取方式

        option.setKeyUrlConverter(new KeyUrlConverter());//处理加密的情况

        option.setBandWidthUrlConverter(new BandWidthUrlConverter(url)); //解析重定向的m3u8地址 有可能有多个分辨率的视频可以选择

        option.setMaxTsQueueNum(sMaxTsQueueNum);

        long taskId = Aria.download(context)
                .load(url) // 设置点播文件下载地址
                .setFilePath(path, true) // 设置点播文件保存路径
                .m3u8VodOption(option)   // 调整下载模式为m3u8点播
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

        M3U8VodOption option = new M3U8VodOption(); // 创建m3u8点播文件配置

        option.setVodTsUrlConvert(new VodTsUrlConverter()); //ts文件的地址获取方式

        option.setKeyUrlConverter(new KeyUrlConverter());//处理加密的情况

        option.setBandWidthUrlConverter(new BandWidthUrlConverter(url)); //解析重定向的m3u8地址 有可能有多个分辨率的视频可以选择


        option.setMaxTsQueueNum(sMaxTsQueueNum);

        Aria.download(context)
                .load(taskId)
                .m3u8VodOption(option)
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


}
