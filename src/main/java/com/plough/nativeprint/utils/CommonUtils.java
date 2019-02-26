package com.plough.nativeprint.utils;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by plough on 2019/2/25.
 */
public class CommonUtils {
    public static String pathJoin(String path1, String... paths) {
        return Paths.get(path1, paths).toString();
    }

    public static boolean makeSureFileExist(File file) throws IOException {
        if (file == null) {
            return false;
        } else if (file.exists()) {
            return true;
        } else {
            mkdirs(file.getParentFile());
            file.createNewFile();
            return true;
        }
    }

    public static boolean mkdirs(File file) {
        return file != null && (file.exists() || file.mkdirs());
    }

    public static String[] getSystemPrinterNameArray() {
        List<String> printerList = new ArrayList<>();
        PrintService[] psArr = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.AUTOSENSE, new HashPrintRequestAttributeSet());

        for(int i = 0; i < psArr.length; i++) {
            printerList.add(psArr[i].getName());
        }

        return printerList.toArray(new String[0]);
    }
}
