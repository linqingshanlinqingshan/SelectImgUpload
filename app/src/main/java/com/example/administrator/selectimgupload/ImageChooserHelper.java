package com.example.administrator.selectimgupload;

/**
 * Created by CD on 2016/8/6.
 */
public class ImageChooserHelper {
    public static final String CHOOSE_TYPE = "choose_type";
    public static final String CHOOSE_IMAGES = "choose_images";
    public static final String SAVE_DIR = "save_dir";   //保存目录
    public static final String MAX = "max";
    public static final int TYPE_SINGLE = 1;
    public static final int TYPE_MULTY = 2;
    public static int max = 100;   //默认很多张

    public static void setMax(int max) {
        ImageChooserHelper.max = max;
    }
}
