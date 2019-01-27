package com.example.administrator.selectimgupload;

import android.os.Environment;

/**
 * Description: ScreenUtil
 * Creator: lhd
 * date: $date $time
 */
public class BriefStoreConfig {

   public static boolean isBebug = true;
   public static String SP_FILE_NAME = "sp_briefStore";
   public static String SP_LOGIN_FILE = "sp_login_file";
   public static String SP_LONGIN_DEFAULT = "sp_login_default";
   public static String SP_KEY_LOGIN_DEFAULT_ACCOUNT = "sp_key_login_default_account";
   public static String SP_KEY_LOGIN_DEFAULT_PASSWORD = "sp_key_login_default_password";
   public static String SP_KEY_USERID = "sp_key_userid";

   public static String SP_KEY_TOKEN = "sp_key_token";

   public static final int LIMIT_NUM_HOMECAMP = 5;
   public static final int LIMIT_NUM_HOMEMENU = 5;


   public static final String DIR_ROOT= Environment.getExternalStorageDirectory()+"/eetogether/";
   public static final String DIR_IMAGE= DIR_ROOT + "/image/";
   public static final String AUDIO_RECORD= DIR_ROOT + "/audio/";
   public static final String DIR_IMAGE_COMPRESS= DIR_IMAGE + "/compress/";


}
