package com.example.zealjiang.txtjsoneditor;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;


public class SpaceImageDetailActivity extends AppCompatActivity {
    private SpaceImageDetailActivity mActivity;
    private String mUrl;
    private int resId;
    private boolean canModify;

    @BindView(R.id.photoDraweeView)
    PhotoDraweeView photoDraweeView;
    @BindView(R.id.ibBack)
    ImageButton ibBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_image_detail_layout);
        ButterKnife.bind(this);
        mActivity = this;
        mUrl = getIntent().getStringExtra("url");
        resId = getIntent().getIntExtra("resId",0);

        if(TextUtils.isEmpty(mUrl)){
            finish();
            return;
        }


        if(mUrl.startsWith("http")){
            String detailUrl = mUrl.replace("/list/", "/detail/");
            photoDraweeView.setPhotoUri(Uri.parse(detailUrl));
        }else if(mUrl.startsWith("res")){
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(Uri.parse("res:///" + resId));
            controller.setOldController(photoDraweeView.getController());
            // You need setControllerListener
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null || photoDraweeView == null) {
                        return;
                    }
                    photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            photoDraweeView.setController(controller.build());

        }else {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.evictFromCache(Uri.parse("file://"+mUrl));
            photoDraweeView.setPhotoUri(Uri.parse("file://"+mUrl));
        }



        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }


}
