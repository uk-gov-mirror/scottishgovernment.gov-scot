package org.hippoecm.hst.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtils {
    public static String[] FULLY_QUALIFIED_URL_PREFIXES = new String[]{"//", "http:", "https:"};
    private static final Logger log = LoggerFactory.getLogger(PathUtils.class);
    private static final String HTML_SUFFIX = ".html";
    private static final String SLASH_ENCODED = "__slash__";

    private PathUtils() {
    }

    public static String normalizePath(String path) {
        if(path == null) {
            return null;
        } else {
            while(path.startsWith("/")) {
                path = path.substring(1);
            }

//            while(path.endsWith("/")) {
//                path = path.substring(0, path.length() - 1);
//            }

            return path;
        }
    }
}
