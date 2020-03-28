package com.example.zealjiang.txtjsoneditor;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.FileIOUtils;
import com.example.zealjiang.util.FileUtil;
import com.example.zealjiang.util.JsonUtil;
import com.example.zealjiang.util.PermissionUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditorActivity extends AppCompatActivity {

    private PermissionUtil permissionUtil;
    private String uriPath;
    private boolean isEdit = false;//是否正在编辑
    private String contentOld;
    private String filePath;

    @BindView(R.id.tvContent)
    EditText tvContent;
    @BindView(R.id.ivMore)
    ImageView ivMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        permissionUtil = new PermissionUtil();
        boolean boo = permissionUtil.checkPermission(this);

        Intent intent = getIntent();
        if(intent == null){
            return;
        }
        String action = intent.getAction();

        //将文件复制到制定目录中
        if(Intent.ACTION_VIEW.equals(action)) {
            String str = intent.getDataString();
            Log.e("uri", str);
            if (str != null) {
                uriPath = str;
                if(boo){
                    readFile(str);
                }
            }
        }
    }

    @OnClick({R.id.ivMore})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ivMore :
                if(!isEdit){
                    tvContent.setFocusable(true);
                    tvContent.setFocusableInTouchMode(true);
                    tvContent.requestFocus();
                    ivMore.setBackgroundResource(R.mipmap.save);
                    isEdit = true;
                }else{
                    tvContent.setFocusable(false);
                    tvContent.setFocusableInTouchMode(false);
                    ivMore.setBackgroundResource(R.mipmap.xiugai);
                    isEdit = false;
                    saveModify();
                }
                break;
        }
    }

    /**
     * 保存修改后的内容到文件
     */
    private void saveModify(){
        if(tvContent != null && !tvContent.getText().toString().equals(contentOld)){
            String content = tvContent.getText().toString();
            File file = new File(filePath);
            FileUtil.writeSDFile(file, content);
        }
    }

    private void readFile(String uriPath){
        if(TextUtils.isEmpty(uriPath))return;

        Uri uri = Uri.parse(uriPath);//uri路径
        filePath = FileUtil.getRealFilePath(this, uri);//获取文件绝对路径

        String encode = null;
        try{
            URL url = new File(filePath).toURI().toURL();
            encode = FileUtil.getUrlEncode(url);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }

        String content = "";
        if(TextUtils.isEmpty(encode)){
            //读取文件内容
            content = FileUtil.convertCodeAndGetText(filePath);
        }else {
            //读取文件内容
            content = FileIOUtils.readFile2String(filePath,encode);
        }

        //如果显示的内容是json,格式化content
        content = JsonUtil.formatJson(content);
        //显示
        tvContent.setText(content);
        contentOld = content;

/*                try {
                String fileName= FileUtil.getFileName(filePath);
                InputStream in = this.getContentResolver().openInputStream(uri);//io
                FileOutputStream out = new FileOutputStream(new File(path));//文件输出到开发app路径
                byte[] b = new byte[1024];
                try {
                    while ((in.read(b)) != -1) {
                        out.write(b);
                    }
                    in.close();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }*/


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(permissionUtil == null)return;
        boolean boo = permissionUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(boo){
            readFile(uriPath);
        }else{
            permissionUtil.checkPermission(this);
        }
    }

}
