package com.peykasa.audit.auditserver;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */

public class ConfigUtil {
    private static List<String> origContent;
    private static String name = "application.properties";
    private static File origFile;

    static {
        URL configFileUrl = ConfigUtil.class.getClassLoader().getResource(name);
        URI uri;
        try {
            assert configFileUrl != null;
            uri = configFileUrl.toURI();
            origFile = new File(uri);
            origContent = read(origFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> read(File file) throws IOException {
        FileInputStream fStream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in), 8192);
        String strLine;
        List<String> list = new ArrayList<>();
        while ((strLine = br.readLine()) != null) {
            list.add(strLine);
        }
        br.close();
        in.close();
        fStream.close();
        return list;
    }

    public static Properties change(String source, ConfigEntry... props) throws IOException {
        Properties properties = new Properties();
        properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream(name));
        for (ConfigEntry prop : props) {
            properties.setProperty(prop.getKey(), prop.getValue());
        }
        properties.store(new FileOutputStream(new File(origFile.toURI())), source + " : rewrite at " + new Date());
        return properties;
    }

    public static void reset() throws URISyntaxException, IOException {
        URL configFileUrl = ConfigUtil.class.getClassLoader().getResource(name);
        assert configFileUrl != null;
        URI uri = configFileUrl.toURI();
        File origFile = new File(uri);
        write(origContent, origFile);
    }

    private static void write(List<String> lines, File file) throws IOException {
        FileOutputStream fStream = new FileOutputStream(file);
        DataOutputStream in = new DataOutputStream(fStream);
        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(in), 8192);
        br.write("#" + new Date() + "\n");
        for (String line : lines) {
            br.write(line + "\n");
        }
        br.close();
        in.close();
        fStream.close();
    }

    public static void print(String section) throws IOException {
        System.err.println(section + "============================================ ");
        Properties properties = new Properties();
        properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream("application.properties"));
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.err.println(entry.getKey() + "=" + entry.getValue());
        }
    }

}
