package com.apicloud.common;

import java.io.File;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * 
 * ClassName: FrameConfigure <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2014年11月20日 下午5:59:37 <br/>
 * 框架参数配置
 * 
 * @author chenhao
 * @version
 * @since JDK 1.6
 */
public class FrameConfigure {
    /**
     * 总目录
     */
    @SuppressLint("SdCardPath")
    public static final String MAIN_FOLDER = "/mnt/sdcard/kalai/";

    /**
     * 正常文件目录
     */
    public static final String NORMAL_DRC = MAIN_FOLDER + "normal/";
    /** 图片路径 */
    public static final String NORMAL_IMG_DRC = NORMAL_DRC + "Image/";
    /** APK路径 */
    public static final String NORMAL_APK_DRC = NORMAL_DRC + "apk/";
    /** 用户图片路径 */
    public static final String NORMAL_USER_DRC = NORMAL_DRC + "userico/";

    /** 删除重复文件 */
    public static void deleteSDCardFolder(File dir) {
        File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(to);
        if (to.isDirectory()) {
            String[] children = to.list();
            for (int i = 0; i < children.length; i++) {
                File temp = new File(to, children[i]);
                if (temp.isDirectory()) {
                    deleteSDCardFolder(temp);
                } else {
                    boolean b = temp.delete();
                    if (b == false) {
                        Log.d("deleteSDCardFolder", "DELETE FAIL");
                    }
                }
            }
            to.delete();
        }
    }
}
