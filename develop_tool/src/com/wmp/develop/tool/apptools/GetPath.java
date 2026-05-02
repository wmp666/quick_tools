package com.wmp.develop.tool.apptools;

import java.io.File;
import java.net.URISyntaxException;

public class GetPath {

    public static final int APPLICATION_PATH = 1;
    public static final int SOURCE_FILE_PATH = 0;

    private static File getProgramDirectory() throws URISyntaxException {
        File file = new File(GetPath.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        return file.isFile()?file.getParentFile():file;
    }

    /**
     * 获取应用程序路径
     *
     * @param type 1: 应用程序路径 0: 源文件路径
     */
    public static String getAppPath(int type) throws URISyntaxException {
        if (type == APPLICATION_PATH){
            return getProgramDirectory().getParent();
        }else{
            return getProgramDirectory().toString();
        }
    }
}
