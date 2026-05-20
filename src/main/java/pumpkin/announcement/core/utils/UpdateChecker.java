package pumpkin.announcement.core.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    private final String currentVersion;
    private final String projectSlug;

    public UpdateChecker(String currentVersion, String projectSlug) {
        this.currentVersion = currentVersion;
        this.projectSlug = projectSlug;
    }

    public CompletableFuture<String[]> check() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                conn.setRequestProperty("User-Agent", "Pumpkingz/PumpkinAnnouncer/" + currentVersion);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    String json = response.toString();

                    Matcher versionMatcher = Pattern.compile("\"version_number\":\"([^\"]+)\"").matcher(json);
                    Matcher nameMatcher = Pattern.compile("\"name\":\"([^\"]+)\"").matcher(json);

                    if (versionMatcher.find() && nameMatcher.find()) {
                        String latestVersion = versionMatcher.group(1);
                        String releaseName = nameMatcher.group(1);

                        String cleanCurrent = currentVersion.toLowerCase().replace("v", "").trim();
                        String cleanLatest = latestVersion.toLowerCase().replace("v", "").trim();

                        if (!cleanCurrent.equalsIgnoreCase(cleanLatest)) {
                            return new String[]{latestVersion, releaseName};
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
            return null;
        });
    }
}
