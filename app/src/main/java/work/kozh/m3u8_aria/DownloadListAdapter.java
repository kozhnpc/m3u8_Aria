package work.kozh.m3u8_aria;

import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.M3U8;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import work.kozh.aria_m3u8.AriaDownloadManager;
import work.kozh.aria_m3u8.utils.ConvertUtil;

/**
 * 处理了RecyclerView 动态变化下载进度时的显示情况
 */
public class DownloadListAdapter extends BaseQuickAdapter<DownloadEntity, BaseViewHolder> {


    // 缓存每一个RecyclerView条目，以便实时更新其状态，一一对应关系是根据每个视频的url地址来区分
    private Map<String, DownloadInfo> mConvertViews = new LinkedHashMap<String, DownloadInfo>();

    static class DownloadInfo {
        //缓存每个item下载时的一些数据，以免recyclerView显示混乱
        public double progress; //下载进度
        public BaseViewHolder holder; //显示的item
        public DownloadEntity mEntity;
        public double downloadSize; //已下载文件大小
        public long totalSize; //文件大小

        public boolean isDownloading = false; //是否正在下载
        public boolean isDownloaded = false; //是否已经下载完成
        public boolean isDownloadError = false; //是否下载失败
    }


    public DownloadListAdapter(@Nullable List<DownloadEntity> data) {
        super(R.layout.item_download_list, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, DownloadEntity item) {

        holder.setText(R.id.tv_song_position, holder.getAdapterPosition() + 1 + "")
                .setText(R.id.tvFileName, item.getFileName())
                .setText(R.id.tvDownloadSize, ConvertUtil.formatFileSize(item.getCurrentProgress() < 0 ? 0 : item.getCurrentProgress()))
                .setText(R.id.tvPercent, item.getPercent() + "%")
                .setText(R.id.tvSpeed, item.getConvertSpeed())
                .addOnClickListener(R.id.pause, R.id.resume, R.id.cancel);

        //处理变化的部分
        handleChange(holder, item.getKey(), item);

    }


    /**
     * 处理下载问题 以每一个地址作为标识
     */
    private void handleChange(BaseViewHolder holder, final String url, DownloadEntity entity) {
        //去除无效数据
        if (TextUtils.isEmpty(url)) {
            mConvertViews.remove(url);
        }

        DownloadInfo downloadInfo = mConvertViews.get(url);

        //设置缓存信息
        if (downloadInfo != null) {
            Log.i("TAG", "下载中数据，缓存过");
            //该条目已经缓存过  使用缓存的数据来设置属性值
            entity = downloadInfo.mEntity;

            //设置进度条
            ((ProgressBar) downloadInfo.holder.getView(R.id.pbProgress)).setProgress((int) downloadInfo.progress);
            ((ProgressBar) downloadInfo.holder.getView(R.id.pbProgress)).setMax(100);
            downloadInfo.holder.setText(R.id.tvPercent, downloadInfo.progress + "%");

            //设置已下载文件大小文本
            String downloadSize = ConvertUtil.formatFileSize(downloadInfo.downloadSize < 0 ? 0 : downloadInfo.downloadSize);
            downloadInfo.holder.setText(R.id.tvDownloadSize, downloadSize + " / ");
//            downloadInfo.holder.setText(R.id.tvTotalSize, ((float) (Math.round(downloadInfo.totalSize * 100)) / 100) + "M");

            //初始化时 动态地设置下载的提示文字 tvText 显示下载速度 或者下载状态
            showDownloadingInfo(downloadInfo.holder, downloadInfo.mEntity);

        } else {
            Log.i("TAG", "下载中数据，没有缓存过，加入缓存");
            //还没有缓存过 实时获取属性，随后加入到缓存中
            //设置下载进度
            int totalSize = (int) entity.getFileSize();

            //进度条
            ((ProgressBar) holder.getView(R.id.pbProgress)).setProgress(entity.getPercent());
            ((ProgressBar) holder.getView(R.id.pbProgress)).setMax(100);

            // 下载文件大小
            String downlaodSize = ConvertUtil.formatFileSize(entity.getCurrentProgress() < 0 ? 0 : entity.getCurrentProgress());
            holder.setText(R.id.tvDownloadSize, downlaodSize + " / ");
            holder.setText(R.id.tvTotalSize, "0000M");
            // 下载百分比
            holder.setText(R.id.tvPercent, entity.getPercent() + "%");

            //初始化时 动态地设置下载的提示文字 tvText 显示下载速度 或者下载状态
            showDownloadingInfo(holder, entity);

            //加入缓存中
            DownloadInfo info = new DownloadInfo();
            info.holder = holder;
            info.mEntity = entity;
            info.downloadSize = entity.getCurrentProgress();
            info.totalSize = totalSize;
            info.progress = entity.getPercent();

            mConvertViews.put(url, info);
        }


    }


    //初始化时 动态地显示当前的下载状态 或者下载速度信息
    private void showDownloadingInfo(BaseViewHolder holder, DownloadEntity entity) {

        int taskState = AriaDownloadManager.getTaskState(mContext, entity.getId());

        Log.i("TAG", "taskState = " + taskState);

        //设置提示信息文本内容
        switch (taskState) {
            // 下载文件状态：未知
            case AriaDownloadManager.STATE_OTHER:
                holder.setText(R.id.tvSpeed, "下载状态未知!");
                break;
            // 下载文件状态：预处理
            case AriaDownloadManager.STATE_PRE:
                holder.setText(R.id.tvSpeed, "准备中...");
                break;
            //  下载文件状态：预处理完成
            case AriaDownloadManager.STATE_POST_PRE:
                holder.setText(R.id.tvSpeed, "准备下载...");
                break;
            // 下载文件状态：等待
            case AriaDownloadManager.STATE_WAIT:
                holder.setText(R.id.tvSpeed, "等待下载...");
                break;
            // 下载文件状态：暂停  暂停应有一个点击事件 响应继续下载
            case AriaDownloadManager.STATE_PAUSE:
                holder.setText(R.id.tvSpeed, "下载暂停...");
                break;
            // 下载文件状态：下载中
            case AriaDownloadManager.STATE_RUNNING:
                holder.setText(R.id.tvSpeed, entity.getConvertSpeed());
                break;
            // 下载文件状态：成功
            case AriaDownloadManager.STATE_COMPLETE:
                holder.setText(R.id.tvSpeed, "下载完成...");
                break;
            // 下载文件状态：取消
            case AriaDownloadManager.STATE_CANCEL:
                holder.setText(R.id.tvSpeed, "下载取消...");
                break;
        }

    }


    //******************************* 监听下载过程 *********************************************//


    @M3U8.onPeerStart
    void onPeerStart(String m3u8Url, String peerPath, int peerIndex) {
        //m3u8切片开始准备下载
        Log.i("TAG", "peer create, path: " + peerPath + ", index: " + peerIndex);
    }

    @M3U8.onPeerComplete
    void onPeerComplete(String m3u8Url, String peerPath, int peerIndex) {
        //m3u8切片下载完成
        Log.i("TAG", "peer complete, path: " + peerPath + ", index: " + peerIndex);
        //mVideoFragment.addPlayer(peerIndex, peerPath);
    }

    @M3U8.onPeerFail
    void onPeerFail(String m3u8Url, String peerPath, int peerIndex) {
        Log.i("TAG", "peer fail, path: " + peerPath + ", index: " + peerIndex);
    }

    @Download.onWait
    void onWait(DownloadTask task) {
        //下载任务在等待中
        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载任务等待中...");
        } else {
            notifyDataSetChanged();
        }
        Log.i("TAG", "wait ==> " + task.getDownloadEntity().getFileName());
        /*if (url.equals(mUrl)) {
            mTextView.setText("下载等待中...");
        }*/
    }

