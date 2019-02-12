package com.example.administrator.selectimgupload;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * 展示相册，选择图片
 */
public class ImageFolderActivity extends AppCompatActivity implements ListImageDirPopupWindow.OnImageDirSelected {
    private static final String TAG = "----->ImageFolderActivity";
    private ProgressDialog mProgressDialog;

    private ArrayList<String> imageList = new ArrayList<>();    //维护已选择图片,返回给外部
    private int currentType;    //记录当前选择类型 (1单选，2多选)

    /**
     * 存储文件夹中的图片数量
     */
    private int mPicsSize;
    /**
     * 图片数量最多的文件夹
     */
    private File mImgDir;
    /**
     * 所有的图片,仅用于保存所有
     */
    private List<String> mImgs = new ArrayList<>();

    private GridView mGirdView;
    private MyAdapter mAdapter;
    /**
     * 临时的辅助类，用于防止同一个文件夹的多次扫描
     */
    private HashSet<String> mDirPaths = new HashSet<String>();

    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageFolder> mImageFloders = new ArrayList<ImageFolder>();

    private RelativeLayout mBottomLy;

    private TextView mChooseDir;
    private TextView mImageCount;
    int totalCount = 0;

    private int mScreenHeight;

    private ListImageDirPopupWindow mListImageDirPopupWindow;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mProgressDialog.dismiss();
            //为folder绑定第一个数据
            ImageFolder imageFloder = new ImageFolder();
            imageFloder.setDir("/全部图片");
            if (mImgs != null) {
                imageFloder.setCount(mImgs.size());
                if (mImgs.size() != 0) {
                    imageFloder.setFirstImagePath(mImgs.get(0));
                }
            }
            mImageFloders.add(0, imageFloder);
            // 为View绑定数据
            data2View();
            // 初始化展示文件夹的popupWindw
            initListDirPopupWindw();
        }
    };
    private ImageView tvTakephoto;
    private static final int REQUEST_TAKE_PHOTO = 0x123;
    private String photoName;
    private String outputDir;  //拍照输出目录


    /**
     * 仅选择全部时调用
     * 为View绑定数据
     */
    private void data2View() {
        if (mImgs == null || mImgs.size() == 0) {
            Toast.makeText(getApplicationContext(), "没有扫描到图片哦", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
         */

        mAdapter = new MyAdapter(this, mImgs,
                R.layout.photo_grid_item, "", currentType, imageList, flag);
        mGirdView.setAdapter(mAdapter);
        mImageCount.setText(totalCount + "张");
    }

    /**
     * 初始化展示文件夹的popupWindw
     */
    private void initListDirPopupWindw() {
        mListImageDirPopupWindow = new ListImageDirPopupWindow(
                LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
                mImageFloders, LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.photo_list_dir, null));

        mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
                mChooseDir.setSelected(false);
            }
        });
        // 设置选择文件夹的回调
        mListImageDirPopupWindow.setOnImageDirSelected(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_folder);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;

        initView();
        getImages();
        initEvent();
    }

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
     */
    private void getImages() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }
        // 显示进度条
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new MyThread(this).start();
    }

    private String flag = "";

    /**
     * 初始化View
     */
    private void initView() {
        flag = getIntent().getStringExtra("flag");
        LogUtil.logDebug(TAG, "--->flag = " + flag);

        View btn_left = findViewById(R.id.top_bar_left);
        TextView tv_title = (TextView) findViewById(R.id.top_bar_title);
        TextView tv_sure = (TextView) findViewById(R.id.top_bar_right);
        tv_sure.setVisibility(View.VISIBLE);
        tv_sure.setText("完成");
        tvTakephoto = (ImageView) findViewById(R.id.iv_imgchooser_takephoto);
        tvTakephoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                outputDir = getIntent().getStringExtra(ImageChooserHelper.SAVE_DIR);
                if (outputDir == null || "".equals(outputDir)) {
                    ToastUtil.showToastSHORT("未指定图片保存目录");
                    finish();
                    return;
                } else {
                    File dirFile = new File(outputDir);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                }
                //调用系统相机拍照
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoName = outputDir + "/" + DateFormat.format("yyyyMMddhhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                File file = new File(outputDir);
                if (!file.exists()) {
                    file.mkdirs(); // 创建文件夹
                }


                try {

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", new File(photoName));
                    } else {
                        uri = Uri.fromFile(new File(photoName));
                    }

                    LogUtil.logError("uri = ", uri.toString());

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);

                } catch (Exception e) {
                    LogUtil.logError("uri e = ", e.getMessage());
                }

//                try {
//
//                    Uri imageUri = Uri.fromFile(new File(photoName));
//                    LogUtil.logDebug("imageUri", imageUri.toString());
//
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
//
//                } catch (Exception e) {
//                    LogUtil.logError("imageUri e = ", e.getMessage());
//                }

            }
        });
        //头部显示
        btn_left.setVisibility(View.VISIBLE);
        tv_title.setText("图片选择");
        btn_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentType = getIntent().getIntExtra(ImageChooserHelper.CHOOSE_TYPE, -1);
        int max = getIntent().getIntExtra(ImageChooserHelper.MAX, 100);
        int size = getIntent().getIntExtra("size", 0);
