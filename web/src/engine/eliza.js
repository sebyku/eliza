import jsyaml from 'js-yaml';
import { stripAccents, preprocess } from './preprocess.js';
import { reflectText } from './reflect.js';

const INSULT_THRESHOLD = 4;
const PARITY_ERROR = 'PARITY ERROR!!! PARITY ERROR!!! SESSION TERMINATED.';

/**
 * Async factory that creates an ELIZA engine for the given language.
 * Fetches and parses the rules and reflections YAML files.
 *
 * @param {string} language - "us" or "fr"
 * @returns {Promise<object>} engine with respond(), hasParityError(), reset()
 */
export async function createEliza(language) {
  const [rulesData, reflectionsData] = await Promise.all([
    fetch(`/rules_${language}.yaml`).then((r) => r.text()).then((t) => jsyaml.load(t)),
    fetch(`/reflections_${language}.yaml`).then((r) => r.text()).then((t) => jsyaml.load(t)),
  ]);

  // Build rules array with round-robin index per PatternResponse
  const rules = rulesData.rules.map((entry) => ({
    keyword: entry.keyword,
    priority: entry.priority,
    insult: entry.insult === true,
    patterns: entry.patterns.map((p) => ({
      decomposition: p.decomposition,
      reassemblies: p.reassemblies,
      index: 0,
    })),
  }));

  // Build reflections map with accent-stripped keys
  const reflections = new Map();
  for (const [key, value] of Object.entries(reflectionsData.reflections)) {
    reflections.set(stripAccents(key), value);
  }

  // Engine state
  let memory = [];
  let insultCount = 0;

  function nextReassembly(pr) {
    const response = pr.reassemblies[pr.index];
    pr.index = (pr.index + 1) % pr.reassemblies.length;
    return response;
  }

  function fillTemplate(template, match) {
    let result = template;
    for (let i = 1; i < match.length; i++) {
      const captured = match[i];
      if (captured != null) {
        const reflected = reflectText(captured.trim(), reflections);
        result = result.replace(`{${i}}`, reflected);
      }
    }
    return result;
  }

  function applyRule(rule, text) {
    for (const pr of rule.patterns) {
      const regex = new RegExp(stripAccents(pr.decomposition), 'i');
      const match = text.match(regex);
      if (match) {
        const template = nextReassembly(pr);

        // Memory-store directive
        if (template.startsWith('@memory:')) {
          const memoryTemplate = template.substring(8);
          const memResponse = fillTemplate(memoryTemplate, match);
          memory.push(memResponse);
          return null; // keep searching
        }

        return fillTemplate(template, match);
      }
    }
    return null;
  }

  function applyFallback() {
    for (const rule of rules) {
      if (rule.keyword === '@none') {
        for (const pr of rule.patterns) {
          return nextReassembly(pr);
        }
      }
    }
    return 'Please go on.';
  }

  function respond(input) {
    const text = preprocess(input);

    // Collect all matching rules, sorted by descending priority
    const matchingRules = rules.filter((rule) =>
      text.includes(stripAccents(rule.keyword))
    );
    matchingRules.sort((a, b) => b.priority - a.priority);

    // Try each matching rule in priority order
    let storedMemory = false;
    for (const rule of matchingRules) {
      const memorySizeBefore = memory.length;
      const response = applyRule(rule, text);
      storedMemory = storedMemory || memory.length > memorySizeBefore;
      if (response != null) {
        if (rule.insult) {
          insultCount++;
          if (insultCount >= INSULT_THRESHOLD) {
            return PARITY_ERROR;
          }
        }
        return response;
      }
    }

    // Try memory — but not if we just stored one this turn
    if (!storedMemory && memory.length > 0) {
      return memory.shift();
    }

    // Fallback
    return applyFallback();
  }

  function hasParityError() {
    return insultCount >= INSULT_THRESHOLD;
  }

  function reset() {
    memory = [];
    insultCount = 0;
    // Reset all round-robin indices
    for (const rule of rules) {
      for (const pr of rule.patterns) {
        pr.index = 0;
      }
    }
  }

  return { respond, hasParityError, reset };
}

export { PARITY_ERROR };
