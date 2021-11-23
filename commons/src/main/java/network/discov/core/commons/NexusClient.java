package network.discov.core.commons;

import network.discov.core.commons.exception.ArtifactNotFoundException;
import network.discov.core.commons.exception.InvalidResponseCodeException;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NexusClient {
    public static @NotNull String getLatestVersion(String repository, String artifact, String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        String downloadUrl = getDownloadUrl(repository, artifact, auth);
        String[] fragments = downloadUrl.split("/");
        return fragments[fragments.length - 1];
    }

    public static String downloadFile(String path, String repository, String artifact, String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        String downloadUrl = getDownloadUrl(repository, artifact, auth);
        String[] fragments = downloadUrl.split("/");
        String latestVersion = fragments[fragments.length - 1];

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + auth);
        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());

        String filePath = String.format("%s/%s", path, latestVersion);
        FileOutputStream fileOutput = new FileOutputStream(filePath);

        byte[] data = new byte[1024];
        int byteContent ;
        while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
            fileOutput.write(data, 0, byteContent);
        }
        fileOutput.close();
        connection.disconnect();
        return filePath;
    }

    public static List<String> getAvailableComponents(String repository, String auth) throws IOException, ParseException, InvalidResponseCodeException {
        String urlString = String.format("https://nexus.discov.network/service/rest/v1/components?repository=%s", repository);
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + auth);
        connection.connect();

        if (connection.getResponseCode() == 200) {
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            connection.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(inline.toString());
            JSONArray items = (JSONArray) json.get("items");

            List<String> components = new ArrayList<>();
            for (Object item : items) {
                JSONObject component = (JSONObject) item;
                components.add((String) component.get("name"));
            }

            return components;
        }

        connection.disconnect();
        throw new InvalidResponseCodeException(String.format("Nexus responded with an unexpected HTTP code %s while fetching components", connection.getResponseCode()));
    }

    private static @NotNull String getDownloadUrl(String repository, String artifact, @NotNull String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        String base = "https://nexus.discov.network/service/rest/v1/search/assets";
        String urlString = String.format("%s?sort=version&repository=%s&name=%s", base, repository, artifact);
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + auth);
        connection.connect();

        if (connection.getResponseCode() == 200) {
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            connection.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(inline.toString());
            JSONArray array = (JSONArray) json.get("items");

            if (array.size() > 0) {
                JSONObject result = (JSONObject) array.get(0);
                return result.get("downloadUrl").toString();
            }

            throw new ArtifactNotFoundException(String.format("Artifact [%s] was not found on nexus in repo %s", artifact, repository));
        }

        connection.disconnect();
        throw new InvalidResponseCodeException(String.format("Nexus responded with an unexpected HTTP code %s while fetching artifact [%s]", connection.getResponseCode(), artifact));
    }
}
