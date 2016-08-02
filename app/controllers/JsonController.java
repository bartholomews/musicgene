package controllers;

import com.wrapper.spotify.models.AudioFeature;
import com.wrapper.spotify.models.Track;
import model.music.Attribute;
import model.music.MusicCollection;
import model.music.Song;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import scala.Option;
import scala.Tuple1;
import scala.Tuple2;
import scala.concurrent.java8.FuturesConvertersImpl;

import java.io.*;
import java.util.*;

/**
 * @see // crunchify.com/how-to-write-json-object-to-file-in-java/
 */
public class JsonController {

    private final static String PATH = "/Users/bartholomews/Google_Drive/dev/Genetic-playlists/app/resources/";
    private final static String JSON = ".json";

    @SuppressWarnings("unchecked")
    public static void writeJSON(String id, String preview, Set<Attribute> attributes) throws IOException {
        File f = new File(PATH + id + ".json");
        if (!f.exists()) {
            String artist = "unknown_artist";
            String title = "unknown_title";
            System.out.println("WRITING JSON FILE...");
            JSONObject obj = new JSONObject();
            // ((List<Attribute>) s.attributes()).forEach(Attribute::value);
            JSONArray attr = new JSONArray();
            for (Attribute a : attributes) {
                String simpleName = a.getClass().getSimpleName();
                attr.add(simpleName + ": " + a.value());
                //      obj.put(a.getClass().toString(),);
            }
            obj.put("ID", id);
            obj.put("preview_url", preview);
            obj.put("attributes", attr);
            /*
            JSONArray company = new JSONArray();
            company.add("Compnay: eBay");
            company.add("Compnay: Paypal");
            company.add("Compnay: Google");
            obj.put("Company List", company);
            */
            // try-with-resources statement based on post comment below :)
            try (FileWriter file = new FileWriter(f)) {
                file.write(obj.toJSONString());
                System.out.println("Successfully Copied JSON Object to File...");
                System.out.println("\nJSON Object: " + obj);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void readJSON() {
        JSONParser parser = new JSONParser();
        List<String> files = getFiles(new File(PATH));
        try {
            for (String f : files) {
                Object obj = parser.parse(new FileReader(PATH + f));
                JSONObject jsonObject = (JSONObject) obj;
                String id = (String) jsonObject.get("ID");
                String preview = (String) jsonObject.get("preview_url");
                JSONArray audioFeatures = (JSONArray) jsonObject.get("attributes");
                /*
                System.out.println("ID: " + id);
                System.out.println("preview_url: " + preview);
                System.out.println("\nAttributes:");
                */
                for (String audioFeature : (Iterable<String>) audioFeatures) {
                    System.out.println(audioFeature);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* BLAH
    public static List<List<String>> readMultipleJSON() {
        List<List<String>> result = new LinkedList<>();
        List<String> files = getFiles(new File(PATH));
        for (String file : files) {
            result.add(readJSON(file));
        }
        return result;
    }
    */

    public static List<String> readJSON(String file) {
        JSONParser parser = new JSONParser();
        List<String> result = new LinkedList<>();
        try {
            Object obj = parser.parse(new FileReader(PATH + file + JSON));
            JSONObject jsonObject = (JSONObject) obj;
            result.add((String)jsonObject.get("ID"));
            result.add((String)jsonObject.get("preview_url"));
            JSONArray features = (JSONArray) jsonObject.get("attributes");
            for(String f : (Iterable<String>) features) {
                result.add(f);
            }
            return result;
        }
        // FileNotFound
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<String> getFiles() {
        return getFiles(new File(PATH));
    }

    private static List<String> getFiles(File folder) {
        List<String> result = new LinkedList<>();
        File[] files = folder.listFiles();
        assert files != null;
        for(File f : files) {
            if(f.isFile()) {
                result.add(f.getName());
            }
        }
        return result;
    }

    public static boolean isInCache(String id) { return isInCache(id, new File(PATH)); }

    /**
     * TODO make it nice and 'functional' instead
     *
     * @param id
     * @param folder
     * @return
     */
    private static boolean isInCache(String id, File folder) {
        File[] files = folder.listFiles();
        assert files != null;
        for(File f : files) {
            if(f.isFile() && f.getName().equals(id + JSON)) {
                // TODO ugly
                return true;
            }
        }
        return false;
    }

}


