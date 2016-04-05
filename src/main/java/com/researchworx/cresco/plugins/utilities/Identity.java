package com.researchworx.cresco.plugins.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Identity {

    public static String getPluginName(Class impClass, Clogger logger) {
        String name = "unknown";
        try {
            String jarFile = impClass.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(jarFile.substring(5, (jarFile.length() - 2)));
            FileInputStream fis = new FileInputStream(file);
            @SuppressWarnings("resource")
            JarInputStream jarStream = new JarInputStream(fis);
            Manifest mf = jarStream.getManifest();

            Attributes mainAttribs = mf.getMainAttributes();
            name = mainAttribs.getValue("artifactId");
        } catch (Exception ex) {
            logger.error("Unable to determine Plugin artifactId: {}", ex.getMessage());
        }
        return name;
    }

    public static String getPluginVersion(Class impClass, Clogger logger) {
        String version = "unknown";
        try {
            String jarFile = impClass.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(jarFile.substring(5, (jarFile.length() - 2)));
            FileInputStream fis = new FileInputStream(file);
            @SuppressWarnings("resource")
            JarInputStream jarStream = new JarInputStream(fis);
            Manifest mf = jarStream.getManifest();

            Attributes mainAttribs = mf.getMainAttributes();
            version = mainAttribs.getValue("Implementation-Version");
        } catch (Exception ex) {
            logger.error("Unable to determine Plugin Implementation-Version: {}", ex.getMessage());
        }
        return version;
    }
}
