package com.example.zealjiang.txtjsoneditor;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zealjiang.HandleDataListView;
import com.example.zealjiang.MyApplication;
import com.example.zealjiang.bean.FileEntity;
import com.example.zealjiang.bean.Mp3ImjadInfoBean;
import com.example.zealjiang.fragment.DialogFragmenFileMerge;
import com.example.zealjiang.fragment.DialogFragmentChangeSuffix;
import com.example.zealjiang.helper.UcToMp3Helper;
import com.example.zealjiang.inf.Mp3ImjadInfoInf;
import com.example.zealjiang.util.FileUtil;
import com.example.zealjiang.util.MaterialManager;
import com.example.zealjiang.util.MediaUtil;
import com.example.zealjiang.util.PIcFilterUtil;
import com.example.zealjiang.util.PermissionUtil;
import com.example.zealjiang.util.log.XLog;

import java.io.File;
import java.util.ArrayList;

public class FileListActivity extends AppCompatActivity implements View.OnClickListener {

    private HandleDataListView mListView;
    private ImageView ivHelp;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private File currentFile;
    String sdRootPath;
    private PermissionUtil permissionUtil;

    private ArrayList<FileEntity> mList;

    private Handler mHandler;
    private int selectItemPos = -1;
    private ArrayList<String> selectPosList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:

                        selectItemPos = -1;
                        boolean needRefresh = false;
                        if(mAdapter ==null){
                            mAdapter = new MyFileAdapter(mContext, mList);
                            mListView.setAdapter(mAdapter);

                            mListView.setDataChangedListener(new HandleDataListView.DataChangedListener() {
                                @Override
                                public void onSuccess() {
                                    XLog.debug("mtest","setDataChangedListener  selectItemPos: "+selectItemPos);
                                    if(selectItemPos > 0){
                                        mListView.setSelection(selectItemPos);
                                    }
                                }
                            });
                        }else{
                            needRefresh = true;
                            XLog.debug("mtest","1 notifyDataSetChanged ");
                            mAdapter.notifyDataSetChanged();
                            XLog.debug("mtest","2 notifyDataSetChanged ");
                        }

