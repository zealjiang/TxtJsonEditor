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
import android.widget.Toast;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.zealjiang.MyApplication;
import com.example.zealjiang.helper.UcToMp3Helper;
import com.example.zealjiang.util.FileUtil;
import com.example.zealjiang.util.JsonUtil;
import com.example.zealjiang.util.PermissionUtil;
import com.example.zealjiang.util.log.XLog;

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
    @BindView(R.id.pageStateLayout)
    com.example.zealjiang.view.PageStateLayout pageStateLayout;

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

        if(Intent.ACTION_VIEW.equals(action)) {
            String str = intent.getDataString();
            Log.e("uri", str);
            if (str != null) {
                uriPath = str;
                if(boo){
                    //如果是网易缓存音乐
                    String realPath = uriPathToRealPath(uriPath);
                    if(realPath.endsWith(".mp3.uc!")){
                        UcToMp3Helper ucToMp3Helper = new UcToMp3Helper(EditorActivity.this);
                        ucToMp3Helper.readFile(pageStateLayout,realPath);
                    }else{
                        readFile(realPath);
                    }

                }
            }
        }
    }

    private String uriPathToRealPath(String uriPath){
        if(TextUtils.isEmpty(uriPath))return "";

        Uri uri = Uri.parse(uriPath);//uri路径
        String filePath = FileUtil.getPath(this, uri);//获取文件绝对路径
        XLog.debug("mtest","readFile  filePath: "+filePath);
        if(filePath == null){
            //ToastUtils.showShort("找不到文件");
            Toast.makeText(MyApplication.getContext(),"找不到文件",Toast.LENGTH_SHORT).show();
            return "";
        }
        return filePath;
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

    private void readFile(String filePath){

        try{
/*            if(TextUtils.isEmpty(uriPath))return;

            Uri uri = Uri.parse(uriPath);//uri路径
            filePath = FileUtil.getPath(this, uri);//获取文件绝对路径
            XLog.debug("mtest","readFile  filePath: "+filePath);
            if(filePath == null){
                //ToastUtils.showShort("找不到文件");
                Toast.makeText(MyApplication.getContext(),"找不到文件",Toast.LENGTH_SHORT).show();
                return;
            }*/

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
        }catch (Exception e){
            e.printStackTrace();
        }
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