    @Download.onPre
    protected void onPre(DownloadTask task) {
        //准备下载...
        Log.i("TAG", "onPre ==> " + task.getDownloadEntity().getFileName());
        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载任务准备中...");
        } else {
            notifyDataSetChanged();
        }

    }

    @Download.onTaskStart
    void taskStart(DownloadTask task) {
        //开始下载了...
        //可以在这里获取切片的总数量
        int peerNum = task.getDownloadEntity().getM3U8Entity().getPeerNum();
//        if (task.getKey().equals(mUrl)) {
//            Log.i("TAG", "isComplete = " + task.isComplete() + ", state = " + task.getState());
//            Log.i("TAG", "切片总数量 = " + peerNum);
//        }

        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载任务开始...");
        } else {
            notifyDataSetChanged();
        }

    }

    @Download.onTaskRunning
    protected void running(DownloadTask task) {
        //下载任务进行中  获取下载信息
        //下载进度  下载速度
        //实时更新item信息显示 并缓存下来，下次再滑动到这个item时，就会从缓存中读取数据 设置

        DownloadInfo info = mConvertViews.get(task.getKey());
        if (info != null) {

            //设置进度条
            // 下载进度百分比
            ((ProgressBar) info.holder.getView(R.id.pbProgress)).setProgress(task.getPercent());
            ((ProgressBar) info.holder.getView(R.id.pbProgress)).setMax(100);
            info.holder.setText(R.id.tvPercent, task.getPercent() + "%");

            //设置已下载文件大小文本
            String downloadSize = ConvertUtil.formatFileSize(task.getCurrentProgress() < 0 ? 0 : task.getCurrentProgress());
            info.holder.setText(R.id.tvDownloadSize, downloadSize + "/ ");

            // 下载速度
            info.holder.setText(R.id.tvSpeed, task.getConvertSpeed());

            //后续还需要实时缓存记录下载的进度等信息，用于给列表保存显示，这样下次再显示时就是最新的下载数据
            info.progress = task.getPercent();
//            info.totalSize = (int) fileSize;
            info.downloadSize = (int) task.getCurrentProgress();
            info.mEntity = task.getEntity();//这个对象可以不缓存 减小内存消耗 其实后续从中获取的信息就是任务ID 不会动态变化，无需缓存

            mConvertViews.put(task.getKey(), info);

        } else {
            Log.i("TAG", "Adapter else部分调用下载进度");
            notifyDataSetChanged();
        }

      /*  if (task.getKey().equals(mUrl)) {
            //自行转换的
            String speed = ConvertUtil.formatFileSize(task.getSpeed() < 0 ? 0 : task.getSpeed()) + "/s";
            String downloadFileSize = ConvertUtil.formatFileSize(task.getCurrentProgress() < 0 ? 0 : task.getCurrentProgress());
//            mTextView.setText("下载进行中，进度：" + task.getPercent() + "%，速度：" + task.getConvertSpeed() + "，已下载：" + task.getConvertCurrentProgress());
            mTextView.setText("下载进行中，进度：" + task.getPercent() + "%，速度：" + speed + "，已下载：" + downloadFileSize);
            Log.i("TAG", "m3u8 void running, p = " + task.getPercent() + ", speed  = " + task.getConvertSpeed());
        }*/


    }

    @Download.onTaskResume
    void taskResume(DownloadTask task) {
        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载继续...");
        } else {
            notifyDataSetChanged();
        }

        /*if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "m3u8 vod resume");
            mTextView.setText("下载继续...");
        }*/
    }

    @Download.onTaskStop
    void taskStop(DownloadTask task) {

        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载暂停...");
        } else {
            notifyDataSetChanged();
        }

        /*if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "stop");
            mTextView.setText("下载暂停...");
        }*/
    }

    @Download.onTaskCancel
    void taskCancel(DownloadTask task) {
        /*String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载任务取消...");
        } else {
            notifyDataSetChanged();
        }*/

        /*if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "cancel");
            mTextView.setText("下载取消...");
        }*/
    }

    @Download.onTaskFail
    void taskFail(DownloadTask task, Exception e) {

        try {
            String url = task.getKey();
            DownloadInfo info = mConvertViews.get(url);
            if (info != null) {
                info.holder.setText(R.id.tvSpeed, "下载任务失败...");
            } else {
                notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, "下载操作出错", Toast.LENGTH_SHORT).show();
        }

        /*if (task != null && task.getKey().equals(mUrl)) {
            Log.i("TAG", "fail");
            mTextView.setText("下载失败...");
        }*/
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {

        Toast.makeText(mContext, "文件下载完成，路径：" + task.getFilePath(), Toast.LENGTH_LONG).show();
        String url = task.getKey();
        DownloadInfo info = mConvertViews.get(url);
        if (info != null) {
            info.holder.setText(R.id.tvSpeed, "下载完成...");
            Log.i("TAG", "文件下载完成，路径：" + task.getFilePath());
        } else {
            notifyDataSetChanged();
        }

       /* if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "md5: " + CommonUtil.getFileMD5(new File(task.getFilePath())));
            mTextView.setText("下载完成...");
        }*/
    }


}
