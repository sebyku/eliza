/**
 * Language selector for English, French, German, and Spanish.
 */
export default function LanguageSelector({ language, onSwitch }) {
  const languages = [
    { code: 'us', label: 'English' },
    { code: 'fr', label: 'Français' },
    { code: 'de', label: 'Deutsch' },
    { code: 'es', label: 'Español' },
  ];

  return (
    <div className="language-selector">
      {languages.map(({ code, label }) => (
        <button
          key={code}
          className={language === code ? 'active' : ''}
          onClick={() => onSwitch(code)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
