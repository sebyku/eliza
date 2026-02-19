import { useState, useRef, useEffect } from 'react';
import MessageBubble from './MessageBubble.jsx';

/**
 * Scrollable message list with an input form at the bottom.
 */
export default function ChatWindow({ messages, onSend, disabled, prompt }) {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef(null);

  // Auto-scroll to bottom on new message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  function handleSubmit(e) {
    e.preventDefault();
    if (!input.trim() || disabled) return;
    onSend(input);
    setInput('');
  }

  return (
    <div className="chat-window">
      <div className="chat-messages">
        {messages.map((msg, i) => (
          <MessageBubble key={i} sender={msg.sender} text={msg.text} />
        ))}
        <div ref={messagesEndRef} />
      </div>
      <form className="chat-input" onSubmit={handleSubmit}>
        <span className="input-prompt">{prompt || '>'}</span>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={disabled}
          autoFocus
          placeholder={disabled ? '' : '...'}
        />
      </form>
    </div>
  );
}