//        ToastUtil.showToastSHORT("size = " + size);
        ImageChooserHelper.setMax(max - size);
        if (currentType == -1) {
            ToastUtil.showToastSHORT("选择类型错误");
            return;
        }
        if (currentType == ImageChooserHelper.TYPE_MULTY) {
            tv_sure.setVisibility(View.VISIBLE);
            tv_sure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageList.size() == 0) {
                        ToastUtil.showToastSHORT("还没有选择图片！");
                    } else {
                        setResult(RESULT_OK, new Intent()
                                .putExtra(ImageChooserHelper.CHOOSE_IMAGES, StringUtil.getListString(imageList))
                        );
                        finish();
                    }
                }
            });
        }
        mGirdView = (GridView) findViewById(R.id.id_gridView);
        mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
        mImageCount = (TextView) findViewById(R.id.id_total_count);
        mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);
    }


    private void initEvent() {
        /**
         * 为底部的布局设置点击事件，弹出popupWindow
         */
        mBottomLy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListImageDirPopupWindow
                        .setAnimationStyle(R.style.anim_popup_dir);
                mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

                mChooseDir.setSelected(true);
				/*// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = .3f;
				getWindow().setAttributes(lp);*/
            }
        });
    }

    @Override
    public void selected(ImageFolder folder) {
        if ("/全部图片".equals(folder.getDir())) {
            tvTakephoto.setVisibility(View.VISIBLE);
            mImageCount.setVisibility(View.GONE);
            data2View();
        } else {
            tvTakephoto.setVisibility(View.GONE);
            mImageCount.setVisibility(View.VISIBLE);
            mImgDir = new File(folder.getDir());
            List<String> mImgsTemp = Arrays.asList(mImgDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(".jpg") || filename.endsWith(".png")
                            || filename.endsWith(".jpeg"))
                        return true;
                    return false;
                }
            }));
            Collections.reverse(mImgsTemp);
            /**
             * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
             */
            mAdapter = new MyAdapter(this, mImgsTemp,
                    R.layout.photo_grid_item, mImgDir.getAbsolutePath(), currentType, imageList, flag);
            mGirdView.setAdapter(mAdapter);
        }
        mImageCount.setText(folder.getCount() + "张");
        mChooseDir.setText(folder.getName());
        mListImageDirPopupWindow.dismiss();
    }

    /**
     * 防泄漏
     */
    static class MyThread extends Thread {
        private WeakReference<Object> reference;

        public MyThread(Object object) {
            reference = new WeakReference<Object>(object);
        }

        @Override
        public void run() {
            ImageFolderActivity activity = (ImageFolderActivity) reference.get();
            if (activity == null) return;
            try {
                String firstImage = null;


                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = activity
                        .getContentResolver();

                // 只查询jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");

                Log.e(TAG, mCursor.getCount() + "");
                activity.mImgs = new ArrayList<>();
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    Log.e(TAG, path);
                    // 拿到第一张图片的路径
                    if (firstImage == null)
                        firstImage = path;
                    // 获取该图片的父路径名
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null || !parentFile.exists())
                        continue;
                    activity.mImgs.add(path);
                    String dirPath = parentFile.getAbsolutePath();
                    ImageFolder imageFloder = null;
                    // 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
                    if (activity.mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        activity.mDirPaths.add(dirPath);
                        // 初始化imageFloder
                        imageFloder = new ImageFolder();
                        imageFloder.setDir(dirPath);
                        imageFloder.setFirstImagePath(path);
                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if (filename.endsWith(".jpg")
                                    || filename.endsWith(".png")
                                    || filename.endsWith(".jpeg"))
                                return true;
                            return false;
                        }
                    }).length;
                    activity.totalCount += picSize;

                    imageFloder.setCount(picSize);
                    activity.mImageFloders.add(imageFloder);

                    if (picSize > activity.mPicsSize) {
                        activity.mPicsSize = picSize;
                        activity.mImgDir = parentFile;
                    }
                }
                mCursor.close();
                // 扫描完成，辅助的HashSet也就可以释放内存了
                activity.mDirPaths = null;
                // 通知Handler扫描图片完成
                activity.mHandler.sendEmptyMessage(0x110);
            } catch (Exception e) {
                Log.e(TAG, "queryPhotos error:" + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                //将照片添加
                mImgs.add(0, photoName);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter = new MyAdapter(this, mImgs,
                            R.layout.photo_grid_item, "", currentType, imageList, flag);
                    mGirdView.setAdapter(mAdapter);

                    mImageCount.setText(mImgs.size() + "张");
                }
                ImageFolder imageFolder = mImageFloders.get(0);
                if ("/全部图片".equals(imageFolder.getDir())) {
                    imageFolder.setFirstImagePath(photoName);
                }
                mListImageDirPopupWindow.notifyDataChanged();
                MediaUtil.checkMedia(this, photoName);
                return;
            }
        }
    }
}
