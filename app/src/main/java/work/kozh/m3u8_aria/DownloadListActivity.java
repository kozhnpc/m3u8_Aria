package work.kozh.m3u8_aria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.arialyy.aria.core.download.DownloadEntity;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import work.kozh.aria_m3u8.AriaDownloadManager;
import work.kozh.aria_m3u8.m3u8.M3U8Downloader;

public class DownloadListActivity extends AppCompatActivity {

    private RecyclerView mRv;
    private Button mBtnPauseAll;
    private Button mBtnResumeAll;
    private Button mBtnCancelAll;
    private DownloadListAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downlaod_list);

        AriaDownloadManager.init(this);

        findView();

    }

    private void findView() {

        mRv = findViewById(R.id.rv);
        mBtnPauseAll = findViewById(R.id.btn_pause_all);
        mBtnResumeAll = findViewById(R.id.btn_resume_all);
        mBtnCancelAll = findViewById(R.id.btn_cancel_all);

        final List<DownloadEntity> list = AriaDownloadManager.getTaskList(this);
        mAdapter = new DownloadListAdapter(list);
        AriaDownloadManager.register(mAdapter);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                long taskId = list.get(position).getId();
                switch (view.getId()) {
                    case R.id.pause:
                        M3U8Downloader.pauseTask(DownloadListActivity.this, taskId);
                        break;
                    case R.id.resume:
                        M3U8Downloader.resumeTask(DownloadListActivity.this, taskId, list.get(position).getKey());
                        break;
                    case R.id.cancel:
                        M3U8Downloader.cancelTask(DownloadListActivity.this, taskId);
                        break;
                }

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRv.setLayoutManager(linearLayoutManager);
        mRv.setAdapter(mAdapter);


    }

    public void pause(View view) {
        AriaDownloadManager.stopAllTask(this);
    }


    public void resume(View view) {
        AriaDownloadManager.resumeAllTask(this);
    }

    public void cancel(View view) {
        AriaDownloadManager.removeAllTask(this, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            AriaDownloadManager.unRegister(mAdapter);
        }

    }
}
