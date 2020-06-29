package com.example.zealjiang.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zealjiang.txtjsoneditor.R;
import com.example.zealjiang.util.TextViewResetUtils;

/**
 * Created by gcw
 * 原则上APP应该有公共的状态切换页面，本类没有详细处理公共情况，待完善，请继承使用
 * 本Layout给所有子Page设置Clickable=true防止点击传穿透
 * 不同界面背景是否透明会导致UI和Content重叠，需要自行变更处理
 * 本类当前默认是除了showloading VISIBLE contentView外其他INVISIBLE contentView
 * 注意contentView必须只能有一个，请包裹使用
 * 默认内置4种Loading样式，1无背景、2有黑框背景有字、3有黑框背景无字、4有黑框背景外加progress可取消
 * 增加黑白两种页面状态，默认黑色样式
 */

public class PageStateLayout extends RelativeLayout {

    protected int emptyResId;
    protected int errorNetResId;
    protected int errorServerResId;
    protected int loadingResId;
    protected int loginResId;

    protected int loadingType;
    public static final String DEFAULT_LOADING_HINT = "Loading...";
    protected String loadingHint;
    protected String loginHint;
    protected int chidViewPaddingTop, chidViewPaddingBottom = 0;

    protected boolean isChildClickable = true;

    protected ViewGroup contentLayout;

    protected int curPageState = PAGE_STATE_LOADING;
    protected View curExtraPageView;

    public static final int PAGE_STATE_CONTENT = 0;
    public static final int PAGE_STATE_ERROR_NET = -1;
    public static final int PAGE_STATE_ERROR_SERVER = -2;
    public static final int PAGE_STATE_LOGIN = -3;
    public static final int PAGE_STATE_EMPTY = -4;
    public static final int PAGE_STATE_LOADING = -5;
    public static final int PAGE_STATE_NULL = -6;

    public static final int PAGE_SKIN_BLACK = 1;
    public static final int PAGE_SKIN_WHITE = 2;

    public static final int LOADING_TYPE_NOBG = 1;
    public static final int PAGE_STATE_BG = 2;
    public static final int PAGE_STATE_BG_NOTEXT = 3;
    public static final int PAGE_STATE_PROCESS = 4;

    protected LayoutInflater layoutInflater;
    private AnimationDrawable loadingAnimDrawable;
    private DonutProgressView donutProgressView;
    private int progress = 0;

    private int skinMode = PAGE_SKIN_BLACK;

    public int getSkinMode() {
        return skinMode;
    }

    public void setSkinMode(int skinMode) {
        this.skinMode = skinMode;
    }

    public PageStateLayout(Context context) {
        this(context, null);
    }

