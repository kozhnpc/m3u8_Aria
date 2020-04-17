package work.kozh.m3u8_aria;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.M3U8;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;
import work.kozh.aria_m3u8.AriaDownloadManager;
import work.kozh.aria_m3u8.m3u8.M3U8Downloader;
import work.kozh.aria_m3u8.utils.ConvertUtil;

public class MainActivity extends AppCompatActivity {

    private String mUrl;
    private long mTaskId;

    private TextView mTextView;
    private Button mDownload;
    private Button mPause;
    private Button mCancle;
    private FloatingActionButton mFab;
    private Button mCreate;
    private EditText mEtUrl;
    private EditText mEtName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AriaDownloadManager.register(this);
        applyPermission();
        mTextView = findViewById(R.id.textView);
        mDownload = findViewById(R.id.download);
        mPause = findViewById(R.id.pause);
        mCancle = findViewById(R.id.cancle);
        mFab = findViewById(R.id.fab);
        mCreate = findViewById(R.id.create);
        mEtUrl = findViewById(R.id.url);
        mEtName = findViewById(R.id.name);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadListActivity.class);
                startActivity(intent);
            }
        });

    }


    private void applyPermission() {
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "文件写入", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, "文件读取", R.drawable.permission_ic_storage));
        HiPermission.create(MainActivity.this)
                .permissions(permissionItems)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))//图标的颜色
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });

    }

    public void download(View view) {

        if (AriaDownloadManager.getTaskState(this, mTaskId) == AriaDownloadManager.STATE_RUNNING) {
            Toast.makeText(this, "正在下载中，请下载结束后再试", Toast.LENGTH_LONG).show();
            return;
        }

        Log.i("TAG", "开始下载文件：");
        mUrl = "https://youku.cdn4-okzy.com/20200415/6318_6225b18b/index.m3u8";
//        mUrl = "https://iqiyi.cdn9-okzy.com/20200407/8474_18e1dd5a/index.m3u8";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/m3u8/test.mp4";
        mTaskId = M3U8Downloader.createTask(this, mUrl, path);

    }

    public void pause(View view) {
        if (AriaDownloadManager.getTaskState(this, mTaskId) == AriaDownloadManager.STATE_RUNNING) {
            M3U8Downloader.pauseTask(this, mTaskId);
            mPause.setText("继续下载");
        } else if (AriaDownloadManager.getTaskState(this, mTaskId) == AriaDownloadManager.STATE_PAUSE) {
            M3U8Downloader.resumeTask(this, mTaskId, mUrl);
            mPause.setText("暂停下载");
        }
//        mTextView.setText("下载暂停");
    }

    public void cancel(View view) {
        M3U8Downloader.cancelTask(this, mTaskId);
//        mTextView.setText("下载取消");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AriaDownloadManager.unRegister(this);
    }

    //***************************  下载过程监听方法  *****************************//

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
        if (task.getKey().equals(mUrl)) {
            mTextView.setText("下载等待中...");
            Log.i("TAG", "wait ==> " + task.getDownloadEntity().getFileName());
        }
    }

    @Download.onPre
    protected void onPre(DownloadTask task) {
        //准备下载...
        mTextView.setText("下载初始化中...");
        Log.i("TAG", "onPre ==> " + task.getDownloadEntity().getFileName());
    }

    @Download.onTaskStart
    void taskStart(DownloadTask task) {
        //开始下载了...
        //可以在这里获取切片的总数量
        int peerNum = task.getDownloadEntity().getM3U8Entity().getPeerNum();
        if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "isComplete = " + task.isComplete() + ", state = " + task.getState());
            Log.i("TAG", "切片总数量 = " + peerNum);
            mTextView.setText("下载开始...");
        }
    }

    @Download.onTaskRunning
    protected void running(DownloadTask task) {
        //下载任务进行中  获取下载信息
        //下载进度  下载速度
        if (task.getKey().equals(mUrl)) {
            //自行转换的
            String speed = ConvertUtil.formatFileSize(task.getSpeed() < 0 ? 0 : task.getSpeed()) + "/s";
            String downloadFileSize = ConvertUtil.formatFileSize(task.getCurrentProgress() < 0 ? 0 : task.getCurrentProgress());
//            mTextView.setText("下载进行中，进度：" + task.getPercent() + "%，速度：" + task.getConvertSpeed() + "，已下载：" + task.getConvertCurrentProgress());
            mTextView.setText("下载进行中，进度：" + task.getPercent() + "%，速度：" + speed + "，已下载：" + downloadFileSize);
            Log.i("TAG", "m3u8 void running, p = " + task.getPercent() + ", speed  = " + task.getConvertSpeed());
        }
    }

    @Download.onTaskResume
    void taskResume(DownloadTask task) {
        if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "m3u8 vod resume");
            mTextView.setText("下载继续...");
        }
    }

    @Download.onTaskStop
    void taskStop(DownloadTask task) {
        if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "stop");
            mTextView.setText("下载暂停...");
        }
    }

    @Download.onTaskCancel
    void taskCancel(DownloadTask task) {
        if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "cancel");
            mTextView.setText("下载取消...");
        }
    }

    @Download.onTaskFail
    void taskFail(DownloadTask task, Exception e) {
        if (task != null && task.getKey().equals(mUrl)) {
            Log.i("TAG", "fail");
            mTextView.setText("下载失败...");
        }
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
        if (task.getKey().equals(mUrl)) {
            Log.i("TAG", "md5: " + CommonUtil.getFileMD5(new File(task.getFilePath())));
            mTextView.setText("下载完成...");
        }
    }


    public void create(View view) {
        String url = mEtUrl.getText().toString();
        String name = mEtName.getText().toString();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/m3u8/";
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "请先输入地址", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            name = formatter.format(date);
        }

        long id = M3U8Downloader.createTask(this, url, rootPath + name + ".mp4");
        Toast.makeText(this, "创建一个下载任务，ID = " + id, Toast.LENGTH_LONG).show();

    }

}
