package com.example.zealjiang.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zealjiang.bean.FileBean;
import com.example.zealjiang.txtjsoneditor.R;

import java.util.List;

/**
 * Created by lizhijiang on 2018/1/22.
 */

public class FileSearchAdapter extends RecyclerView.Adapter<FileSearchAdapter.ViewHolder>  {

    private Context context;
    private List<FileBean> data;

    public FileSearchAdapter(Context context, List<FileBean> data){
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file,parent,false);
        FileSearchAdapter.ViewHolder viewHolder = new FileSearchAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileBean fileBean = data.get(position);
        if(fileBean.getFileType() == FileBean.FileType.DIRECTORY){
            Glide.with(context).load(R.mipmap.directory).into(holder.iv);
        }else if(fileBean.getFileType() == FileBean.FileType.FILE){
            Glide.with(context).load(R.mipmap.file).into(holder.iv);
        }
        holder.tvFileName.setText(fileBean.getFileName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv;
        private TextView tvFileName;

        public ViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.iv);
            tvFileName = (TextView) itemView.findViewById(R.id.tvFileName);
        }
    }
}