    public PageStateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageStateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layoutInflater = LayoutInflater.from(getContext());
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PageStateLayout, 0, 0);
        try {
            emptyResId = typedArray.getResourceId(R.styleable.PageStateLayout_emptyId, R.layout.layout_default_page_state_empty);
            errorNetResId = typedArray.getResourceId(R.styleable.PageStateLayout_errorNetId, R.layout.layout_default_page_state_error_net);
            errorServerResId = typedArray.getResourceId(R.styleable.PageStateLayout_errorServerId, R.layout.layout_default_page_state_error_server);
            loginResId = typedArray.getResourceId(R.styleable.PageStateLayout_loginId, R.layout.layout_default_page_state_login);
            skinMode = typedArray.getInt(R.styleable.PageStateLayout_skinMode, PAGE_SKIN_BLACK);
            loadingType = typedArray.getInt(R.styleable.PageStateLayout_loadingType, LOADING_TYPE_NOBG);
            loadingResId = typedArray.getResourceId(R.styleable.PageStateLayout_loadingId, getLoadingLayoutByLoadingType(loadingType));
            loadingHint = typedArray.getString(R.styleable.PageStateLayout_loadingHint);
            loginHint = typedArray.getString(R.styleable.PageStateLayout_loginHint);
            chidViewPaddingTop = typedArray.getDimensionPixelSize(R.styleable.PageStateLayout_childViewPaddingTop, 0);
            chidViewPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.PageStateLayout_childViewPaddingBottom, 0);
            isChildClickable = typedArray.getBoolean(R.styleable.PageStateLayout_isChildClickable, true);
            if (TextUtils.isEmpty(loadingHint)) {
                loadingHint = DEFAULT_LOADING_HINT;
            }
        } finally {
            typedArray.recycle();
        }
    }

    private int getLoadingLayoutByLoadingType(int type) {
        int loadingLayout = R.layout.layout_default_page_state_loading_nobg;
        switch (type) {
            case PAGE_STATE_BG:
                loadingLayout = R.layout.layout_default_page_state_loading_bg;
                break;
            case PAGE_STATE_BG_NOTEXT:
                loadingLayout = R.layout.layout_default_page_state_loading_bg_notext;
                break;
            case PAGE_STATE_PROCESS:
                loadingLayout = R.layout.layout_default_page_state_loading_progress;
                break;
            default:
        }
        return loadingLayout;
    }

    public void setLoadingType(int loadingType){
        loadingResId = getLoadingLayoutByLoadingType(loadingType);
    }

    public interface OnPageStateChangeListener {
        void onPageStateChanged(int pageSate);
    }

    public interface OnStatePageClickListener {

        void onEmptyStateClick();

        void onLoginStateClick();

        void onErrorNetStateClick();

        void onErrorServerStateClick();
    }

    public interface OnLoadingCancelListener {
        void onLoadingCancel();
    }

    protected OnPageStateChangeListener onPageStateChangeListener;
    protected OnStatePageClickListener onStatePageClickListener;
    protected OnLoadingCancelListener onLoadingCancelListener;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onContentViewAdded();
    }


    public void onContentViewAdded() {
        int childCount = getChildCount();
        if (childCount != 1) {
            return;
        }
        contentLayout = (ViewGroup) getChildAt(0);
        if (isChildClickable) {
            contentLayout.setClickable(true);
        }
    }

    public void setOnPageStateChangeListener(OnPageStateChangeListener onPageStateChangeListener) {
        this.onPageStateChangeListener = onPageStateChangeListener;
    }

    public void setOnStatePageClickListener(OnStatePageClickListener onStatePageClickListener) {
        this.onStatePageClickListener = onStatePageClickListener;
    }

    public void setOnLoadingCancelListener(OnLoadingCancelListener onLoadingCancelListener) {
        this.onLoadingCancelListener = onLoadingCancelListener;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (donutProgressView != null) {
            donutProgressView.setProgress(progress);
        }
    }

    public void showContent() {
        showPage(PAGE_STATE_CONTENT);
    }

    public void showErrorServer() {
        showError(PAGE_STATE_ERROR_SERVER);
    }

    public void showErrorNet() {
        showError(PAGE_STATE_ERROR_NET);
    }

    public void showError(int errorState) {
        showPage(errorState);
    }

    public void showEmpty(boolean ...isSelf) {
        showPage(PAGE_STATE_EMPTY);
    }

    public void showLoading() {
        showPage(PAGE_STATE_LOADING);
    }

    public void showLogin() {
        showPage(PAGE_STATE_LOGIN);
    }

    public void showNull() {
        showPage(PAGE_STATE_NULL);
    }

    public void showPage(int pageState) {
        int childCount = getChildCount();
        if (childCount < 1 || contentLayout == null) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != contentLayout) {
                removeView(child);
            }
        }
        curPageState = pageState;
        curExtraPageView = handPageByState(pageState);
        if (onPageStateChangeListener != null) {
            onPageStateChangeListener.onPageStateChanged(pageState);
        }
    }

    protected View handPageByState(int pageState) {
        View extraView = null;
        boolean contentVisible = true;
        switch (pageState) {
            case PAGE_STATE_CONTENT:
                extraView = null;
                break;
            case PAGE_STATE_ERROR_NET:
                ViewGroup errorNetLayout = getErrorNetLayout();
                if (errorNetLayout != null) {
                    addView(errorNetLayout);
                    contentVisible = false;
                    extraView = errorNetLayout;
                }
                break;
            case PAGE_STATE_ERROR_SERVER:
                ViewGroup errorServerLayout = getErrorServerLayout();
                if (errorServerLayout != null) {
                    addView(errorServerLayout);
                    contentVisible = false;
                    extraView = errorServerLayout;
                }
                break;
            case PAGE_STATE_EMPTY:
                ViewGroup emptyLayout = getEmptyLayout();
                if (emptyLayout != null) {
                    addView(emptyLayout);
                    contentVisible = false;
                    extraView = emptyLayout;
                }
                break;
            case PAGE_STATE_LOADING:
                ViewGroup loadingLayout = getLoadingLayout();
                if (loadingLayout != null) {
                    addView(loadingLayout);
                    contentVisible = true;
                    extraView = loadingLayout;
                }
                break;
            case PAGE_STATE_LOGIN:
                ViewGroup loginLayout = getLoginLayout();
                if (loginLayout != null) {
                    addView(loginLayout);
                    contentVisible = false;
                    extraView = loginLayout;
                }
                break;
            case PAGE_STATE_NULL:
                contentVisible = false;
                extraView = null;
                break;
            default:
                extraView = null;
        }
        contentLayout.setVisibility(contentVisible ? VISIBLE : INVISIBLE);
        if (loadingAnimDrawable != null && pageState != PAGE_STATE_LOADING) {
            loadingAnimDrawable.stop();
            loadingAnimDrawable = null;
        }
        if (donutProgressView != null && pageState != PAGE_STATE_LOADING) {
            donutProgressView = null;
        }
        if (extraView != null && isChildClickable) {
            extraView.setClickable(true);
        }
        return extraView;
    }

    /**
     * 如果不传errorNetResId，自行继承重写此方法创建ErrorNetLayout
     *
     * @return
     */
    protected ViewGroup getErrorNetLayout() {
        if (errorNetResId > 0) {
            ViewGroup errorViewGroup = (ViewGroup) layoutInflater.inflate(errorNetResId, this, false);
            if (errorNetResId == R.layout.layout_default_page_state_error_net && onStatePageClickListener != null) {
                errorViewGroup.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStatePageClickListener.onErrorNetStateClick();
                    }
                });
            }
            errorViewGroup.setPadding(0, chidViewPaddingTop, 0, chidViewPaddingBottom);

            if(chidViewPaddingTop > 0){
                ((RelativeLayout)errorViewGroup).setGravity(Gravity.TOP);
                ViewGroup.LayoutParams params = errorViewGroup.getLayoutParams();
                if(params != null){
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    errorViewGroup.setLayoutParams(params);
                }
            }
            return errorViewGroup;
        }
        return null;
    }

    /**
     * 如果不传errorServerResId，自行继承重写此方法创建ErrorServerLayout
     *
     * @return
     */
    protected ViewGroup getErrorServerLayout() {
        if (errorServerResId > 0) {
            ViewGroup errorViewGroup = (ViewGroup) layoutInflater.inflate(errorServerResId, this, false);
            if (errorServerResId == R.layout.layout_default_page_state_error_server) {
                if (skinMode == PAGE_SKIN_WHITE) {
                    TextView titleTextView = errorViewGroup.findViewById(R.id.tv_title);
                    TextView subTitleTextView = errorViewGroup.findViewById(R.id.tv_subtitle);
                    titleTextView.setTextColor(Color.parseColor("#000000"));
                    subTitleTextView.setTextColor(Color.parseColor("#999999"));
                }
                if (onStatePageClickListener != null) {
                    View retryView = errorViewGroup.findViewById(R.id.tv_retry_action);
                    if (retryView != null) {
                        retryView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onStatePageClickListener.onErrorNetStateClick();
                            }
                        });
                    }
                }
            }
            errorViewGroup.setPadding(0, chidViewPaddingTop, 0, chidViewPaddingBottom);
            if(chidViewPaddingTop > 0){
                ((RelativeLayout)errorViewGroup).setGravity(Gravity.TOP);
                ViewGroup.LayoutParams params = errorViewGroup.getLayoutParams();
                if(params != null){
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    errorViewGroup.setLayoutParams(params);
                }
            }
            return errorViewGroup;
        }
        return null;
    }

    /**
     * 如果不传loadingResId，自行继承重写此方法创建LoadingLayout
     *
     * @return
     */
    protected ViewGroup getLoadingLayout() {
        ViewGroup loadingViewGroup = null;
        if (loadingResId > 0) {
            loadingViewGroup = (ViewGroup) layoutInflater.inflate(loadingResId, this, false);
            loadingViewGroup.setPadding(0, chidViewPaddingTop, 0, chidViewPaddingBottom);
            if (loadingResId == R.layout.layout_default_page_state_loading_nobg) {
                TextView loadingTextView = loadingViewGroup.findViewById(R.id.loading_textView);
                if (loadingTextView != null) {
                    loadingTextView.setText(loadingHint);
                }
                ImageView loadingImageView = loadingViewGroup.findViewById(R.id.loading_imageView);
                loadingAnimDrawable = (AnimationDrawable) loadingImageView.getDrawable();
                if (loadingAnimDrawable != null) {
                    loadingAnimDrawable.start();
                }
            } else if (loadingResId == R.layout.layout_default_page_state_loading_bg ||
                    loadingResId == R.layout.layout_default_page_state_loading_bg_notext) {
                TextView loadingTextView = loadingViewGroup.findViewById(R.id.loading_textView);
                if (loadingTextView != null) {
                    loadingTextView.setText(loadingHint);
                }
                ImageView loadingImageView = loadingViewGroup.findViewById(R.id.loading_imageView);
                loadingAnimDrawable = (AnimationDrawable) loadingImageView.getDrawable();
                if (loadingAnimDrawable != null) {
                    loadingAnimDrawable.start();
                }
            } else if (loadingResId == R.layout.layout_default_page_state_loading_progress) {
                TextView loadingTextView = loadingViewGroup.findViewById(R.id.loading_textView);
                if (loadingTextView != null) {
                    loadingTextView.setText(loadingHint);
                }
                TextViewResetUtils.reSizeTextView(loadingTextView ,"Reverse processing..." , 160);
                donutProgressView = loadingViewGroup.findViewById(R.id.loading_progress);
                View cancelView = loadingViewGroup.findViewById(R.id.loading_cancel);
                if (cancelView != null && onLoadingCancelListener != null) {
                    cancelView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onLoadingCancelListener.onLoadingCancel();
                        }
                    });
                }
            }
        }
        return loadingViewGroup;
    }

    /**
     * 如果不传emptyResId，自行继承重写此方法创建EmptyLayout
     *
     * @return
     */
    protected ViewGroup getEmptyLayout() {
        if (emptyResId > 0) {
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(emptyResId, this, false);
            if (emptyResId == R.layout.layout_default_page_state_empty) {

            }
            viewGroup.setPadding(0, chidViewPaddingTop, 0, chidViewPaddingBottom);
            return viewGroup;
        }
        return null;
    }

    /**
     * 如果不传loginResId，自行继承重写此方法创建LoginLayout
     *
     * @return
     */
    protected ViewGroup getLoginLayout() {
        if (loginResId > 0) {
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(loginResId, this, false);
            if (loginResId == R.layout.layout_default_page_state_login) {
                TextView titleTextView = viewGroup.findViewById(R.id.tv_title);
                TextView subTitleTextView = viewGroup.findViewById(R.id.tv_subtitle);
                TextView actionTextView = viewGroup.findViewById(R.id.tv_login_action);
                if (!TextUtils.isEmpty(loginHint)) {
                    subTitleTextView.setText(loginHint);
                }
                if (skinMode == PAGE_SKIN_WHITE) {
                    titleTextView.setTextColor(0xff000000);
                    subTitleTextView.setTextColor(0xff999999);
                }
                if (onStatePageClickListener != null) {
                    actionTextView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onStatePageClickListener.onLoginStateClick();
                        }
                    });
                }
            }
            viewGroup.setPadding(0, chidViewPaddingTop, 0, chidViewPaddingBottom);
            return viewGroup;
        }
        return null;
    }

    public int getCurPageState() {
        return curPageState;
    }

    public View getCurExtraPageView() {
        return curExtraPageView;
    }

    public void setEmptyResId(int emptyResId) {
        this.emptyResId = emptyResId;
    }

    public void setLoadingResId(int loadingResId) {
        this.loadingResId = loadingResId;
    }

    public void setErrorNetResId(int errorNetResId) {
        this.errorNetResId = errorNetResId;
    }

    public void setErrorServerResId(int errorServerResId) {
        this.errorServerResId = errorServerResId;
    }

    public void setLoginResId(int loginResId) {
        this.loginResId = loginResId;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public boolean isPageStateIsLogin() {
        return curPageState == PAGE_STATE_LOGIN;
    }
}
