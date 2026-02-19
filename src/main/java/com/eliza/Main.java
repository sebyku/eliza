package com.eliza;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * Console interface for the ELIZA chatbot.
 * Type "quit" or "bye" to exit.
 */
public class Main {

    public static void main(String[] args) {
        String language = "us";
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if ("fr".equals(arg) || "us".equals(arg)) {
                language = arg;
            } else {
                System.err.println("Unknown language: " + args[0] + ". Supported: us, fr. Defaulting to us.");
            }
        }

        Map<String, Object> messages = loadMessages(language);
        Eliza eliza = new Eliza(language);
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String intro = ((String) messages.get("intro")).stripTrailing();
        List<String> greetings = getList(messages, "greetings");
        String prompt = (String) messages.get("prompt");
        String goodbye = (String) messages.get("goodbye");
        List<String> quitWords = getList(messages, "quit_words");
        List<String> crashLines = getList(messages, "crash");

        System.out.println(intro);
        System.out.println("ELIZA: " + greetings.get(random.nextInt(greetings.size())) + "\n");

        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (quitWords.stream().anyMatch(input::equalsIgnoreCase)) {
                System.out.println("\nELIZA: " + goodbye);
                break;
            }

            String response = eliza.respond(input);
            System.out.println("ELIZA: " + response + "\n");

            if (eliza.hasParityError()) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                for (String line : crashLines) {
                    System.out.println("    " + line);
                }
                System.out.println();
                break;
            }
        }

        scanner.close();
    }

    @SuppressWarnings("unchecked")
    private static List<String> getList(Map<String, Object> map, String key) {
        return (List<String>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadMessages(String language) {
        String filename = "messages_" + language + ".yaml";
        Yaml yaml = new Yaml();
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalStateException(filename + " not found on classpath");
            }
            return yaml.load(in);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }
}
