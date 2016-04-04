package com.google.sherlockhash;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

    private static File readAPK(File apkFile) {
        File file = new File("classes.dex");
        ZipInputStream zipInputStream;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry zipEntry;

            while (true) {
                zipEntry = zipInputStream.getNextEntry();

                if (zipEntry == null) {
                    break;
                }

                if (zipEntry.getName().endsWith(".dex")) {
                    String fName = "classes";
                    String ext = "dex";

                    file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, zipInputStream, fName, ext);
                    //System.out.println(file.getName());
                }

                if (zipEntry.getName().endsWith("jar") || zipEntry.getName().endsWith("zip")) {
                    String fName = "inner_zip";
                    String ext = "zip";

                    File innerZip = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, zipInputStream, fName, ext);
                    //System.out.println(innerZip.getAbsolutePath());

                    ZipInputStream fromInnerZip = new ZipInputStream(new FileInputStream(
                            innerZip));
                    ZipEntry innerZipEntry;

                    while (true) {
                        innerZipEntry = fromInnerZip.getNextEntry();
                        if (innerZipEntry == null) {
                            break;
                        }

                        if (innerZipEntry.getName().endsWith(".dex")) {
                            fName = "inner_zip";
                            ext = "dex";
                            file = SherlockHash.INSTANCE.getFileFromZipStream(apkFile, zipInputStream, fName, ext);
                            //System.out.println(file.getAbsolutePath());
                        }
                    }
                }
            }
            zipInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void main(String[] args) throws Exception {
        String testFilePath = System.getProperty("user.home") +
                "/Desktop/Scenarios/3 APKs/com.google.samples.apps.iosched-555.apk";

        File testFile = new File(testFilePath);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            readAPK(testFile);
        }

        long end = System.currentTimeMillis();
        System.out.println("*** time " + (end - start));

        // 2 inners
        // SH extra cost 17329
        // NO SH extra cost 20168

        // 1 inner
        // SH extra cost 4170
        // NO SH extra cost 5763
    }
}
