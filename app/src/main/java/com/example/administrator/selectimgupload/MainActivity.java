package com.example.administrator.selectimgupload;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * 选择一张或者多张图片 test
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "----->MainActivity";

    private final static int IMAGE_CHOOSER_REQUEST_CODE = 1001;                                      //选择图片

    private TextView tv_select_single;
    private TextView tv_select_more;

    private ImageView iv_img;

    private RecyclerView recycler_view;

    private String select_img_path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_select_single = findViewById(R.id.tv_select_single);
        tv_select_more = findViewById(R.id.tv_select_more);
        recycler_view = findViewById(R.id.recycler_view);
        iv_img = findViewById(R.id.iv_img);

        tv_select_single.setOnClickListener(this);
        tv_select_more.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_single:

                requestPermission("single", 1);

                break;
            case R.id.tv_select_more:

                requestPermission("more", 2);

                break;
        }
    }


    /**
     * 请求权限
     *
     * @param flag 无用
     * @param type 单张 或 多张
     */
    private void requestPermission(final String flag, final int type) {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception {
                        if (!aBoolean) {
                            ToastUtil.showToastLONG("请您先允许相机权限！");
                            return;
                        } else {
                            startTakePhoto(flag, type);
                        }
                    }
                });
    }

    protected void startTakePhoto(String flag, int type) {
        Intent intent = new Intent(this, ImageFolderActivity.class);
        intent.putExtra(ImageChooserHelper.CHOOSE_TYPE, type);
        intent.putExtra(ImageCropHelper.SAVE_DIR, BriefStoreConfig.DIR_IMAGE);
        intent.putExtra("flag", flag);
        startActivityForResult(intent, IMAGE_CHOOSER_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtil.logDebug(TAG, "requestCode = " + requestCode + " resultCode = " + resultCode + "  data = " + data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (data != null) {

            if (requestCode == IMAGE_CHOOSER_REQUEST_CODE) {

                select_img_path = data.getStringExtra(ImageChooserHelper.CHOOSE_IMAGES);
                LogUtil.logDebug(TAG, "图片选择成功 select_img_path = " + select_img_path);

                if (select_img_path != null && !TextUtils.isEmpty(select_img_path) && !select_img_path.contains(",")) {

                    Glide.with(this).load(select_img_path).asBitmap().dontAnimate().into(iv_img);

                } else if (select_img_path != null && !TextUtils.isEmpty(select_img_path) && select_img_path.contains(",")) {

                    String[] split = select_img_path.split(",");
                    if (split != null && split.length != 0) {

                        int length = split.length;

                        List<String> stringList = new ArrayList<>();

                        for (int i = 0; i < length; i++) {
                            String s = split[i];
                            stringList.add(s);
                        }

                        StaggeredGridLayoutManager recyclerViewLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                        ImgAdapter adapter = new ImgAdapter(stringList);
                        recycler_view.setLayoutManager(recyclerViewLayoutManager);
                        recycler_view.setAdapter(adapter);

                    }
                }
            }
        }
    }

    public class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.ViewHolder> {

        private List<String> list = new ArrayList<>();

        public ImgAdapter(List<String> lists) {
            this.list = lists;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(DemoApplication.getInstance()).inflate(R.layout.item_layout, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

            Glide.with(DemoApplication.getInstance()).load(list.get(position)).asBitmap().dontAnimate().into(viewHolder.iv_img);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView iv_img;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                iv_img = itemView.findViewById(R.id.iv_img);

            }
        }
    }

}
