package com.google.sherlockhash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

public enum SherlockHash {

    INSTANCE;

    private Map<String, BinaryPack> mapPerBinary = new TreeMap<>();

    private static class BinaryPack {
        private final long timeStamp;
        private Map<String, File> map = new TreeMap<>();

        public BinaryPack(long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }

    public File getFileFromZipStream(File binaryFile, ZipInputStream zipInputStream, String fName,
                                     String ext) throws IOException {
        BinaryPack pack = mapPerBinary.get(binaryFile.getCanonicalPath());
        if (pack == null || pack.timeStamp != binaryFile.lastModified()) {
            pack = new BinaryPack(binaryFile.lastModified());
            mapPerBinary.put(binaryFile.getCanonicalPath(), pack);
        }

        String innerFileName = fName + ext;
        File file = pack.map.get(innerFileName);

        if (file != null) {
            return file;
        }

        file = File.createTempFile(fName, ext);
        file.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = zipInputStream.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            pack.map.put(innerFileName, file);
        }

        return file;
    }
}
