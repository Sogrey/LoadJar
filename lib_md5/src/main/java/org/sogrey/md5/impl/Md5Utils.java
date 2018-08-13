package org.sogrey.md5.impl;

import org.sogrey.jarinterface.Md5JarInterface;
import org.sogrey.md5.MD5;

/**
 * Created by Sogrey on 2018/8/13.
 */

public class Md5Utils implements Md5JarInterface {
    @Override
    public String getMd5(String content) {
        return MD5.MD5(content);
    }
}
