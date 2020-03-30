package com.example.zealjiang.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.zealjiang.MyApplication;
import com.example.zealjiang.txtjsoneditor.R;
import com.example.zealjiang.util.ScreenUtils;

/**
 * 批量修改后缀
 */
public class DialogFragmentChangeSuffix extends DialogFragment {

    private final String TAG = "DialogFragmentConsumeCredit";
    private TextView tvCancel;
    private TextView tvConfirm;
    private EditText etSuffix;

    private String fileDirPath;


    public static DialogFragmentChangeSuffix newInstance(String fileDirPath) {
        Bundle args = new Bundle();
        args.putSerializable("fileDirPath", fileDirPath);
        DialogFragmentChangeSuffix fragment = new DialogFragmentChangeSuffix();
        fragment.setArguments(args);
        return fragment;
    }

    public void setData(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogCredit);
    }

    @Override
    public void onStart() {
        super.onStart();

        //设置显示的大小
        Window win = getDialog().getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        int margin = ScreenUtils.dipConvertPx(MyApplication.getContext(),10);
        int screenWidth = ScreenUtils.getScreenWidth(MyApplication.getContext());

        params.width =  screenWidth - margin * 2;
        //params.height = height;

        //底边距
        int marginBottom = margin;
        params.y = marginBottom;
        //params.gravity = Gravity.BOTTOM;
        win.setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_suffix, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            fileDirPath = getArguments().getString("fileDirPath");
        }

        tvCancel = view.findViewById(R.id.tvCancel);
        tvConfirm = view.findViewById(R.id.tvConfirm);
        etSuffix = view.findViewById(R.id.etSuffix);


        setListener();
        init();
    }

    private void setListener(){
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(creditExchangeInf != null){
                    creditExchangeInf.exchange(false);
                }


                close();
            }
        });

    }

    private void init(){


    }

    public void close(){

        DialogFragmentChangeSuffix.this.dismiss();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private CreditExchangeInf creditExchangeInf;
    public interface CreditExchangeInf{
        public void exchange(boolean isNotify);
        public void cancel();
    }

    public void setCreditExchangeInf(CreditExchangeInf creditExchangeInf){
        this.creditExchangeInf = creditExchangeInf;
    }
}
