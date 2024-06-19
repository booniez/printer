package com.ccwcar.printer.utils;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;

public class DownloadUtil {

    public static InputStream download(final String networkUrl) throws Exception {
        URL url = new URL(networkUrl);
        return new DataInputStream(url.openStream());
    }
}
