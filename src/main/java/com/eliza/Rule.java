package com.eliza;

import java.util.List;

/**
 * A keyword rule for ELIZA. Each rule has a keyword, a priority,
 * an insult flag, and a list of pattern/reassembly pairs.
 */
public record Rule(String keyword, int priority, boolean insult, List<PatternResponse> patterns) {

    /**
     * A decomposition pattern paired with its possible reassembly responses.
     * The index tracks which reassembly to use next (round-robin).
     */
    public static class PatternResponse {

        private final String decomposition;
        private final List<String> reassemblies;
        private int index;

        public PatternResponse(String decomposition, List<String> reassemblies) {
            this.decomposition = decomposition;
            this.reassemblies = reassemblies;
            this.index = 0;
        }

        public String decomposition() {
            return decomposition;
        }

        public String nextReassembly() {
            String response = reassemblies.get(index);
            index = (index + 1) % reassemblies.size();
            return response;
        }
    }
}
