/**
 * Text preprocessing for the ELIZA engine.
 * Handles accent stripping, ligature expansion, and input normalization.
 */

/**
 * Expand ligatures and strip diacritical marks from text.
 * Mirrors the Java Eliza.stripAccents() method.
 */
export function stripAccents(text) {
  let result = text
    .replace(/œ/g, 'oe')
    .replace(/Œ/g, 'OE')
    .replace(/æ/g, 'ae')
    .replace(/Æ/g, 'AE');
  // NFD normalize then strip combining diacritical marks
  result = result.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  return result;
}

/**
 * Preprocess user input: trim, strip trailing punctuation,
 * collapse whitespace, lowercase, and strip accents.
 */
export function preprocess(input) {
  return stripAccents(
    input
      .trim()
      .replace(/[.!,;]+$/, '')   // strip trailing punctuation
      .replace(/\s+/g, ' ')      // normalize whitespace
      .toLowerCase()
  );
}