                        //选中上次选中的位置
                        if((boolean)msg.obj && selectPosList.size() > 0){
                            String pos_path = selectPosList.get(selectPosList.size() -1);
                            if(!TextUtils.isEmpty(pos_path) && pos_path.contains("_")){
                                String[] array = pos_path.split("_");
                                String pos = array[0];
                                String path = array[1];
                                try{
                                    int posInt = Integer.parseInt(pos);
                                    if(mList.size() > posInt){
                                        if(mList.get(posInt).getFilePath().equals(path)){
                                            //mListView.smoothScrollToPosition(posInt);
                                            //mListView.setSelection(selectItemPos);
                                            selectItemPos = posInt;
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            if(selectPosList.size()>0){
                                //删除刚退出的目录
                                selectPosList.remove(selectPosList.size() -1);
                            }
                        }

                        if(needRefresh){
                            //XLog.debug("mtest","1 notifyDataSetChanged ");
                            //mAdapter.notifyDataSetChanged();
                            //XLog.debug("mtest","2 notifyDataSetChanged ");
                        }

                        break;
                    case 2:

                        break;

                    default:
                        break;
                }
            }
        };

        mContext = this;
        mList = new ArrayList<>();
        sdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        currentFile = new File(sdRootPath);
        //Log.d("mtest"," sdRootPath: "+sdRootPath);
        initView();


        permissionUtil = new PermissionUtil();
        boolean boo = permissionUtil.checkPermission(this);
        if(boo){
            getData(sdRootPath,false);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(permissionUtil == null)return;
        boolean boo = permissionUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(boo){
            getData(sdRootPath,false);
        }else{
            permissionUtil.checkPermission(this);
        }
    }

    @Override
    public void onBackPressed() {
//      super.onBackPressed();
        System.out.println("onBackPressed...");
        if(sdRootPath.equals(currentFile.getAbsolutePath())){
            System.out.println("已经到了根目录...");
            selectPosList.clear();
            return ;
        }

        String parentPath = currentFile.getParent();
        currentFile = new File(parentPath);
        getData(parentPath,true);
    }

    private void initView() {
        mListView = findViewById(R.id.listView1);
        ivHelp = findViewById(R.id.ivHelp);

        ivHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyApplication.getContext(),"email:zealjiang@126.com\nphone:18618269575",Toast.LENGTH_SHORT).show();

                //test
                //boolean boo = permissionUtil.checkPermission(FileListActivity.this);
                //UcToMp3Helper ucToMp3Helper = new UcToMp3Helper(FileListActivity.this);
                //ucToMp3Helper.readFile("/sdcard/1536558-160000-f6e3973a658886c5cae941c5443aa373.mp3.uc!");
                //MediaUtil.getMedieInfo("/sdcard/1536558-160000-f6e3973a658886c5cae941c5443aa373.mp3.uc!");
                //MediaUtil.getMedieInfo(MaterialManager.UC_MUSIC_DIR+"temp.mp3");


/*                ucToMp3Helper.obtainMp3Info(null, "1536558", new Mp3ImjadInfoInf() {
                    @Override
                    public void onSuccess(Mp3ImjadInfoBean bean) {
                        if(bean == null || bean.getSongs() == null || bean.getSongs().size() == 0)return;
                        String title = bean.getSongs().get(0).getName();
                        if(TextUtils.isEmpty(title)){
                            XLog.debug("mtest","网络获取歌曲名失败");
                            return;
                        }

                        String newTitle = "/sdcard/"+title+".mp3";
                        (new File(MaterialManager.UC_MUSIC_DIR+"temp.mp3")).renameTo(new File(newTitle));
                    }

                    @Override
                    public void onFailure(int code, String errMsg) {
                        XLog.debug("mtest","网络获取歌曲名失败+"+errMsg);
                    }
                });*/
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final FileEntity entity = mList.get(position);
                if(entity.getFileType() == FileEntity.Type.FLODER){
                    currentFile = new File(entity.getFilePath());
                    getData(entity.getFilePath(),false);
                    selectPosList.add(position+"_"+entity.getFilePath());
                }else if(entity.getFileType() == FileEntity.Type.FILE){

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
/*                            Toast.makeText(mContext, entity.getFilePath()+"  "+entity.getFileName(),
                                    Toast.LENGTH_SHORT).show();*/

                            String path = entity.getFilePath();
                            if(TextUtils.isEmpty(path)){
                                return;
                            }
                            if(FileUtil.isPic(path)) {
                                Intent intent = new Intent(FileListActivity.this, SpaceImageDetailActivity.class);
                                intent.putExtra("url", path);
                                startActivity(intent);
//                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            }else{
                                //根据后缀名判断文件格式
                                boolean boo = FileUtil.isText(path);
                                boo = true;
                                if(boo) {
                                    Intent intent = new Intent(FileListActivity.this, EditorActivity.class);
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(entity.getFilePath()));
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(MyApplication.getContext(),"文件格式不支持",Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    });
                }

            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final FileEntity entity = mList.get(position);
                if(entity.getFileType() == FileEntity.Type.FLODER){
                    modifyDirFilesSuffix(entity.getFilePath());
                    //mergeFile(entity.getFilePath());  //
                }else if(entity.getFileType() == FileEntity.Type.FILE){
                    Toast.makeText(MyApplication.getContext(),"文件",Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    /**
     * 多个片段文件合成一个文件
     * @param dirPath
     */
    private void modifyDirFilesSuffix(String dirPath){
        //提醒
        final DialogFragmentChangeSuffix dialogFragmentChangeSuffix = DialogFragmentChangeSuffix.newInstance(dirPath);
        dialogFragmentChangeSuffix.setCreditExchangeInf(new DialogFragmentChangeSuffix.CreditExchangeInf() {
            @Override
            public void exchange(boolean isNotify) {

            }
            @Override
            public void cancel() {

            }
        });
        dialogFragmentChangeSuffix.show(this.getSupportFragmentManager(),DialogFragmentChangeSuffix.class.getSimpleName());
    }

    private void mergeFile(String dirPath){
        //提醒
        final DialogFragmenFileMerge dialogFragmenFileMerge = DialogFragmenFileMerge.newInstance(dirPath);
        dialogFragmenFileMerge.setCreditExchangeInf(new DialogFragmenFileMerge.CreditExchangeInf() {
            @Override
            public void exchange(boolean isNotify) {

            }
            @Override
            public void cancel() {

            }
        });
        dialogFragmenFileMerge.show(this.getSupportFragmentManager(),DialogFragmentChangeSuffix.class.getSimpleName());
    }

    private void getData(final String path,final boolean isBack) {
        new Thread(){
            @Override
            public void run() {
                super.run();

                findAllFiles(path,isBack);
            }
        }.start();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }

    }

    /**
     * 查找path地址下所有文件
     * @param path
     */
    public void findAllFiles(String path,final boolean isBack) {
        mList.clear();

        if(path ==null ||path.equals("")){
            return;
        }
        File fatherFile = new File(path);
        File[] files = fatherFile.listFiles();
        Log.d("mtest"," files: "+files+"  fatherFile.exists :"+fatherFile.exists()
                +"  fatherFile.isDirectory :"+fatherFile.isDirectory());
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                FileEntity entity = new FileEntity();
                boolean isDirectory = files[i].isDirectory();
                if(isDirectory ==true){
                    entity.setFileType(FileEntity.Type.FLODER);
                }else{
                    entity.setFileType(FileEntity.Type.FILE);
                }
                entity.setFileName(files[i].getName().toString());
                entity.setFilePath(files[i].getAbsolutePath());
                entity.setFileSize(files[i].length()+"");
                mList.add(entity);
            }
        }

        Message msg = new Message();
        msg.obj = isBack;
        msg.what = 1;
        mHandler.sendMessage(msg);

    }


