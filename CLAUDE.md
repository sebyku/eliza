# CLAUDE.md

## Project overview

ELIZA chatbot — a Java implementation of Weizenbaum's 1966 DOCTOR script with Dr. Sbaitso personality. Supports English (`us`) and French (`fr`) via YAML-driven rules.

## Build & test

```bash
mvn clean package        # build fat JAR (target/eliza.jar)
mvn clean test           # run 32 JUnit 5 tests
java -jar target/eliza.jar       # run in English
java -jar target/eliza.jar fr    # run in French
```

## Architecture

Single-module Maven project. Java 17, SnakeYAML 2.2, JUnit 5.

### Source files

| File | Role |
|------|------|
| `Main.java` | Console UI — parses `args[0]` for language, loads `messages_{lang}.yaml`, runs the input loop |
| `Eliza.java` | Engine — loads `rules_{lang}.yaml` and `reflections_{lang}.yaml`, keyword matching, decomposition/reassembly, pronoun reflection, memory queue, insult counter |
| `Rule.java` | `record Rule(keyword, priority, insult, patterns)` with inner `PatternResponse` class (round-robin index) |

### Resource files (per language: `us`, `fr`)

| File | Content |
|------|---------|
| `rules_{lang}.yaml` | Keyword rules: keyword, priority, insult flag, decomposition regex, reassembly templates |
| `reflections_{lang}.yaml` | Pronoun/verb reflection map (e.g., `i` → `you`, `je` → `vous`) |
| `messages_{lang}.yaml` | UI strings: intro banner, greetings, prompt, goodbye, quit_words, crash lines |

### Engine flow

1. `preprocess()` — trim, strip punctuation, normalize whitespace, lowercase, **strip accents** (NFD + ligature expansion `œ`→`oe`, `æ`→`ae`)
2. Keyword matching — `text.contains(stripAccents(keyword))` — collects all matching rules, sorts by descending priority
3. Decomposition — regex compiled with `stripAccents(pattern)`, matched against accent-stripped input
4. Reassembly — templates keep proper accents; captured groups are reflected via the reflections map
5. Fallback — memory queue → `@none` catch-all

### Key design decisions

- **Accent-insensitive matching**: input, keywords, and decomposition patterns are all accent-stripped for comparison. Reassembly templates and `@memory:` directives keep proper accents. Reflection map keys are stripped at load time.
- **Ligature handling**: `stripAccents()` expands `œ`→`oe` and `æ`→`ae` before NFD normalization, since `Normalizer.NFD` does not decompose ligatures.
- **`"i am"` / `"je suis"` at priority 2**: intentionally low so that specific keywords (`sad` at 4, `depressed` at 4, etc.) win over the generic identity pattern.
- **Keywords stay unaccented in YAML**: keywords and decomposition patterns in the YAML files don't need accents — the engine strips both sides. Only reassemblies need proper accents.

## Adding a new language

1. Create `src/main/resources/rules_{lang}.yaml`
2. Create `src/main/resources/reflections_{lang}.yaml`
3. Create `src/main/resources/messages_{lang}.yaml`
4. Add the language code to the CLI check in `Main.java` (line ~21)

## Git

- Remote: `git@github.com:sebyku/eliza.git`
- Branch: `master`
