# ELIZA

A Java implementation of Joseph Weizenbaum's [ELIZA](https://en.wikipedia.org/wiki/ELIZA) chatbot (1966), running the DOCTOR script — a Rogerian psychotherapist simulation. Seasoned with a dash of [Dr. Sbaitso](https://en.wikipedia.org/wiki/Dr._Sbaitso) personality.

## Features

- **Keyword-based pattern matching** with decomposition/reassembly rules, following Weizenbaum's original design
- **Priority system** — when multiple keywords match, the highest-priority rule wins; if its patterns don't fit, the engine falls through to the next matching rule
- **Pronoun reflection** — captured text is reflected (`I` → `you`, `my` → `your`, etc.)
- **Round-robin responses** — each pattern cycles through its reassembly templates
- **Memory** — certain rules store a response for later recall when no keyword matches
- **Parity error** — repeated insults trigger a Dr. Sbaitso-style fake crash (`PARITY ERROR!!! SESSION TERMINATED.`)
- **YAML-driven rules** — all rules and reflections are defined in external YAML files, easy to extend

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
java -jar target/eliza.jar
```

Or on Windows:

```cmd
eliza.cmd
```

Type anything to talk to ELIZA. Type `quit`, `bye`, or `exit` to end the session.

## Example session

```
ELIZA: Hello. I'm ELIZA. How are you feeling today?

You:   I am feeling sad
ELIZA: How long have you been feeling sad?

You:   Since my mother passed away
ELIZA: Tell me more about your family.

You:   I dream about her sometimes
ELIZA: What does that dream suggest to you?

You:   I don't know
ELIZA: Why don't you know?

You:   bye
ELIZA: Goodbye. Thank you for talking with me.
```

## Project structure

```
src/
  main/
    java/com/eliza/
      Eliza.java          # Engine: matching, reflection, memory, insult tracking
      Rule.java           # Rule record with PatternResponse (round-robin)
      Main.java           # Console interface
    resources/
      rules.yaml          # ~90 keyword rules with decomposition/reassembly patterns
      reflections.yaml    # Pronoun/verb reflection mappings
  test/
    java/com/eliza/
      ElizaTest.java      # 24 JUnit 5 tests
```

## How it works

1. **Preprocess** — trim, strip trailing punctuation, normalize whitespace, lowercase
2. **Match** — find all rules whose keyword appears in the input, sorted by descending priority
3. **Decompose** — for each matching rule, try its regex patterns against the input
4. **Reassemble** — fill the response template with captured groups, reflecting pronouns
5. **Fallback chain** — if no pattern fits, try the next rule; then check memory; then use the `@none` fallback

### Rule priority tiers

| Priority | Category |
|----------|----------|
| 7 | Safety (die) |
| 6 | Computer |
| 5 | Family, "I remember", "I want", "I feel", insults (stupid, idiot, shut up, ...) |
| 4 | Emotions, topics (dream, sad, angry, ...) |
| 3 | Greetings, misc patterns, ambiguous adjective insults (annoying, boring, ...) |
| 2 | "my", absolutes (always, never) |
| 1 | Yes/no, uncertainty, sorry |
| 0 | `@none` fallback |

### Memory

Some rules include a `@memory:` directive as one of their round-robin reassemblies. When triggered, the response is stored in a queue instead of being returned. On a later turn where no keyword matches, the stored memory is recalled — giving ELIZA the appearance of remembering earlier topics.

### Parity error

Rules tagged with `insult: true` increment a counter when matched. After 4 insults, ELIZA returns a `PARITY ERROR!!!` message and the console displays a fake system crash — a nod to Dr. Sbaitso's behavior from 1991.

## Tests

```bash
mvn test
```

24 tests covering keyword matching, priority ordering, pronoun reflection, preprocessing, round-robin cycling, fallback, memory storage/recall, and parity error behavior.

## Extending the rules

Edit `src/main/resources/rules.yaml` to add new rules:

```yaml
- keyword: "weather"
  priority: 3
  patterns:
    - decomposition: ".*weather(.*)"
      reassemblies:
        - "Does the weather affect your mood?"
        - "Tell me how{1} weather makes you feel."
```

- `keyword` — substring matched against the preprocessed (lowercased) input
- `priority` — higher wins; ties go to the first rule defined
- `insult: true` — optional flag to count toward parity error
- `decomposition` — Java regex; capture groups are referenced as `{1}`, `{2}`, ...
- `reassemblies` — cycled round-robin; prefix with `@memory:` to store instead of respond
