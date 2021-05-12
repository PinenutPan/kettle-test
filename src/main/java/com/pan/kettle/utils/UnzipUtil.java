package com.pan.kettle.utils;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.NativeStorage;
import de.innosystec.unrar.rarfile.FileHeader;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipUtil {

    private static Logger logger = LoggerFactory.getLogger(UnzipUtil.class);


    public static List<File> unZip(String sourceFile, String outputDir) throws IOException {
        ZipFile zipFile = null;
        List<File> files = new ArrayList<>();
        File file = new File(sourceFile);
        try {
            Charset CP866 = Charset.forName("CP866");
            zipFile = new ZipFile(file, CP866);
            Enumeration<?> enums = zipFile.entries();
            while (enums.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enums.nextElement();
                logger.info("解压：" + entry.getName());
                if (entry.isDirectory()) {
                    logger.info("目录：" + entry.getName());
                    continue;
                }
                File tmpFile = new File(outputDir + File.separator + entry.getName());
                if (!tmpFile.getParentFile().exists()) {
                    tmpFile.getParentFile().mkdirs();
                }
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = zipFile.getInputStream(entry);
                    out = new FileOutputStream(tmpFile);
                    int length = 0;
                    byte[] b = new byte[2048];
                    while ((length = in.read(b)) != -1) {
                        out.write(b, 0, length);
                    }
                    files.add(tmpFile);
                } catch (IOException ex) {
                    throw ex;
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("解压缩文件出现异常", e);
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ex) {
                throw new IOException("解压缩文件出现异常", ex);
            }
        }
        return files;
    }

    public static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        if (subDir != null && !subDir.trim().equals("")) {
            file = new File(outputDir + "/" + subDir);
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }
    }

    public static void unRar(String sourceFile, String outputDir) throws Exception {
        Archive archive = null;
        FileOutputStream fos = null;
        File file = new File(sourceFile);
        try {
            archive = new Archive(new NativeStorage(file));
            FileHeader fh = archive.nextFileHeader();
            int count = 0;
            File destFileName = null;
            while (fh != null) {
                logger.info(++count + ") " + fh.getFileNameString());
                String compressFileName = fh.getFileNameString().trim();
                destFileName = new File(outputDir + "/" + compressFileName);
                if (fh.isDirectory()) {
                    if (!destFileName.exists()) {
                        destFileName.mkdirs();
                    }
                    fh = archive.nextFileHeader();
                    continue;
                }
                if (!destFileName.getParentFile().exists()) {
                    destFileName.getParentFile().mkdirs();
                }
                fos = new FileOutputStream(destFileName);
                archive.extractFile(fh, fos);
                fos.close();
                fos = null;
                fh = archive.nextFileHeader();
            }
            archive.close();
            archive = null;
        } catch (Exception e) {
            throw e;
        } finally {
            if (fos != null)
                try {
                    fos.close();
                    fos = null;
                } catch (Exception exception) {
                }
            if (archive != null)
                try {
                    archive.close();
                    archive = null;
                } catch (Exception exception) {
                }
        }
    }

    public static void unGz(String sourceFile, String outputDir) {
        try {
            FileInputStream fin = new FileInputStream(sourceFile);
            GZIPInputStream gzin = new GZIPInputStream(fin);
            File file = new File(sourceFile);
            String fileName = file.getName();
            outputDir = outputDir + "/" + fileName.substring(0, fileName.lastIndexOf('.'));
            FileOutputStream fout = new FileOutputStream(outputDir);
            byte[] buf = new byte[1024];
            int num;
            while ((num = gzin.read(buf, 0, buf.length)) != -1) {
                fout.write(buf, 0, num);
            }
            gzin.close();
            fout.close();
            fin.close();
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    public static void unTarGz(String sourceFile, String outputDir) throws IOException {
        TarInputStream tarIn = null;
        File file = new File(sourceFile);
        try {
            tarIn = new TarInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))), 2048);
            createDirectory(outputDir, null);
            TarEntry entry = null;
            while ((entry = tarIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    entry.getName();
                    createDirectory(outputDir, entry.getName());
                    continue;
                }
                File tmpFile = new File(outputDir + "/" + entry.getName());
                createDirectory(tmpFile.getParent() + "/", null);
                OutputStream out = null;
                try {
                    out = new FileOutputStream(tmpFile);
                    int length = 0;
                    byte[] b = new byte[2048];
                    while ((length = tarIn.read(b)) != -1)
                        out.write(b, 0, length);
                } catch (IOException ex) {
                    throw ex;
                } finally {
                    if (out != null)
                        out.close();
                }
            }
        } catch (IOException ex) {
            throw new IOException("", ex);
        } finally {
            try {
                if (tarIn != null) {
                    tarIn.close();
                }
            } catch (IOException ex) {
                throw new IOException("关闭zipFile出现异常", ex);
            }
        }
    }

    public static List listFilesZip(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles((FileFilter) new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            logger.info(file.getName());
            filelist.add(file.getName());
        }
        return filelist;
    }

    public static List listFilesEnd(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            logger.info(file.getName());
            filelist.add(file.getName());
        }
        return filelist;
    }

    public static List listFilesZipEqual(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles((FileFilter) new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            filelist.add(file.getName());
            logger.info(file.getName());
        }
        return filelist;
    }

    public static void copyFileUsingFileStreams(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1)
                output.write(buf, 0, bytesRead);
        } finally {
            input.close();
            output.close();
        }
    }

    public static void rename(File oldFile, File newFile) throws Exception {
        if (!oldFile.exists()) {
            oldFile.createNewFile();
        }
        logger.info("修改前文件名称是：" + oldFile.getName());
        String rootPath = oldFile.getParent();
        logger.info("根路径是：" + rootPath);

        logger.info("修改后文件名称是：" + newFile.getName());
        if (oldFile.renameTo(newFile)) {
            logger.info("修改成功!");
        } else {
            logger.info("修改失败");
        }
    }

    public static void renameFile(String oldFileName, String newFileName) {
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }
    }

    public static List listFilesEndWith(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles((FileFilter) new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            filelist.add(file.getName());
            logger.info(file.getName());
        }
        return filelist;
    }

    public static List listFilesEndWithZip(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles((FileFilter) new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            filelist.add(path + "/" + file.getName());
            logger.info(file.getName());
        }
        return filelist;
    }

    public static List listFilesStartWithZip(String path, String rex) {
        List<String> filelist = new ArrayList();
        File dir = new File(path);
        final String pattern = rex;
        File[] files = dir.listFiles((FileFilter) new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(pattern, pathname.getName());
            }
        });
        for (File file : files) {
            filelist.add(file.getName());
            logger.info(file.getName());
        }
        return filelist;
    }

    public static void main(String[] args) throws Exception {
        String zipPath = "D:\\mk\\zipTest.zip";
        String outputDir = "D:\\mk\\";

        unZip(zipPath, outputDir);
    }
}
