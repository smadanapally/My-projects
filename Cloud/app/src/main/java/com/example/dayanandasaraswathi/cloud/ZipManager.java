package com.example.dayanandasaraswathi.cloud;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by DayanandaSaraswathi on 12/8/2015.
 */
public class ZipManager {
    private static final int BUFFER = 2048;
    private static String createZipFileName(String fileName)
    {
        return fileName+".zip";
    }

    public static String zip(String fileName) {
        if(fileName!=null && fileName.contains(".zip")){
            return fileName;
        }
        BufferedInputStream origin = null;
        String zipFileName = null;
        try {
            zipFileName = createZipFileName(fileName);
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];
            Log.v("File Compression", "Filename: " + fileName);
            FileInputStream fi = new FileInputStream(fileName);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();
            return zipFileName;
        } catch (Exception e) {
            Log.e("File Compression", "Zip File", e);
            e.printStackTrace();
        }
        return zipFileName;
    }

    public static void unzip(String _zipFile, String _targetLocation) {
        try {
            FileInputStream fin = new FileInputStream(_targetLocation+_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                byte data[] = new byte[BUFFER];
                int c;
                while ((c = zin.read(data, 0, BUFFER)) != -1) {
                    fout.write(data, 0, c);
                }

                zin.closeEntry();
                fout.close();
            }
            zin.close();
        } catch (Exception e) {
            Log.e("File Decompression", "UnZip File", e);
        }
    }
}
