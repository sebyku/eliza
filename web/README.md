# ELIZA Web

A React/JavaScript port of the ELIZA chatbot with a retro terminal UI.

Based on Joseph Weizenbaum's 1966 program, this web version faithfully reproduces the Java engine's behavior: keyword-based pattern matching, pronoun reflection, memory queue, round-robin responses, and the infamous parity error.

## Features

- **Dual language** — English and French, with automatic detection from browser locale
- **Accent-insensitive matching** — ligature expansion (oe, ae) and diacritical stripping for French input
- **Memory system** — ELIZA stores certain responses for later recall
- **Parity error** — 4 insults trigger a fake crash overlay with reboot button
- **Retro terminal styling** — dark background, green/amber monospace text

## Quick start

```bash
npm install
npm run dev
```

## Production build

```bash
npm run build
npm run preview
```

## Project structure

```
public/
  rules_us.yaml           # English conversation rules
  rules_fr.yaml           # French conversation rules
  reflections_us.yaml     # English pronoun reflections
  reflections_fr.yaml     # French pronoun reflections
  messages_us.yaml        # English UI strings (intro, greetings, crash lines)
  messages_fr.yaml        # French UI strings
src/
  main.jsx                # React entry point
  App.jsx                 # Top-level state, language switching, crash overlay
  engine/
    preprocess.js          # stripAccents(), preprocess()
    reflect.js             # reflectText() — pronoun reflection
    eliza.js               # createEliza() factory — full engine logic
  components/
    ChatWindow.jsx         # Message list + input bar
    MessageBubble.jsx      # Single message (user vs ELIZA styling)
    LanguageSelector.jsx   # English/French toggle
  hooks/
    useEliza.js            # Engine lifecycle, sendMessage, switchLanguage
  styles/
    app.css                # Retro terminal theme
```

## Tech stack

- [Vite](https://vite.dev/) + [React](https://react.dev/) (vanilla JavaScript)
- [js-yaml](https://github.com/nodeca/js-yaml) for loading YAML rules at runtime
