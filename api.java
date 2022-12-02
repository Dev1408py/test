package com.ASR_JAVA;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class api {
    public String get_transcript(String api_key,String lang) throws Exception{
        String secret_key = api_key;
        HttpURLConnection conn;

        // endpoint and options to start a transcription task
        URL endpoint = new URL("https://api.speechtext.ai/recognize?key=" + secret_key + "&language="+lang+"&punctuation=true&format=m4a");

        // loads the audio into memory
        File file = new File("C:/Users/devpa/downloads/Record (online-voice-recorder.com).mp3");
        RandomAccessFile f = new RandomAccessFile(file, "r");
        long sz = f.length();
        byte[] post_body = new byte[(int) sz];
        f.readFully(post_body);
        f.close();

        // send an audio transcription request
        conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/octet-stream");

        conn.setDoOutput(true);
        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(post_body);
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            String result = response.toString();
            JSONObject json = new JSONObject(result);
            // get the id of the speech recognition task
            String task = json.getString("id");
            System.out.println("Task ID: " + task);
            // endpoint to check status of the transcription task
            URL res_endpoint = new URL("https://api.speechtext.ai/results?key=" + secret_key + "&task=" + task + "&summary=true&summary_size=15&highlights=true&max_keywords=15");
            System.out.println("Get transcription results, summary, and highlights");
            // use a loop to check if the task is finished
            JSONObject results;
            while (true) {
                conn = (HttpURLConnection) res_endpoint.openConnection();
                conn.setRequestMethod("GET");
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = new StringBuffer();
                String res;
                while ((res = in.readLine()) != null) {
                    response.append(res);
                }
                in.close();
                results = new JSONObject(response.toString());
                System.out.println("Task status: " + results.getString("status"));
                if (results.getString("status").equals("failed")) {
                    System.out.println("Failed to transcribe!");
                    return "f";
                }
                if (results.getString("status").equals("finished")) {
                    String store = results.toString();
                    String ans = "";
                    int flag =0;
                    for(int i=0;i<store.length()-23;i++){
                        if((store.charAt(i) == 't' && store.charAt(i+1) == 'r' && store.charAt(i+2) == 'a' && store.charAt(i+3) == 'n') || flag == 1){
                            if(store.charAt(i) != '\'') {
                                ans = ans + store.charAt(i);
                            }
                            flag = 1;
                        }
                    }
                    return ans;
                }
                // sleep for 15 seconds if the task has the status - 'processing'
                TimeUnit.SECONDS.sleep(15);
            }

        }
        else {
            return "f";
        }
    }
}
