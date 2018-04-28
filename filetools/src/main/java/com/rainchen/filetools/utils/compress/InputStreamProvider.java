package com.rainchen.filetools.utils.compress;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通过此接口获取输入流，以兼容文件、FileProvider方式获取到的图片
 * Get the input stream through this interface, and obtain the picture using compatible files and FileProvider
 * Created by MrFeng on 2018/4/23.
 */
public interface InputStreamProvider {
  InputStream open() throws IOException;

  String getPath();

//  <T extends FileInfo> T getFileInfo();
}
