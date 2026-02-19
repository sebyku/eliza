/**
 * Simple language toggle between English (us) and French (fr).
 */
export default function LanguageSelector({ language, onSwitch }) {
  return (
    <div className="language-selector">
      <button
        className={language === 'us' ? 'active' : ''}
        onClick={() => onSwitch('us')}
      >
        English
      </button>
      <button
        className={language === 'fr' ? 'active' : ''}
        onClick={() => onSwitch('fr')}
      >
        Français
      </button>
    </div>
  );
}