    class MyFileAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FileEntity> mAList;
        private LayoutInflater mInflater;



        public MyFileAdapter(Context mContext, ArrayList<FileEntity> mList) {
            super();
            this.mContext = mContext;
            this.mAList = mList;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mAList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mAList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if(mAList.get(position).getFileType() == FileEntity.Type.FLODER){
                return 0;
            }else{
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//          System.out.println("position-->"+position+"    ---convertView--"+convertView);
            ViewHolder holder = null;
            int type = getItemViewType(position);
            FileEntity entity = mAList.get(position);

            if(convertView == null){
                holder = new ViewHolder();
                switch (type) {
                    case 0://folder
                        convertView = mInflater.inflate(R.layout.item_listview, parent, false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);
                        break;
                    case 1://file
                        convertView = mInflater.inflate(R.layout.item_listview, parent, false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.ivPic = (ImageView) convertView.findViewById(R.id.item_ivPic);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);

                        break;

                    default:
                        break;

                }
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            switch (type) {
                case 0:
                    holder.iv.setImageResource(R.mipmap.folder);
                    holder.tv.setText(entity.getFileName());
                    break;
                case 1:
                    holder.iv.setImageResource(R.mipmap.file);
                    holder.tv.setText(entity.getFileName());
                    String path = entity.getFilePath();
                    if(FileUtil.isPic(path)){
                        holder.iv.setVisibility(View.GONE);
                        holder.ivPic.setVisibility(View.VISIBLE);
                        Glide.with(FileListActivity.this).load(path).into(holder.ivPic);
                    }else{
                        holder.iv.setVisibility(View.VISIBLE);
                        holder.ivPic.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }


            return convertView;
        }

    }

    class ViewHolder {
        ImageView iv;
        ImageView ivPic;
        TextView tv;
    }
}
