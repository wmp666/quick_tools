package com.wmp.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {
    
    public static String post(String urlString, Map<String, Object> params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        StringBuilder paramBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (paramBuilder.length() > 0) {
                paramBuilder.append("&");
            }
            paramBuilder.append(entry.getKey())
                    .append("=")
                    .append(java.net.URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8.name()));
        }
        
        byte[] postData = paramBuilder.toString().getBytes(StandardCharsets.UTF_8);
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData);
            os.flush();
        }
        
        int responseCode = connection.getResponseCode();
        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        return response.toString();
    }
}
