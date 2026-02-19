# ELIZA

A Java implementation of Joseph Weizenbaum's [ELIZA](https://en.wikipedia.org/wiki/ELIZA) chatbot (1966), running the DOCTOR script — a Rogerian psychotherapist simulation. Seasoned with a dash of [Dr. Sbaitso](https://en.wikipedia.org/wiki/Dr._Sbaitso) personality.

## Features

- **Keyword-based pattern matching** with decomposition/reassembly rules, following Weizenbaum's original design
- **Priority system** — when multiple keywords match, the highest-priority rule wins; if its patterns don't fit, the engine falls through to the next matching rule
- **Pronoun reflection** — captured text is reflected (`I` → `you`, `my` → `your`, `je` → `vous`, etc.)
- **Round-robin responses** — each pattern cycles through its reassembly templates
- **Memory** — certain rules store a response for later recall when no keyword matches
- **Parity error** — repeated insults trigger a Dr. Sbaitso-style fake crash (`PARITY ERROR!!! SESSION TERMINATED.`)
- **YAML-driven rules** — all rules, reflections, and UI messages are defined in external YAML files, easy to extend
- **Multilingual** — English (default) and French, selectable at startup via a CLI argument
- **Accent-insensitive matching** — French users can type with or without accents (`deprime` matches `déprimé`); ligatures like `œ` are also handled (`sœur` matches `soeur`)

## Requirements

- Java 17+
- Maven 3.8+

## Build

```bash
mvn clean package
```

This produces an executable fat JAR at `target/eliza.jar`.

## Run

```bash
# English (default)
java -jar target/eliza.jar

# French
java -jar target/eliza.jar fr
```

Or on Windows:

```cmd
eliza.cmd
eliza.cmd fr
```

### English session

Type anything to talk to ELIZA. Type `quit`, `bye`, or `exit` to end the session.

```
ELIZA: Hello. I'm ELIZA. How are you feeling today?

You:   I am feeling sad
ELIZA: I'm sorry to hear that you are sad.

You:   Since my mother passed away
ELIZA: Tell me more about your mother.

You:   I dream about her sometimes
ELIZA: What does that dream suggest to you?

You:   I don't know
ELIZA: Why don't you know?

You:   bye
ELIZA: Goodbye. Thank you for talking with me.
```

### French session

Tapez ce que vous voulez. Tapez `quitter`, `quit` ou `au revoir` pour sortir.

```
ELIZA: Bonjour. Je suis ELIZA. Comment vous sentez-vous aujourd'hui ?

Vous:  je suis triste
ELIZA: Je suis désolé d'apprendre que vous êtes triste.

Vous:  ma mere est decedee
ELIZA: Parlez-moi davantage de votre mère.

Vous:  je reve d'elle parfois
ELIZA: Que vous suggère ce rêve ?

Vous:  quitter
ELIZA: Au revoir. Merci d'avoir parlé avec moi.
```

## Project structure

```
src/
  main/
    java/com/eliza/
      Eliza.java              # Engine: matching, reflection, memory, insult tracking
      Rule.java               # Rule record with PatternResponse (round-robin)
      Main.java               # Console interface, loads messages from YAML
    resources/
      rules_us.yaml           # ~170 English keyword rules
      rules_fr.yaml           # ~180 French keyword rules
      reflections_us.yaml     # English pronoun/verb reflection mappings
      reflections_fr.yaml     # French pronoun/verb reflection mappings
      messages_us.yaml        # English UI strings (intro, greetings, prompts, etc.)
      messages_fr.yaml        # French UI strings
  test/
    java/com/eliza/
      ElizaTest.java          # 32 JUnit 5 tests
```

## How it works

1. **Preprocess** — trim, strip trailing punctuation, normalize whitespace, lowercase, strip accents and expand ligatures (`œ` → `oe`)
2. **Match** — find all rules whose keyword appears in the input (accent-insensitive), sorted by descending priority
3. **Decompose** — for each matching rule, try its regex patterns (accent-insensitive) against the input
4. **Reassemble** — fill the response template with captured groups, reflecting pronouns; responses keep proper accents
5. **Fallback chain** — if no pattern fits, try the next rule; then check memory; then use the `@none` fallback

### Rule priority tiers

| Priority | Category |
|----------|----------|
| 7 | Safety (die/mourir) |
| 6 | Computer/ordinateur |
| 5 | Family, "I remember", "I want", "I feel", insults |
| 4 | Emotions, topics (dream, sad, angry, ...) |
| 3 | Greetings, misc patterns, work, help |
| 2 | "I am"/"je suis", "my", absolutes (always, never) |
| 1 | Yes/no, uncertainty, sorry |
| 0 | `@none` fallback |

Note: `"i am"` / `"je suis"` are intentionally low priority (2) so that specific keywords like `"sad"` (4) or `"depressed"` (4) win when the input is `"I am sad"`.

### Memory

Some rules include a `@memory:` directive as one of their round-robin reassemblies. When triggered, the response is stored in a queue instead of being returned. On a later turn where no keyword matches, the stored memory is recalled — giving ELIZA the appearance of remembering earlier topics.

### Parity error

Rules tagged with `insult: true` increment a counter when matched. After 4 insults, ELIZA returns a `PARITY ERROR!!!` message and the console displays a fake system crash — a nod to Dr. Sbaitso's behavior from 1991.

## Tests

```bash
mvn test
```

32 tests covering keyword matching, priority ordering, pronoun reflection, preprocessing, round-robin cycling, fallback, memory storage/recall, parity error, French language loading, accent-insensitive matching, and ligature handling.

## Adding a language

1. Create `rules_{lang}.yaml` — keyword rules with decomposition/reassembly patterns
2. Create `reflections_{lang}.yaml` — pronoun/verb reflection mappings
3. Create `messages_{lang}.yaml` — UI strings (intro, greetings, prompt, goodbye, quit_words, crash)
4. Add the language code to the CLI check in `Main.java`

## Extending the rules

Edit `src/main/resources/rules_{lang}.yaml` to add new rules:

```yaml
- keyword: "weather"
  priority: 3
  patterns:
    - decomposition: ".*weather(.*)"
      reassemblies:
        - "Does the weather affect your mood?"
        - "Tell me how{1} weather makes you feel."
```

- `keyword` — substring matched against the preprocessed input (accent-insensitive)
- `priority` — higher wins; ties go to the first rule defined
- `insult: true` — optional flag to count toward parity error
- `decomposition` — Java regex, matched accent-insensitively; capture groups are referenced as `{1}`, `{2}`, ...
- `reassemblies` — cycled round-robin; prefix with `@memory:` to store instead of respond; keep proper accents here
