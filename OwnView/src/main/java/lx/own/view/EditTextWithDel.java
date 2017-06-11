package lx.own.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import lx.own.R;


/**
 * Created by Lx on 2016/10/29.
 */

public class EditTextWithDel extends EditText {
    private Drawable imgInable;
    private Context mContext;
    private Rect mCurrentRect;

    public EditTextWithDel(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mCurrentRect = new Rect();
        imgInable = mContext.getResources().getDrawable(R.drawable.icon_delete);
        EditTextWithDel.this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setDrawable();
            }
        });
        setDrawable();
    }

    private void setDrawable() {
        if(EditTextWithDel.this.length() > 0){
            EditTextWithDel.this.setCompoundDrawablesWithIntrinsicBounds(null,null,imgInable,null);
        }else {
            EditTextWithDel.this.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);

        }
    }

    //处理删除事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(imgInable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            mCurrentRect.right = getWidth();
            mCurrentRect.bottom = getHeight();
            mCurrentRect.top = 0;
            mCurrentRect.left = mCurrentRect.right - imgInable.getIntrinsicWidth();
            if(mCurrentRect.contains(eventX,eventY)) {
                EditTextWithDel.this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }
}
