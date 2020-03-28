package com.example.zealjiang.txtjsoneditor;

import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.zealjiang.adapter.FileSearchAdapter;
import com.example.zealjiang.bean.FileBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileSearchActivity extends AppCompatActivity {


    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(R.id.tv)
    TextView tv;

    private FileSearchAdapter fileSearchAdapter;
    private List<FileBean> listData = new ArrayList<>();

    private File defaultDir;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_search);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileSearchAdapter = new FileSearchAdapter(this,listData);
        recyclerView.setAdapter(fileSearchAdapter);

        init();
    }

    private void init(){
        defaultDir = Environment.getExternalStorageDirectory();
        showChangge(defaultDir);
    }

    //显示改变data之后的文件数据列表
    private void showChangge(File file) {
        //showtv.setText(path);
        File[] files = file.listFiles();
        if(files == null || files.length == 0){
            return;
        }
        listData.clear();
        for (File f : files) {
            FileBean fileBean = new FileBean();
            fileBean.setFileName(f.getName());
            if(f.isDirectory()){
                fileBean.setFileType(FileBean.FileType.DIRECTORY);
            }else if(f.isFile()){
                fileBean.setFileType(FileBean.FileType.FILE);
            }
            listData.add(fileBean);
        }
        fileSearchAdapter.notifyDataSetChanged();
    }
}
