package com.example.administrator.selectimgupload;

import java.util.List;

/**
 * Created by longhengdong on 2018/7/30.
 */

public class StringUtil {
    /**
     * 拼接list，为string
     * @param list
     * @return
     */
    public static String getListString(List<String> list) {
        String file_url;
        StringBuilder sb = new StringBuilder();
        for(int i=0;i< list.size();i++){
            if(i!=0) sb.append(",");
            sb.append(list.get(i));
        }
        file_url = sb.toString();
        return file_url;
    }
}
