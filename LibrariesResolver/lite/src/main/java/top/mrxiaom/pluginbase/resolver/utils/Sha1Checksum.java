package top.mrxiaom.pluginbase.resolver.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Sha1Checksum {

    public static File getChecksumFile(File file) {
        File parent = file.getParentFile();
        if (parent != null) {
            return new File(parent, file.getName() + ".sha1");
        }
        return new File(file.getAbsolutePath() + ".sha1");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checksum(File file) {
        if (file.exists()) {
            File sha1File = getChecksumFile(file);
            if (!sha1File.exists()) return false;
            return Sha1Checksum.verify(file, sha1File);
        }
        return false;
    }

    public static String calculateFileSha1(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            try (FileInputStream is = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(is)
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            return null;
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String readSha1FromFile(File sha1File) {
        if (!sha1File.exists() || !sha1File.isFile()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(sha1File);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))
        ) {
            String sha1 = reader.readLine();
            if (sha1 == null) {
                return null;
            }
            return sha1.trim().toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean verify(File targetFile, File sha1File) {
        String calculatedSha1 = calculateFileSha1(targetFile);
        if (calculatedSha1 == null) return false;
        String expectedSha1 = readSha1FromFile(sha1File);
        if (expectedSha1 == null) return false;
        return calculatedSha1.equals(expectedSha1);
    }

}
