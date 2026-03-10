/*
 * Minimal Maven Wrapper downloader fallback.
 *
 * This source file is included so teams can (optionally) compile/download the wrapper jar if it is missing.
 * In most setups, the wrapper jar is committed. If not, you can generate it via:
 *   mvn -N -q -DskipTests -Dmaven.wrapper.version=3.2.0 wrapper:wrapper
 *
 * Keeping this file aligns with common Maven Wrapper layouts and helps when bootstrapping.
 */
public class MavenWrapperDownloader {
  public static void main(String[] args) {
    System.err.println("Maven Wrapper jar missing. Please generate wrapper files using:");
    System.err.println("  mvn -N -q -DskipTests -Dmaven.wrapper.version=3.2.0 wrapper:wrapper");
    System.exit(1);
  }
}
