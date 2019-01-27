package com.example.administrator.selectimgupload;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.util.List;

/**
 * 展示相册，选择图片
 */
public class MyAdapter extends CommonAdapter<String> {
    /**
     * 文件夹路径
     */
    private String mDirPath;
    private Context context;
    private int chooseType;
    private List<String> result;
    private String flag;

    public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
                     String dirPath, int chooseType, List<String> result, String flag) {
        super(context, mDatas, itemLayoutId);
        this.mDirPath = dirPath;
        this.context = context;
        this.chooseType = chooseType;
        this.result = result;
        this.flag = flag;
    }

    @Override
    public void convert(final ViewHolder helper, final String item) {

        //设置no_pic
        helper.setImageResource(R.id.id_item_image, R.mipmap.photo_pictures_no);
        //设置no_selected
        helper.setImageResource(R.id.id_item_select,
                R.mipmap.photo_picture_unselected);
        //设置图片
        final String imgPath = mDirPath + "/" + item;
        helper.setImageByUrl(context, R.id.id_item_image, imgPath);

        final ImageView mImageView = helper.getView(R.id.id_item_image);
        final ImageView mSelect = helper.getView(R.id.id_item_select);

        mImageView.setColorFilter(null);
        if (chooseType == ImageChooserHelper.TYPE_SINGLE) {
            mSelect.setVisibility(View.GONE);
            //显示确认上传对话框
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//					((AppCompatActivity)mContext).showDialog("友情提示：确认选择这张图片？", "确认", "取消", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    result.add(imgPath);
                    intent.putExtra(ImageChooserHelper.CHOOSE_IMAGES, imgPath).putExtra("flag", flag);
                    ((AppCompatActivity) mContext).setResult(((AppCompatActivity) mContext).RESULT_OK, intent);
                    ((AppCompatActivity) mContext).finish();
//						}
//					},
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							((AppCompatActivity)mContext).alertDialog.dismiss();
//						}
//					});
                }
            });
            return;
        } else if (chooseType == ImageChooserHelper.TYPE_MULTY) {
            //设置ImageView的点击事件
            mImageView.setOnClickListener(new OnClickListener() {
                //选择，则将图片变暗，反之则反之
                @Override
                public void onClick(View v) {
                    // 已经选择过该图片
                    if (result.contains(imgPath)) {
                        result.remove(imgPath);
                        mSelect.setImageResource(R.mipmap.photo_picture_unselected);
                        mImageView.setColorFilter(null);
                    } else
                    // 未选择该图片
                    {
                        //限定最多只能添加9张图片
//                        ToastUtil.showToastSHORT("result.size = " + result.size());
                        if (result.size() >= ImageChooserHelper.max) { //比较之前，集合长度没加1，就是还没有选择图片
                            ToastUtil.showToastSHORT("最多再上传" + ImageChooserHelper.max + "张图片！");
                            return;
                        } else {
                            result.add(imgPath);
                            mSelect.setImageResource(R.mipmap.photo_pictures_selected);
                            mImageView.setColorFilter(Color.parseColor("#77000000"));
                        }
                    }
                }
            });
        }

        /**
         * 已经选择过的图片，显示出选择过的效果
         */
        if (result.contains(imgPath)) {
            mSelect.setImageResource(R.mipmap.photo_pictures_selected);
            mImageView.setColorFilter(Color.parseColor("#77000000"));
        }
    }
}
