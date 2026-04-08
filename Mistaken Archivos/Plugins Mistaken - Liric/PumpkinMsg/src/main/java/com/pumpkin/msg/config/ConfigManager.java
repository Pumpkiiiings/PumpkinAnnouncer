package com.pumpkin.msg.config;

import com.moandjiezana.toml.Toml;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfigManager {
    private final Path dataDirectory;
    private Toml config;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        initializeDirectory();
        loadConfiguration();
    }

    private void initializeDirectory() {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadConfiguration() {
        File file = new File(dataDirectory.toFile(), "config.toml");
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = new Toml().read(file);
    }

    public void saveUUIDSet(Set<UUID> users, String fileName) {
        File file = new File(dataDirectory.toFile(), fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (UUID uuid : users) {
                writer.println(uuid.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<UUID> loadUUIDSet(String fileName) {
        File file = new File(dataDirectory.toFile(), fileName);
        if (!file.exists()) return ConcurrentHashMap.newKeySet();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines()
                    .filter(line -> !line.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        } catch (IOException | IllegalArgumentException e) {
            return ConcurrentHashMap.newKeySet();
        }
    }

    public void saveSpyUsers(Set<UUID> users) {
        saveUUIDSet(users, "spy_data.txt");
    }

    public void saveIgnoreMap(Map<UUID, Set<UUID>> ignoreMap) {
        File file = new File(dataDirectory.toFile(), "ignore_data.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Map.Entry<UUID, Set<UUID>> entry : ignoreMap.entrySet()) {
                if (entry.getValue().isEmpty()) continue;

                String ignoredList = entry.getValue().stream()
                        .map(UUID::toString)
                        .collect(Collectors.joining(","));
                writer.println(entry.getKey().toString() + ":" + ignoredList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Set<UUID>> loadIgnoreMap() {
        Map<UUID, Set<UUID>> map = new ConcurrentHashMap<>();
        File file = new File(dataDirectory.toFile(), "ignore_data.txt");
        if (!file.exists()) return map;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || !line.contains(":")) continue;

                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        UUID user = UUID.fromString(parts[0]);
                        Set<UUID> ignored = Arrays.stream(parts[1].split(","))
                                .map(UUID::fromString)
                                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
                        map.put(user, ignored);
                    } catch (IllegalArgumentException ignoredEx) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    // --- NUEVO: Gestión de Command Spy ---
    public void saveCmdSpy(Map<UUID, String> cmdSpyMap) {
        File file = new File(dataDirectory.toFile(), "cmdspy_data.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Map.Entry<UUID, String> entry : cmdSpyMap.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<UUID, String> loadCmdSpy() {
        Map<UUID, String> map = new ConcurrentHashMap<>();
        File file = new File(dataDirectory.toFile(), "cmdspy_data.txt");
        if (!file.exists()) return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    try {
                        map.put(UUID.fromString(parts[0]), parts[1]);
                    } catch (Exception ignored) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return map;
    }

    public String getString(String path) {
        String value = config.getString(path);
        return value != null ? value : "<red>Missing path '" + path + "' in config.toml</red>";
    }
}
