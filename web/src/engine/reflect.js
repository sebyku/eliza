/**
 * Pronoun reflection for captured text.
 * Mirrors the Java Eliza.reflect() method.
 */

/**
 * Reflect pronouns in captured text using the reflections map.
 * Keys in the reflections map are already accent-stripped at load time.
 */
export function reflectText(text, reflections) {
  const words = text.split(/\s+/);
  return words
    .map((word) => {
      const lower = word.toLowerCase();
      if (reflections.has(lower)) {
        return reflections.get(lower);
      }
      return word;
    })
    .join(' ');
}
