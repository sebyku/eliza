/**
 * A single message bubble: user messages right-aligned, ELIZA left-aligned.
 */
export default function MessageBubble({ sender, text }) {
  const isUser = sender === 'user';
  return (
    <div className={`message ${isUser ? 'message-user' : 'message-eliza'}`}>
      {!isUser && <span className="message-label">ELIZA:</span>}
      <span className="message-text">{text}</span>
    </div>
  );
}
