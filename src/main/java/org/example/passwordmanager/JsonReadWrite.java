package org.example.passwordmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonReadWrite {

    public static class PasswordManagerData {
        public Metadata metadata_db;

        @JsonProperty("entries_db")
        public List<Entry> entries_db = new ArrayList<>();

        public List<Entry> getEntries_db() {
            return entries_db;
        }

        public void setEntries_db(List<Entry> entries_db) {
            this.entries_db = entries_db;
        }

    }

    public static class AppSession {
        private static String masterPassword;
        public static void setMasterPassword(String password) {masterPassword = password;}
        public static String getMasterPassword() {return masterPassword;}
    }

    public static class Metadata {
        public String master_key;
        public String salt;
    }

    public static class Entry {
        public String entry_website;
        public String entry_username;
        public String entry_password;
        public String entry_url;
        public String entry_notes;
        public String iv;

        public String getEntry_website() {
            return entry_website;
        }

        public void setEntry_website(String entry_website) {
            this.entry_website = entry_website;
        }

        public String getEntry_username() {
            return entry_username;
        }

        public void setEntry_username(String entry_username) {
            this.entry_username = entry_username;
        }

        public String getEntry_password() {
            return entry_password;
        }

        public void setEntry_password(String entry_password) {
            this.entry_password = entry_password;
        }

        public String getEntry_url() {
            return entry_url;
        }

        public void setEntry_url(String entry_url) {
            this.entry_url = entry_url;
        }

        public String getEntry_notes() {
            return entry_notes;
        }

        public void setEntry_notes(String entry_notes) {
            this.entry_notes = entry_notes;
        }

        public String getIV(){return iv;}
        public void setIV(String iv){this.iv = iv;}


    }
    public static boolean validateFile(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);

            JsonNode metadata = root.get("metadata_db");
            JsonNode entries = root.get("entries_db");

            if (metadata == null || !metadata.has("master_key") || !metadata.has("salt") ||
                    entries == null || !entries.isArray()) {
                return false;
            }
            if (!Encryption.isValidBase64(metadata.get("master_key").asText()) ||
                    !Encryption.isValidBase64(metadata.get("salt").asText())) {
                return false;
            }
            for (JsonNode entry : entries) {
                String[] requiredFields = {
                        "entry_website", "entry_username", "entry_password",
                        "entry_url", "entry_notes", "iv"
                };

                for (String field : requiredFields) {
                    if (!entry.has(field) || !Encryption.isValidBase64(entry.get(field).asText())) {
                        return false;
                    }
                }

                byte[] ivBytes = Base64.getDecoder().decode(entry.get("iv").asText());
                if (ivBytes.length != 16) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void createJSONFile(String masterPassword) throws Exception {
        byte[] salt = Encryption.generateSalt();
        String salt_string = Base64.getEncoder().encodeToString(salt);
        SecretKeySpec secretKeySpec = Encryption.getAESKeyFromPassword(masterPassword, salt);
        String hashedKey = Encryption.hashKey(Base64.getEncoder().encodeToString(secretKeySpec.getEncoded()));

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode rootNode = objectMapper.createObjectNode();

        ObjectNode metadataDbNode = objectMapper.createObjectNode();
        metadataDbNode.put("master_key", hashedKey);
        metadataDbNode.put("salt", salt_string);
        rootNode.set("metadata_db", metadataDbNode);

        ArrayNode entriesDbNode = objectMapper.createArrayNode();
        rootNode.set("entries_db", entriesDbNode);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/org/example/passwordmanager/passwordmanager.json"), rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createNewEntry(String entry_website, String entry_username, String entry_password, String entry_url, String entry_notes)
    {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            File jsonFile = new File("src/main/resources/org/example/passwordmanager/passwordmanager.json");
            PasswordManagerData passwordManagerData = objectMapper.readValue(jsonFile, PasswordManagerData.class);

            IvParameterSpec iv = Encryption.generateIV();

            Entry newEntry = new Entry();
            newEntry.setEntry_website(Encryption.encryptCBC(entry_website, PasswordManagerController.getAESKey(), iv));
            newEntry.setEntry_username(Encryption.encryptCBC(entry_username, PasswordManagerController.getAESKey(), iv));
            newEntry.setEntry_password(Encryption.encryptCBC(entry_password, PasswordManagerController.getAESKey(), iv));
            newEntry.setEntry_url(Encryption.encryptCBC(entry_url, PasswordManagerController.getAESKey(), iv));
            newEntry.setEntry_notes(Encryption.encryptCBC(entry_notes, PasswordManagerController.getAESKey(), iv));
            newEntry.setIV(Base64.getEncoder().encodeToString(iv.getIV()));

            passwordManagerData.getEntries_db().add(newEntry);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, passwordManagerData);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<Entry> getAllEntries()
    {
        try {
            File jsonFile = new File("src/main/resources/org/example/passwordmanager/passwordmanager.json");

            ObjectMapper objectMapper = new ObjectMapper();
            PasswordManagerData data = objectMapper.readValue(jsonFile, PasswordManagerData.class);

            List<Entry> entries = data.getEntries_db();
            for (Entry e : entries) {

                IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(e.getIV()));
                e.setEntry_website(Encryption.decryptCBC(e.getEntry_website(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_username(Encryption.decryptCBC(e.getEntry_username(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_password(Encryption.decryptCBC(e.getEntry_password(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_url(Encryption.decryptCBC(e.getEntry_url(), PasswordManagerController.getAESKey(), iv));
                if (!e.getEntry_notes().isEmpty()) {
                    e.setEntry_notes(Encryption.decryptCBC(e.getEntry_notes(), PasswordManagerController.getAESKey(), iv));
                }
            }

            return new ArrayList<>(entries);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static String getSaltString() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PasswordManagerData data = mapper.readValue(new File("src/main/resources/org/example/passwordmanager/passwordmanager.json"), PasswordManagerData.class);
        return data.metadata_db.salt;
    }

    public static Map<String, String> getMasterKeyHashAndSalt() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PasswordManagerData data = mapper.readValue(new File("src/main/resources/org/example/passwordmanager/passwordmanager.json"), PasswordManagerData.class);
            Map<String, String> result = new HashMap<>();
            result.put("master_key", data.metadata_db.master_key);
            result.put("salt", data.metadata_db.salt);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void overwriteFile(ArrayList<Entry> updatedEntries) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            File jsonFile = new File("src/main/resources/org/example/passwordmanager/passwordmanager.json");

            PasswordManagerData passwordManagerData = objectMapper.readValue(jsonFile, PasswordManagerData.class);


            for (Entry e : updatedEntries) {
                IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(e.getIV()));
                e.setEntry_website(Encryption.encryptCBC(e.getEntry_website(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_username(Encryption.encryptCBC(e.getEntry_username(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_password(Encryption.encryptCBC(e.getEntry_password(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_url(Encryption.encryptCBC(e.getEntry_url(), PasswordManagerController.getAESKey(), iv));
                e.setEntry_notes(Encryption.encryptCBC(e.getEntry_notes(), PasswordManagerController.getAESKey(), iv));
            }

            passwordManagerData.setEntries_db(updatedEntries);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, passwordManagerData);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
