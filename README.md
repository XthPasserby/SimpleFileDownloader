# SimpleFileDownloader [ ![Download](https://api.bintray.com/packages/xthpasserby/maven/SimpleFileDownloader/images/download.svg?version=0.0.1) ](https://bintray.com/xthpasserby/maven/SimpleFileDownloader/0.0.1/link)

Android平台基于OkHttp的简单文件下载工具

### 如何使用

配置gradle

```
// project的build.gradle文件中添加jcenter
repositories {
    jcenter()
}

// module的build.gradle文件中添加依赖
implementation 'com.xthpasserby.lib:SimpleFileDownloader:0.0.1'
```

```java
// 全局初始化 推荐在Application中初始化
SimpleDownloader.init(this);
if (BuildConfig.DEBUG) {
    SimpleDownloader.enableDebug()
}

// 简单使用
DownloadTask task = SimpleDownloader.getInstance()
    .url("http://download_url")
    .filePath("your_file_path")
    .fileName("your_file_name")
    // 设置对当前DownloadTask的监听，在主线程回调
    .setTaskStatusChangeLisener(new DownloadTask.ITaskStatusListener() {
        @Override
        public void onStatusChange(DownloadStatus status) {
            // 下载状态改变
        }

        @Override
        public void onProgress(int percentage) {
            // 下载进度变化
        }
    })
    // 创建DoanloadTask实例
    .buildTask();

// 添加对所有下载的监听，子线程回调不可刷新UI，若想在主线程回调可以替换成addDownloadListenerOnMainThread方法
SimpleDownloader.getInstance().addDownloadListener(new IDownloadListener() {
    @Override
    public void onStatusChange(DownloadTask task) {
        // 下载状态变化 
        // task.getDownloadStatus()获取下载状态
        // task.getSpeed()获取当前下载速度
    }

    @Override
    public void onProgress(DownloadTask task) {
        // 下载进度变化
        // task.getPercentage()获取下载进度百分比
    }

    @Override
    public void onStorageOverFlow() {
        // 存储不足
    }
});

// 获取所有下载任务
SimpleDownloader.getInstance().getAllTasks();

// 取消所有下载任务
SimpleDownloader.getInstance().clearAllTasks();

// DownloadTask方法
task.start(); // 开始下载
task.pause(); // 暂停下载
task.resume(); // 继续下载
task.cancel(); // 取消下载
```

// 配合SimpleDownloadButton使用

布局文件：

```xml
<com.xthpasserby.lib.simpledownloadbutton.MultiDownloadButton
        android:id="@+id/multi_download_button"
        android:layout_width="63dp"
        android:layout_height="63dp"
        android:gravity="center"
        android:textSize="15sp" />

<com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton
        android:id="@+id/progress_download_button"
        android:layout_width="80dp"
        android:layout_height="30dp"
        android:layout_marginTop="30dp"
        android:background="@drawable/rectangle_blue_solid_bg"
        android:gravity="center"
        android:maxLines="1"
        android:text="下载"
        android:textColor="#FFFFFF"
        app:max="100"
        app:progressDrawable="@drawable/rectangle_blue_solid_bg" />
```

调用代码： 

```java
SimpleDownloader.getInstance()
    .url("http://download_url")
    .filePath("your_file_path")
    .fileName("your_file_name")
    .setTaskStatusChangeLisener(SimpleDownloadButton)
    .buildTask();
```

**更多示例参考sample项目**