package com.google.sherlockhash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

public enum SherlockHash {

    INSTANCE;

    private Map<String, BinaryPack> mapPerBinary = new TreeMap<String, BinaryPack>();

    private static class BinaryPack {
        private final long timeStamp;
        private Map<String, File> map = new TreeMap();

        public BinaryPack(long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }

    public File getFileFromZipStream(File binaryFile, ZipInputStream zipInputStream, String fName,
                                     String ext) throws IOException {
        if (mapPerBinary.containsKey(binaryFile.getCanonicalPath())) {
            BinaryPack pack = mapPerBinary.get(binaryFile.getCanonicalPath());

            if(pack.timeStamp != binaryFile.lastModified()) {
                mapPerBinary.put(binaryFile.getCanonicalPath(),
                        new BinaryPack(binaryFile.lastModified()));
            }
        }
        else {
            mapPerBinary.put(binaryFile.getCanonicalPath(),
                    new BinaryPack(binaryFile.lastModified()));
        }

        String innerFileName = fName + ext;
        BinaryPack pack = mapPerBinary.get(binaryFile.getCanonicalPath());

        if (pack.map.containsKey(innerFileName)) {
            return pack.map.get(innerFileName);
        }

        File file = File.createTempFile(fName, ext);
        file.deleteOnExit();

        FileOutputStream fos =
                new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = zipInputStream.read(bytes)) >= 0) {
            fos.write(bytes, 0, length);
        }

        fos.close();

        pack.map.put(innerFileName, file);

        return file;
    }
}
