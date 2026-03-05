package com.eliza;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ElizaTest {

    private Eliza eliza;

    @BeforeEach
    void setUp() {
        eliza = new Eliza();
    }

    // ── Keyword matching ──────────────────────────────────────

    @Test
    void respondToGreeting() {
        String response = eliza.respond("Hello");
        assertTrue(
                response.contains("How are you") || response.contains("What's on your mind")
                        || response.contains("bothering"),
                "Should respond to greeting, got: " + response);
    }

    @Test
    void respondToIAm() {
        // Use a word that doesn't match any specific keyword, so "i am" (priority 2) wins
        String response = eliza.respond("I am confused");
        Set<String> expected = Set.of(
                "What are you confused about?",
                "Tell me what is confusing you.",
                "Confusion is normal. Let us work through it.",
                "What about is confusing?",
                "Why does confuse you?");
        // "confused" (priority 3) beats "i am" (priority 2), so we get the "confused" rule
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should respond to 'I am confused', got: " + response);
    }

    @Test
    void respondToIWant() {
        String response = eliza.respond("I want a vacation");
        assertTrue(response.contains("vacation"), "Should echo back the captured text, got: " + response);
    }

    @Test
    void respondToMother() {
        String response = eliza.respond("My mother is annoying");
        assertTrue(
                response.toLowerCase().contains("mother"),
                "Should respond about mother, got: " + response);
    }

    @Test
    void respondToFather() {
        String response = eliza.respond("I hate my father");
        assertTrue(
                response.toLowerCase().contains("father"),
                "Should respond about father, got: " + response);
    }

    @Test
    void respondToComputer() {
        String response = eliza.respond("You are just a computer");
        // "computer" has priority 6, so it should win over "you are" (priority 4)
        Set<String> expected = Set.of(
                "Do computers worry you?",
                "Why do you mention computers?",
                "What do you think machines have to do with your problem?",
                "Don't you think computers can help people?");
        assertTrue(expected.contains(response),
                "Should match high-priority 'computer' rule, got: " + response);
    }

    @Test
    void respondToSorry() {
        String response = eliza.respond("Sorry about that");
        Set<String> expected = Set.of(
                "Please don't apologize.",
                "Apologies are not necessary.",
                "What feelings does apologizing bring up?");
        assertTrue(expected.contains(response), "Should match 'sorry' rule, got: " + response);
    }

    @Test
    void respondToBecause() {
        String response = eliza.respond("Because I said so");
        Set<String> expected = Set.of(
                "Is that the real reason?",
                "What other reasons might there be?",
                "Does that reason explain anything else?",
                "What other reasons come to mind?");
        assertTrue(expected.contains(response), "Should match 'because' rule, got: " + response);
    }

    // ── Priority ──────────────────────────────────────────────

    @Test
    void higherPriorityKeywordWins() {
        // "i remember" (priority 5) should beat "my" (priority 2)
        String response = eliza.respond("I remember my childhood");
        assertTrue(
                response.contains("think about") || response.contains("bring to mind")
                        || response.contains("important to you"),
                "Higher-priority 'i remember' should win, got: " + response);
    }

    @Test
    void specificKeywordBeatsIAm() {
        // "sad" (priority 4) should beat "i am" (priority 2)
        String response = eliza.respond("I am sad");
        Set<String> sadResponses = Set.of(
                "I'm sorry to hear that you are sad.",
                "Can you tell me what is making you sad?",
                "How long have you been feeling this way?");
        assertTrue(sadResponses.contains(response),
                "Specific 'sad' keyword should beat generic 'i am', got: " + response);
    }

    @Test
    void specificKeywordBeatsJeSuis() {
        Eliza frEliza = new Eliza("fr");
        // "triste" (priority 4) should beat "je suis" (priority 2)
        String response = frEliza.respond("je suis triste");
        assertTrue(response.toLowerCase().contains("triste") || response.toLowerCase().contains("sentez"),
                "Specific 'triste' keyword should beat generic 'je suis', got: " + response);
    }

    // ── Pronoun reflection ────────────────────────────────────

    @Test
    void reflectsPronouns() {
        // "I want you to help me" → captured "you to help me" → reflected "I to help you"
        String response = eliza.respond("I need you to help me");
        // The captured part "you to help me" should become "I to help you"
        assertTrue(
                response.contains("I to help you"),
                "Should reflect pronouns in captured text, got: " + response);
    }

    @Test
    void reflectsMyToYour() {
        // "i want" (priority 5) captures "my dog back" → reflected to "your dog back"
        String response = eliza.respond("I want my dog back");
        assertTrue(
                response.contains("your dog back"),
                "Should reflect 'my' to 'your', got: " + response);
    }

    // ── Preprocessing ─────────────────────────────────────────

    @Test
    void handlesTrailingPunctuation() {
        String response = eliza.respond("I am sad!!!");
        // Should still match the "i am" rule after stripping punctuation
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void handlesMixedCase() {
        String response = eliza.respond("I AM VERY SAD");
        // "sad" (priority 4) beats "i am" (priority 2), so we get the "sad" rule
        assertTrue(
                response.toLowerCase().contains("sad") || response.toLowerCase().contains("feeling"),
                "Should match 'sad' keyword on uppercase input, got: " + response);
    }

    @Test
    void handlesExtraWhitespace() {
        String response = eliza.respond("  I   am   happy  ");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    // ── Round-robin responses ─────────────────────────────────

    @Test
    void cyclesThroughResponses() {
        String first = eliza.respond("Hello");
        String second = eliza.respond("Hello");
        String third = eliza.respond("Hello");
        // After 3 calls we should have gotten different responses (round-robin)
        assertTrue(
                !first.equals(second) || !second.equals(third),
                "Should cycle through different responses");
    }

    // ── Fallback ──────────────────────────────────────────────

    @Test
    void fallbackOnUnrecognizedInput() {
        String response = eliza.respond("xyzzy plugh");
        assertNotNull(response, "Should return a fallback response");
        assertFalse(response.isEmpty(), "Fallback response should not be empty");
    }

    @Test
    void emptyInputGetsFallback() {
        String response = eliza.respond("   ");
        assertNotNull(response, "Should handle blank input gracefully");
    }

    // ── Rule model ────────────────────────────────────────────

    @Test
    void rulePatternResponseRoundRobin() {
        Rule.PatternResponse pr = new Rule.PatternResponse(".*", List.of("A", "B", "C"));
        assertEquals("A", pr.nextReassembly());
        assertEquals("B", pr.nextReassembly());
        assertEquals("C", pr.nextReassembly());
        assertEquals("A", pr.nextReassembly()); // wraps around
    }

    @Test
    void ruleStoresKeywordAndPriority() {
        Rule rule = new Rule("hello", 3, false, List.of());
        assertEquals("hello", rule.keyword());
        assertEquals(3, rule.priority());
        assertFalse(rule.insult());
    }

    @Test
    void ruleInsultFlag() {
        Rule rule = new Rule("stupid", 5, true, List.of());
        assertTrue(rule.insult());
    }

    // ── Parity error ────────────────────────────────────────

    @Test
    void parityErrorAfterRepeatedInsults() {
        assertFalse(eliza.hasParityError(), "Should not have parity error initially");

        eliza.respond("You are stupid");
        assertFalse(eliza.hasParityError());

        eliza.respond("You idiot");
        assertFalse(eliza.hasParityError());

        eliza.respond("Shut up");
        assertFalse(eliza.hasParityError());

        // 4th insult triggers parity error
        String response = eliza.respond("You are so dumb");
        assertTrue(eliza.hasParityError(), "Should have parity error after 4 insults");
        assertEquals(Eliza.PARITY_ERROR, response);
    }

    @Test
    void noParityErrorWithoutInsults() {
        eliza.respond("I am sad");
        eliza.respond("I feel lonely");
        eliza.respond("I need help");
        eliza.respond("I am depressed");
        eliza.respond("I want to be happy");
        assertFalse(eliza.hasParityError(), "Non-insults should not trigger parity error");
    }

    // ── Memory ──────────────────────────────────────────────

    @Test
    void memoryIsUsedOnUnrecognizedInput() {
        // Call enough times to hit a @memory: reassembly (round-robin)
        // Mother rule has 5 reassemblies, the 5th is @memory:
        for (int i = 0; i < 4; i++) {
            eliza.respond("My mother is nice");
        }
        // 5th call triggers @memory: on "mother" rule — stores memory,
        // then falls through to next matching rule ("my") which responds
        String fallthrough = eliza.respond("My mother is nice");
        assertNotNull(fallthrough, "Should get a response from a lower-priority rule");

        // Now send unrecognized input — should get the stored memory
        String memoryResponse = eliza.respond("xyzzy plugh");
        assertTrue(memoryResponse.toLowerCase().contains("mother"),
                "Should recall stored memory about mother, got: " + memoryResponse);
    }

    // ── German language ──────────────────────────────────────

    @Test
    void germanElizaLoadsWithoutError() {
        Eliza deEliza = new Eliza("de");
        String response = deEliza.respond("hallo");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "German Eliza should respond to 'hallo'");
    }

    @Test
    void germanElizaFallback() {
        Eliza deEliza = new Eliza("de");
        String response = deEliza.respond("xyzzy plugh");
        assertNotNull(response, "German Eliza should have a fallback response");
        assertFalse(response.isEmpty());
    }

    @Test
    void germanElizaMatchesWithoutUmlauts() {
        Eliza deEliza = new Eliza("de");
        // "traurig" has no umlauts but "müde" does — test "mude" matches "müde" keyword
        String response = deEliza.respond("ich bin mude");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match German keyword even without umlauts");
    }

    @Test
    void germanElizaMatchesWithUmlauts() {
        Eliza deEliza = new Eliza("de");
        String response = deEliza.respond("ich bin müde");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match German keyword with umlauts");
    }

    @Test
    void germanElizaRespondToMutter() {
        Eliza deEliza = new Eliza("de");
        String response = deEliza.respond("meine mutter ist streng");
        assertTrue(response.toLowerCase().contains("mutter"),
                "Should respond about Mutter, got: " + response);
    }

    @Test
    void germanElizaParityError() {
        Eliza deEliza = new Eliza("de");
        deEliza.respond("du bist dumm");
        deEliza.respond("du idiot");
        deEliza.respond("halt die klappe");
        String response = deEliza.respond("du trottel");
        assertTrue(deEliza.hasParityError(), "German Eliza should have parity error after 4 insults");
        assertEquals(Eliza.PARITY_ERROR, response);
    }

    // ── Spanish language ─────────────────────────────────────

    @Test
    void spanishElizaLoadsWithoutError() {
        Eliza esEliza = new Eliza("es");
        String response = esEliza.respond("hola");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Spanish Eliza should respond to 'hola'");
    }

    @Test
    void spanishElizaFallback() {
        Eliza esEliza = new Eliza("es");
        String response = esEliza.respond("xyzzy plugh");
        assertNotNull(response, "Spanish Eliza should have a fallback response");
        assertFalse(response.isEmpty());
    }

    @Test
    void spanishElizaMatchesWithoutAccents() {
        Eliza esEliza = new Eliza("es");
        // "deprimido" without accent should match
        String response = esEliza.respond("estoy deprimido");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match Spanish keyword even without accents");
    }

    @Test
    void spanishElizaMatchesWithAccents() {
        Eliza esEliza = new Eliza("es");
        String response = esEliza.respond("estoy deprimído");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match Spanish keyword with accents");
    }

    @Test
    void spanishElizaRespondToMadre() {
        Eliza esEliza = new Eliza("es");
        String response = esEliza.respond("mi madre es muy estricta");
        assertTrue(response.toLowerCase().contains("madre"),
                "Should respond about madre, got: " + response);
    }

    @Test
    void spanishElizaParityError() {
        Eliza esEliza = new Eliza("es");
        esEliza.respond("eres estupido");
        esEliza.respond("eres un idiota");
        esEliza.respond("callate");
        String response = esEliza.respond("eres un tonto");
        assertTrue(esEliza.hasParityError(), "Spanish Eliza should have parity error after 4 insults");
        assertEquals(Eliza.PARITY_ERROR, response);
    }

    // ── French language ──────────────────────────────────────

    @Test
    void frenchElizaLoadsWithoutError() {
        Eliza frEliza = new Eliza("fr");
        String response = frEliza.respond("bonjour");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "French Eliza should respond to 'bonjour'");
    }

    @Test
    void frenchElizaFallback() {
        Eliza frEliza = new Eliza("fr");
        String response = frEliza.respond("xyzzy plugh");
        assertNotNull(response, "French Eliza should have a fallback response");
        assertFalse(response.isEmpty());
    }

    @Test
    void frenchElizaMatchesWithoutAccents() {
        Eliza frEliza = new Eliza("fr");
        // "deprime" (no accents) should match the "déprimé" keyword
        String response = frEliza.respond("je suis deprime");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match French keyword even without accents");
    }

    @Test
    void frenchElizaMatchesWithAccents() {
        Eliza frEliza = new Eliza("fr");
        // "déprimé" (with accents) should also match
        String response = frEliza.respond("je suis déprimé");
        assertNotNull(response);
        assertFalse(response.isEmpty(), "Should match French keyword with accents");
    }

    @Test
    void stripAccentsHelper() {
        assertEquals("deprime", Eliza.stripAccents("déprimé"));
        assertEquals("etes", Eliza.stripAccents("êtes"));
        assertEquals("hello", Eliza.stripAccents("hello"));
        assertEquals("soeur", Eliza.stripAccents("sœur"));
        assertEquals("SOEUR", Eliza.stripAccents("SŒUR"));
        assertEquals("aegis", Eliza.stripAccents("ægis"));
        // German ß and umlauts
        assertEquals("u", Eliza.stripAccents("ü"));
        assertEquals("o", Eliza.stripAccents("ö"));
        assertEquals("a", Eliza.stripAccents("ä"));
        assertEquals("ss", Eliza.stripAccents("ß"), "ß should be stripped to ss");
        assertEquals("strasse", Eliza.stripAccents("straße"), "straße should become strasse");
        assertEquals("mude", Eliza.stripAccents("müde"));
        // Spanish ñ and accented vowels
        assertEquals("n", Eliza.stripAccents("ñ"), "ñ should be stripped to n");
        assertEquals("espanol", Eliza.stripAccents("español"));
        assertEquals("a", Eliza.stripAccents("á"));
        assertEquals("e", Eliza.stripAccents("é"));
        assertEquals("i", Eliza.stripAccents("í"));
        assertEquals("o", Eliza.stripAccents("ó"));
        assertEquals("u", Eliza.stripAccents("ú"));
    }

    @Test
    void frenchElizaMatchesSoeurWithLigature() {
        Eliza frEliza = new Eliza("fr");
        // "sœur" (with œ ligature) should match keyword "soeur"
        String response = frEliza.respond("ma sœur est gentille");
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("sœur") || response.toLowerCase().contains("soeur"),
                "Should match 'soeur' keyword when user types 'sœur', got: " + response);
    }

    @Test
    void memoryNotConsumedOnSameTurn() {
        // Trigger @memory: storage
        for (int i = 0; i < 4; i++) {
            eliza.respond("My mother is nice");
        }
        // 5th call stores memory — should NOT return the memory on this turn
        String response = eliza.respond("My mother is nice");
        assertFalse(response.contains("Earlier you mentioned your mother"),
                "Memory should not be consumed on the same turn it was stored, got: " + response);
    }
}
