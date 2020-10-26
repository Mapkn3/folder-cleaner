package my.mapkn3;


import com.google.gson.Gson;
import my.mapkn3.visitor.DeleteFileVisitor;
import my.mapkn3.model.Config;
import my.mapkn3.model.Rule;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class App {
    public static final int MISSING_ARG_EXIT_CODE = 1;
    public static final int INVALID_PATH_EXIT_CODE = 2;
    public static final int MISSING_CONFIG_ITEM_EXIT_CODE = 3;
    public static final int PATH_IS_NOT_A_DIR_EXIT_CODE = 4;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("The required argument is missing. Use 'java -jar folder-cleaner.jar /path/to/config.json'");
            System.exit(MISSING_ARG_EXIT_CODE);
        }
        try {
            Config config = parseConfig(args[0]);
            config.getRules().forEach(App::validateConfig);
            for (Rule rule : config.getRules()) {
                deleteFiles(rule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Config parseConfig(String path) {
        Config config = new Config();
        try {
            Gson gson = new Gson();
            config = gson.fromJson(new FileReader(path), Config.class);
        } catch (FileNotFoundException e) {
            System.err.printf("Invalid path to the configuration file: %s\n", path);
            System.exit(INVALID_PATH_EXIT_CODE);
        }
        return config;
    }

    private static void deleteFiles(Rule rule) throws IOException {
        String ruleRoot = rule.getRoot();
        String ruleMask = rule.getMask();
        try {
            Path root = Paths.get(ruleRoot);
            if (!Files.isDirectory(root)) {
                System.err.printf("The 'root' value is not a directory: %s\n", ruleRoot);
                System.exit(PATH_IS_NOT_A_DIR_EXIT_CODE);
            }
            String regex = maskToRegex(ruleMask);
            Pattern pattern = Pattern.compile(regex);
            System.out.printf("Start folder processing for %s with mask %s (regex pattern: %s)\n", ruleRoot, ruleMask, pattern.toString());
            Files.walkFileTree(root, new DeleteFileVisitor(pattern));
            System.out.printf("Stop folder processing for %s\n", ruleRoot);
        } catch (InvalidPathException e) {
            System.err.printf("Invalid path to the target directory: %s\n", ruleRoot);
            System.exit(INVALID_PATH_EXIT_CODE);
        }
    }

    private static void validateConfig(Rule rule) {
        if (rule.getRoot() == null) {
            System.err.println("The required 'root' configuration item is missing.");
            System.exit(MISSING_CONFIG_ITEM_EXIT_CODE);
        }
        if (rule.getMask() == null) {
            System.err.println("The required 'mask' configuration item is missing.");
            System.exit(MISSING_CONFIG_ITEM_EXIT_CODE);
        }
    }

    private static String maskToRegex(String mask) {
        return mask
                .replaceAll("\\.", "\\\\.") // . -> \.
                .replaceAll("\\*", ".*"); // * -> .*
    }
}
