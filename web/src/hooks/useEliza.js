import { useState, useCallback, useRef, useEffect } from 'react';
import jsyaml from 'js-yaml';
import { createEliza } from '../engine/eliza.js';

/**
 * Custom hook that manages the ELIZA engine lifecycle.
 * Exposes sendMessage, switchLanguage, and conversation state.
 */
export function useEliza(initialLanguage = 'us') {
  const [language, setLanguage] = useState(initialLanguage);
  const [resetKey, setResetKey] = useState(0);
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasCrashed, setHasCrashed] = useState(false);
  const [uiMessages, setUiMessages] = useState(null);

  const engineRef = useRef(null);

  // Initialize engine and messages when language or resetKey changes
  useEffect(() => {
    let cancelled = false;

    async function init() {
      setIsLoading(true);
      setHasCrashed(false);
      setMessages([]);

      const [engine, msgData] = await Promise.all([
        createEliza(language),
        fetch(`${import.meta.env.BASE_URL}messages_${language}.yaml`)
          .then((r) => r.text())
          .then((t) => jsyaml.load(t)),
      ]);

      if (cancelled) return;

      engineRef.current = engine;
      setUiMessages(msgData);

      // Pick a random greeting
      const greetings = msgData.greetings;
      const greeting = greetings[Math.floor(Math.random() * greetings.length)];
      setMessages([{ sender: 'eliza', text: greeting }]);
      setIsLoading(false);
    }

    init();
    return () => { cancelled = true; };
  }, [language, resetKey]);

  const sendMessage = useCallback((text) => {
    if (!engineRef.current || hasCrashed) return;

    const trimmed = text.trim();
    if (!trimmed) return;

    // Check for quit words
    if (uiMessages?.quit_words?.some((w) => trimmed.toLowerCase() === w.toLowerCase())) {
      setMessages((prev) => [
        ...prev,
        { sender: 'user', text: trimmed },
        { sender: 'eliza', text: uiMessages.goodbye },
      ]);
      setHasCrashed(true); // disable further input
      return;
    }

    const response = engineRef.current.respond(trimmed);

    setMessages((prev) => [
      ...prev,
      { sender: 'user', text: trimmed },
      { sender: 'eliza', text: response },
    ]);

    if (engineRef.current.hasParityError()) {
      setHasCrashed(true);
    }
  }, [hasCrashed, uiMessages]);

  const switchLanguage = useCallback((lang) => {
    setLanguage(lang);
  }, []);

  const reset = useCallback(() => {
    setResetKey((k) => k + 1);
  }, []);

  return {
    language,
    messages,
    isLoading,
    hasCrashed,
    uiMessages,
    sendMessage,
    switchLanguage,
    reset,
  };
}
