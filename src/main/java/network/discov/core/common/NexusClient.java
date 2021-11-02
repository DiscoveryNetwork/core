package network.discov.core.common;

import network.discov.core.common.exception.ArtifactNotFoundException;
import network.discov.core.common.exception.InvalidResponseCodeException;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class NexusClient {
    public static @NotNull String getLatestVersion(String repository, String artifact, String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        String downloadUrl = getDownloadUrl(repository, artifact, auth);
        String[] fragments = downloadUrl.split("/");
        return fragments[fragments.length - 1];
    }

    public static void downloadFile(String path, String repository, String artifact, String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        String downloadUrl = getDownloadUrl(repository, artifact, auth);
        String[] fragments = downloadUrl.split("/");
        String latestVersion = fragments[fragments.length - 1];

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + new String(auth.getBytes()));
        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fileOutput = new FileOutputStream(String.format("%s/%s", path, latestVersion));

        byte[] data = new byte[1024];
        int byteContent ;
        while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
            fileOutput.write(data, 0, byteContent);
        }
        fileOutput.close();
    }

    private static @NotNull String getDownloadUrl(String repository, String artifact, @NotNull String auth) throws IOException, ParseException, ArtifactNotFoundException, InvalidResponseCodeException {
        URL url = getNexusUrl(repository, artifact);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuth));
        connection.connect();

        if (connection.getResponseCode() == 200) {
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(inline.toString());
            JSONArray array = (JSONArray) json.get("items");

            if (array.size() > 0) {
                JSONObject result = (JSONObject) array.get(0);
                return result.get("downloadUrl").toString();
            }

            throw new ArtifactNotFoundException(String.format("Artifact [%s] was not found on nexus in repo %s", artifact, repository));
        }

        throw new InvalidResponseCodeException(String.format("Nexus responded with an unexpected HTTP code %s while fetching artifact [%s]", connection.getResponseCode(), artifact));
    }

    private static @NotNull URL getNexusUrl(String repository, String artifact) throws MalformedURLException {
        String base = "https://nexus.discov.network/service/rest/v1/search/assets";
        String url = String.format("%s?sort=version&repository=%s&name=%s", base, repository, artifact);
        return new URL(url);
    }
}
