package com.kding.downloadlib;

import android.database.sqlite.SQLiteDatabase;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.List;

/**
 * 数据库管理类，使用LitePal框架，使用时需要让application继承LitePalApplication
 *
 */
public class DownloadDBManger<T extends DownloadItem> {
    private static DownloadDBManger instance;

    private SQLiteDatabase db;

    public static DownloadDBManger getInstance() {
        if (null == instance) {
            synchronized (DownloadDBManger.class) {
                if (null == instance) {
                    instance = new DownloadDBManger();
                }
            }
        }

        return instance;
    }

    private DownloadDBManger() {
        db = Connector.getDatabase();
    }

    boolean addDownloadItem(T item) {
        if (DownloadItem.DOWNLOAD_CANCEL == item.getDownloadStatus()) return false;
        LogUtil.e("addDownloadItem id =" + item.getId() +  ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus());
        return item.save();
    }

    boolean removeDownloadItem(T item) {
        boolean res = true;
        int delRes = item.delete();
        if (item.getId() != -1 && 1 > delRes)
            res = false;
        LogUtil.e("removeDownloadItem id =" + item.getId() + ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus() + ", delRes = " + delRes);
        if (!res) {
            for (int i = 0; i < 3; i++) {
                delRes = item.delete();
                LogUtil.e("removeDownloadItem FOR i = " + i + ", id =" + item.getId() + ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus() + ", delRes = " + delRes);
                if (delRes > 0) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    boolean updateDownloadItem(T item) {
        boolean res = true;
        if (item.update(item.getId()) == 0) {
            res = false;
        }
        return res;
    }

    public void updateDownloadList(List<T> list) {
        if (null == list) {
            return;
        }

        for (DownloadItem item : list) {
            if (-1 == item.getId()) {
                item.save();
            } else {
                item.update(item.getId());
            }
        }
    }

}
