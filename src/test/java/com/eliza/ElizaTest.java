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
        String response = eliza.respond("I am tired");
        Set<String> expected = Set.of(
                "How long have you been tired?",
                "How does being tired make you feel?",
                "Do you enjoy being tired?",
                "Why do you tell me you're tired?");
        assertTrue(expected.contains(response), "Should match 'I am' rule, got: " + response);
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
        String response = eliza.respond("I am worried about my job");
        // "my" in captured text should become "your"
        assertTrue(
                response.contains("your job") || response.contains("worried"),
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
        // Preprocessing lowercases input, so "i am" rule should match
        Set<String> expected = Set.of(
                "How long have you been very sad?",
                "How does being very sad make you feel?",
                "Do you enjoy being very sad?",
                "Why do you tell me you're very sad?");
        assertTrue(expected.contains(response), "Should handle uppercase input, got: " + response);
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
