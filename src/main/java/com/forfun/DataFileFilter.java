package com.forfun;

import java.io.File;
import java.io.FileFilter;

public class DataFileFilter implements FileFilter{

    @Override
    public boolean accept(File pathname) {
        boolean result = true;
        if (pathname.getName().startsWith(".")) {
            result = false;
        }
        return result;
    }
    
}
