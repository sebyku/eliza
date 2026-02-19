package com.eliza;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ELIZA engine. Implements keyword-based pattern matching with
 * decomposition/reassembly rules and pronoun reflection, following
 * Weizenbaum's original 1966 design.
 *
 * <p>Rules and reflections are loaded from YAML files on the classpath:
 * {@code reflections.yaml} and {@code rules.yaml}.
 */
public class Eliza {

    private static final int INSULT_THRESHOLD = 4;
    public static final String PARITY_ERROR = "PARITY ERROR!!! PARITY ERROR!!! SESSION TERMINATED.";

    private final List<Rule> rules;
    private final Deque<String> memory;
    private final Map<String, String> reflections;
    private final String language;
    private int insultCount;

    public Eliza() {
        this("us");
    }

    public Eliza(String language) {
        this.language = language;
        this.rules = new ArrayList<>();
        this.memory = new LinkedList<>();
        this.reflections = loadReflections();
        this.insultCount = 0;
        loadRules();
    }

    public boolean hasParityError() {
        return insultCount >= INSULT_THRESHOLD;
    }

    /**
     * Process user input and return ELIZA's response.
     */
    public String respond(String input) {
        String text = preprocess(input);

        // Collect all matching rules, sorted by descending priority
        List<Rule> matchingRules = new ArrayList<>();
        for (Rule rule : rules) {
            if (text.contains(stripAccents(rule.keyword()))) {
                matchingRules.add(rule);
            }
        }
        matchingRules.sort((a, b) -> Integer.compare(b.priority(), a.priority()));

        // Try each matching rule in priority order until a pattern fits
        boolean storedMemory = false;
        for (Rule rule : matchingRules) {
            int memorySizeBefore = memory.size();
            String response = applyRule(rule, text);
            storedMemory = storedMemory || memory.size() > memorySizeBefore;
            if (response != null) {
                if (rule.insult()) {
                    insultCount++;
                    if (insultCount >= INSULT_THRESHOLD) {
                        return PARITY_ERROR;
                    }
                }
                return response;
            }
        }

        // Try memory — but not if we just stored one this turn
        if (!storedMemory && !memory.isEmpty()) {
            return memory.pollFirst();
        }

        // Fallback
        return applyFallback(text);
    }

    private String applyRule(Rule rule, String text) {
        for (Rule.PatternResponse pr : rule.patterns()) {
            Pattern pattern = Pattern.compile(stripAccents(pr.decomposition()), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String template = pr.nextReassembly();

                // Check if this is a memory-store directive
                if (template.startsWith("@memory:")) {
                    String memoryTemplate = template.substring(8);
                    String memResponse = fillTemplate(memoryTemplate, matcher);
                    memory.addLast(memResponse);
                    return null; // Signal to keep searching
                }

                return fillTemplate(template, matcher);
            }
        }
        return null;
    }

    private String fillTemplate(String template, Matcher matcher) {
        String result = template;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String captured = matcher.group(i);
            if (captured != null) {
                captured = reflect(captured.trim());
                result = result.replace("{" + i + "}", captured);
            }
        }
        return result;
    }

    private String applyFallback(String text) {
        for (Rule rule : rules) {
            if ("@none".equals(rule.keyword())) {
                for (Rule.PatternResponse pr : rule.patterns()) {
                    return pr.nextReassembly();
                }
            }
        }
        return "Please go on.";
    }

    /**
     * Reflect pronouns in captured text (I -> you, my -> your, etc.).
     */
    private String reflect(String text) {
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String lower = words[i].toLowerCase();
            if (reflections.containsKey(lower)) {
                sb.append(reflections.get(lower));
            } else {
                sb.append(words[i]);
            }
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    static String stripAccents(String text) {
        String result = text.replace("œ", "oe").replace("Œ", "OE")
                .replace("æ", "ae").replace("Æ", "AE");
        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String preprocess(String input) {
        return stripAccents(
                input.trim()
                        .replaceAll("[.!,;]+$", "")  // strip trailing punctuation
                        .replaceAll("\\s+", " ")      // normalize whitespace
                        .toLowerCase());
    }

    // ─────────────────────────────────────────────────────────────
    // YAML loading
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, String> loadReflections() {
        String filename = "reflections_" + language + ".yaml";
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalStateException(filename + " not found on classpath");
            }
            Map<String, Object> root = yaml.load(in);
            Map<String, String> raw = (Map<String, String>) root.get("reflections");
            // Normalize keys to accent-stripped form so lookups on preprocessed text match
            Map<String, String> normalized = new HashMap<>();
            for (Map.Entry<String, String> entry : raw.entrySet()) {
                normalized.put(stripAccents(entry.getKey()), entry.getValue());
            }
            return normalized;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadRules() {
        String filename = "rules_" + language + ".yaml";
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalStateException(filename + " not found on classpath");
            }
            Map<String, Object> root = yaml.load(in);
            List<Map<String, Object>> ruleList = (List<Map<String, Object>>) root.get("rules");

            for (Map<String, Object> entry : ruleList) {
                String keyword = (String) entry.get("keyword");
                int priority = (int) entry.get("priority");
                boolean insult = Boolean.TRUE.equals(entry.get("insult"));

                List<Map<String, Object>> patternEntries = (List<Map<String, Object>>) entry.get("patterns");
                List<Rule.PatternResponse> patternList = new ArrayList<>();
                for (Map<String, Object> p : patternEntries) {
                    String decomposition = (String) p.get("decomposition");
                    List<String> reassemblies = (List<String>) p.get("reassemblies");
                    patternList.add(new Rule.PatternResponse(decomposition, reassemblies));
                }

                rules.add(new Rule(keyword, priority, insult, patternList));
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }
}
