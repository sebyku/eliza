import { useEliza } from './hooks/useEliza.js';
import LanguageSelector from './components/LanguageSelector.jsx';
import ChatWindow from './components/ChatWindow.jsx';
import './styles/app.css';

function detectLanguage() {
  const lang = (navigator.language || '').split('-')[0].toLowerCase();
  return lang === 'fr' ? 'fr' : 'us';
}

export default function App() {
  const {
    language,
    messages,
    isLoading,
    hasCrashed,
    uiMessages,
    sendMessage,
    switchLanguage,
    reset,
  } = useEliza(detectLanguage);

  const showCrashOverlay =
    hasCrashed && uiMessages?.crash && messages.length > 0 &&
    messages[messages.length - 1].text?.includes('PARITY ERROR');

  return (
    <div className="app">
      <header className="app-header">
        {uiMessages?.intro && (
          <pre className="intro-banner">{uiMessages.intro.trimEnd()}</pre>
        )}
        <LanguageSelector language={language} onSwitch={switchLanguage} />
      </header>

      {isLoading ? (
        <div className="loading">Loading...</div>
      ) : (
        <ChatWindow
          messages={messages}
          onSend={sendMessage}
          disabled={hasCrashed}
          prompt={uiMessages?.prompt?.trim() || '>'}
        />
      )}

      {showCrashOverlay && (
        <div className="crash-overlay">
          <div className="crash-content">
            <p className="crash-parity">PARITY ERROR!!! PARITY ERROR!!!</p>
            <p className="crash-parity">SESSION TERMINATED.</p>
            <div className="crash-lines">
              {uiMessages.crash.map((line, i) => (
                <p key={i}>{line}</p>
              ))}
            </div>
            <button
              className="crash-reboot"
              onClick={reset}
            >
              {uiMessages.reboot}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
