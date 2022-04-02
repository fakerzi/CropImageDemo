package com.axiang.cropimagedemo.editimg.paint;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.view.paint.PaintColorCircleView;

/**
 * 涂鸦 Fragment
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintFragment extends BaseEditImageFragment implements ColorPaintAdapter.OnItemClickListener,
        ImagePaintAdapter.OnItemClickListener {

    public static final int INDEX = ModuleConfig.INDEX_PAINT;
    private static final int DEFAULT_RED = 45;
    private static final int DEFAULT_GREEN = 215;
    private static final int DEFAULT_BLUE = 215;
    private static final int DEFAULT_STOKE_WIDTH = 10;

    private final int[] mPaintColors = {Color.BLACK,
            Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, R.drawable.ic_more};
    private final int[] mImagePaintMaterials = {R.drawable.image_paint_material_01,
            R.drawable.image_paint_material_02};

    private ImageView mIvBackToMain;
    private ViewFlipper mViewFlipperPaint;
    private RecyclerView mRvColorPaintList;
    private Button mBtnSwitchImage;
    private RecyclerView mRvImagePaintList;
    private Button mBtnSwitchColor;
    private ImageView mPaintEraser;
    private PaintColorCircleView mPaintColorCircleView;

    private PopupWindow mPwSetStokeWidth;
    private SeekBar mSeekBarSetStokeWidth;
    private View mViewSetStokeWidth;

    private ColorPaintAdapter mColorPaintAdapter;
    private ImagePaintAdapter mImagePaintAdapter;

    private int mRed, mGreen, mBlue;

    private boolean mIsEraser = false;    // 是否是擦除模式

    public static PaintFragment newInstance() {
        return new PaintFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paint, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mViewFlipperPaint = rootView.findViewById(R.id.view_flipper_paint);
        mRvColorPaintList = rootView.findViewById(R.id.rv_color_paint_list);
        mBtnSwitchImage = rootView.findViewById(R.id.btn_switch_image);
        mRvImagePaintList = rootView.findViewById(R.id.rv_image_paint_list);
        mBtnSwitchColor = rootView.findViewById(R.id.btn_switch_color);
        mPaintEraser = rootView.findViewById(R.id.paint_eraser);
        mPaintColorCircleView = rootView.findViewById(R.id.paint_color_circle_view);

        mViewFlipperPaint.setInAnimation(mActivity, R.anim.in_bottom_to_top);
        mViewFlipperPaint.setOutAnimation(mActivity, R.anim.out_bottom_to_top);

        initRv();
        resetPaint();
        initPw();

        mIvBackToMain.setOnClickListener(view -> backToMain());
        mBtnSwitchImage.setOnClickListener(view -> mViewFlipperPaint.showNext());
        mBtnSwitchColor.setOnClickListener(view -> mViewFlipperPaint.showPrevious());
        mPaintEraser.setOnClickListener(view -> updateEraser(!mIsEraser));
        mPaintColorCircleView.setOnClickListener(view -> showSetStokeWidthPw());
    }

    private void initRv() {
        mRvColorPaintList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvColorPaintList.setLayoutManager(mLayoutManager);
        mColorPaintAdapter = new ColorPaintAdapter(mActivity, mPaintColors);
        mColorPaintAdapter.setOnItemClickListener(this);
        mRvColorPaintList.setAdapter(mColorPaintAdapter);

        mRvColorPaintList.setHasFixedSize(true);
        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(mActivity);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvImagePaintList.setLayoutManager(stickerListLayoutManager);
        mImagePaintAdapter = new ImagePaintAdapter(mActivity, mImagePaintMaterials);
        mImagePaintAdapter.setOnItemClickListener(this);
        mRvImagePaintList.setAdapter(mImagePaintAdapter);
    }

    private void resetPaint() {
        mPaintColorCircleView.setStokeWidth(DEFAULT_STOKE_WIDTH);
        setPaintColor(DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE);
    }

    private void initPw() {
        mViewSetStokeWidth = LayoutInflater.from(mActivity).inflate(R.layout.view_set_stoke_width, null);
        mPwSetStokeWidth = new PopupWindow(mViewSetStokeWidth, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mSeekBarSetStokeWidth = mViewSetStokeWidth.findViewById(R.id.seekbar_stoke_width);

        mPwSetStokeWidth.setOutsideTouchable(true);
        mPwSetStokeWidth.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void updateEraser(boolean eraser) {
        mIsEraser = eraser;
        mPaintEraser.setSelected(mIsEraser);
        mActivity.mPaintView.setEraser(mIsEraser);
    }

    @Override
    public void onColorClick(int position) {
        int color = mPaintColors[position];
        setPaintColor(Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public void onMoreClick() {
        DialogUtil.showColorPickerDialog(mActivity.getSupportFragmentManager(), mRed, mGreen, mBlue,
                this::setPaintColor);
    }

    private void setPaintColor(int red, int green, int blue) {
        mRed = red;
        mGreen = green;
        mBlue = blue;
        mPaintColorCircleView.setStokeColor(Color.rgb(mRed, mGreen, mBlue));
        updatePaintView();
    }

    @Override
    public void onImageClick(int position) {
        updateEraser(false);
    }

    private void showSetStokeWidthPw() {
        mSeekBarSetStokeWidth.setMax(mPaintColorCircleView.getHeight());
        mSeekBarSetStokeWidth.setProgress((int) mPaintColorCircleView.getStokeWidth());
        mSeekBarSetStokeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintColorCircleView.setStokeWidth(progress);
                updatePaintView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mPwSetStokeWidth.showAtLocation(mActivity.mBottomOperateBar,
                Gravity.BOTTOM, 0, mActivity.mBottomOperateBar.getHeight());
    }

    /**
     * 更新画布的画笔设置
     */
    private void updatePaintView() {
        updateEraser(false);
        mActivity.mPaintView.setStokeColor(mPaintColorCircleView.getStokeColor());
        mActivity.mPaintView.setStokeWidth(mPaintColorCircleView.getStokeWidth());
    }

    @Override
    public void onShow() {
        mActivity.mMode = EditImageActivity.Mode.PAINT;
        mActivity.mViewFlipperSave.showNext();
        mActivity.mPaintView.setVisibility(View.VISIBLE);
    }

    @Override
    public void backToMain() {
        resetPaint();
        mActivity.mPaintView.resetBitmap();
        mActivity.mMode = EditImageActivity.Mode.NONE;
        mActivity.mPaintView.setVisibility(View.GONE);
        mActivity.mBottomOperateBar.setCurrentItem(0);
        mActivity.mViewFlipperSave.showPrevious();
    }


    public void applyPaints() {
        
    }
}