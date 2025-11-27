import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Maven Wrapper Downloader companion.
 * This class is used by the existing mvnw script (legacy Takari flow) when neither curl nor wget are available.
 * It reads .mvn/wrapper/maven-wrapper.properties (wrapperUrl, distributionUrl) and downloads the wrapper JAR
 * to .mvn/wrapper/maven-wrapper.jar.
 */
public class MavenWrapperDownloader {

    private static final String PROPERTIES_RELATIVE = ".mvn/wrapper/maven-wrapper.properties";
    private static final String WRAPPER_JAR_RELATIVE = ".mvn/wrapper/maven-wrapper.jar";
    private static final String DEFAULT_WRAPPER_JAR_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar";

    /**
     * Entry point used by mvnw script: it passes MAVEN_PROJECTBASEDIR as the first argument.
     * @param args args[0] should be the project base directory (MAVEN_PROJECTBASEDIR)
     */
    public static void main(String[] args) {
        String baseDir = args != null && args.length > 0 ? args[0] : ".";
        Path propertiesPath = Paths.get(baseDir, PROPERTIES_RELATIVE);
        Path wrapperJarPath = Paths.get(baseDir, WRAPPER_JAR_RELATIVE);

        try {
            Files.createDirectories(wrapperJarPath.getParent());
            String wrapperUrl = loadWrapperUrl(propertiesPath);
            download(wrapperUrl, wrapperJarPath);
            System.out.println("Downloaded Maven Wrapper JAR to: " + wrapperJarPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to download Maven Wrapper: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String loadWrapperUrl(Path propertiesPath) throws IOException {
        String url = DEFAULT_WRAPPER_JAR_URL;
        if (Files.exists(propertiesPath)) {
            Properties props = new Properties();
            try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(propertiesPath))) {
                props.load(in);
            }
            url = props.getProperty("wrapperUrl", url).trim();
        }
        return url;
    }

    private static void download(String url, Path dest) throws IOException {
        System.out.println("Downloading from: " + url);
        URL u = new URL(url);
        try (ReadableByteChannel rbc = Channels.newChannel(u.openStream());
             FileOutputStream fos = new FileOutputStream(dest.toFile())) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }
}
