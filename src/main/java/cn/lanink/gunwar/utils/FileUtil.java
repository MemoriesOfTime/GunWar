package cn.lanink.gunwar.utils;

import cn.nukkit.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * @author lt_name
 */
public class FileUtil {

    public static boolean deleteFile(String file) {
        return deleteFile(new File(file));
    }

    public static boolean deleteFile(File deleteFile) {
        try {
            if (!deleteFile.exists()) {
                return true;
            }
            File[] files = deleteFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFile(file);
                    }else if (!file.delete()) {
                        throw new IOException("文件: " + file.getName() + " 删除失败！");
                    }
                }
            }
            if (!deleteFile.delete()) {
                throw new IOException("文件: " + deleteFile.getName() + " 删除失败！");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyDir(String from, String to) {
        return copyDir(new File(from), new File(to));
    }

    public static boolean copyDir(String from, File to) {
        return copyDir(new File(from), to);
    }

    public static boolean copyDir(File from, String to) {
        return copyDir(from, new File(to));
    }

    public static boolean copyDir(File from, File to) {
        try {
            File [] files = from.listFiles();
            if (files != null) {
                if (!to.exists() && !to.mkdirs()) {
                    throw new IOException("文件夹: " + to.getName() + " 创建失败！");
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        copyDir(file, new File(to, file.getName()));
                    }else {
                        Utils.copyFile(file, new File(to, file.getName()));
                    }
                }
                return true;
            }else {
                Utils.copyFile(from, to);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
