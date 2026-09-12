"use client";

import { FormEvent, useEffect, useState } from "react";

// Local Next.js development still calls Spring Boot on port 8080. The Render
// Docker build sets this to an empty string, so production calls same-origin /api.
const API_URL = process.env.NEXT_PUBLIC_API_URL ?? (process.env.NODE_ENV === "production" ? "" : "http://localhost:8080");

type LabItem = { id: number; title: string; note: string; createdAt: string };
type User = { authenticated: boolean; username: string };
type Status = { backend: string; database: string; itemCount: number; time: string };

// Every request includes the browser's session cookie so Spring Security can identify the user.
async function api(path: string, options: RequestInit = {}) {
  const response = await fetch(`${API_URL}${path}`, { credentials: "include", ...options });
  if (!response.ok) throw new Error((await response.json().catch(() => null))?.message ?? `Request failed (${response.status})`);
  return response.status === 204 ? null : response.json();
}

export default function Home() {
  const [items, setItems] = useState<LabItem[]>([]);
  const [status, setStatus] = useState<Status | null>(null);
  const [user, setUser] = useState<User>({ authenticated: false, username: "guest" });
  const [apiMessage, setApiMessage] = useState("Click the button to call Spring Boot.");
  const [protectedMessage, setProtectedMessage] = useState("");
  const [title, setTitle] = useState("");
  const [note, setNote] = useState("");
  const [username, setUsername] = useState("demo");
  const [password, setPassword] = useState("demo123");
  const [error, setError] = useState("");

  const refresh = async () => {
    try {
      const [nextStatus, nextItems, nextUser] = await Promise.all([api("/api/status"), api("/api/items"), api("/api/auth/me")]);
      setStatus(nextStatus); setItems(nextItems); setUser(nextUser);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Could not reach the backend.");
    }
  };

  useEffect(() => { void refresh(); }, []);

  async function addItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await api("/api/items", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ title, note }) });
      setTitle(""); setNote(""); setError(""); await refresh();
    } catch (reason) { setError(reason instanceof Error ? reason.message : "Could not create item."); }
  }

  async function editItem(item: LabItem) {
    const newTitle = window.prompt("New title", item.title);
    if (newTitle === null || !newTitle.trim()) return;
    const newNote = window.prompt("New note", item.note);
    if (newNote === null || !newNote.trim()) return;
    try {
      await api(`/api/items/${item.id}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ title: newTitle, note: newNote }) });
      await refresh();
    } catch (reason) { setError(reason instanceof Error ? reason.message : "Could not update item."); }
  }

  async function deleteItem(id: number) {
    if (!window.confirm("Delete this PostgreSQL record?")) return;
    try { await api(`/api/items/${id}`, { method: "DELETE" }); await refresh(); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Could not delete item."); }
  }

  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      // Spring Security's form-login endpoint expects URL-encoded username and password.
      await api("/api/auth/login", { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body: new URLSearchParams({ username, password }) });
      setError(""); await refresh();
    } catch (reason) { setError(reason instanceof Error ? reason.message : "Login failed."); }
  }

  async function logout() {
    try { await api("/api/auth/logout", { method: "POST" }); setProtectedMessage(""); await refresh(); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Logout failed."); }
  }

  return (
    <main>
      <header>
        <p className="eyebrow">NEXT.JS + SPRING BOOT LABORATORY</p>
        <h1>Full-Stack Test Drive</h1>
        <p>One page that makes each layer of your planned stack visible.</p>
      </header>

      {error && <p className="error">{error}</p>}
      <section className="status-grid" aria-label="Technology status">
        <article><h2>Frontend</h2><p className="ok">● Next.js + React + TypeScript</p><small>This HTML is rendered by a React component and styled with CSS.</small></article>
        <article><h2>Backend</h2><p className="ok">● {status?.backend ?? "Waiting for Spring Boot..."}</p><small>REST API: {API_URL}</small></article>
        <article><h2>Database</h2><p className="ok">● {status?.database ?? "Waiting for PostgreSQL..."}</p><small>{status ? `${status.itemCount} row(s) returned through JPA/Hibernate` : "No query yet"}</small></article>
        <article><h2>Authentication</h2><p className="ok">● {user.authenticated ? `Signed in as ${user.username}` : "Guest — protected route is locked"}</p><small>Spring Security uses a server-side session cookie.</small></article>
      </section>

      <section className="panel api-panel">
        <h2>REST API call</h2><p>Click to fetch a message from Java/Spring Boot.</p>
        <button onClick={() => api("/api/hello").then(data => setApiMessage(data.message)).catch(reason => setError(reason.message))}>Call GET /api/hello</button>
        <output>{apiMessage}</output>
      </section>

      <section className="two-column">
        <article className="panel">
          <h2>PostgreSQL CRUD</h2><p>Create, read, update, and delete records through REST → JPA → Hibernate → PostgreSQL.</p>
          <form onSubmit={addItem} className="item-form">
            <input aria-label="Item title" required maxLength={120} value={title} onChange={event => setTitle(event.target.value)} placeholder="Title" />
            <input aria-label="Item note" required maxLength={500} value={note} onChange={event => setNote(event.target.value)} placeholder="Note" />
            <button type="submit">Create record</button>
          </form>
          <ul className="items">
            {items.map(item => <li key={item.id}><div><strong>{item.title}</strong><span>{item.note}</span></div><div className="row-actions"><button onClick={() => editItem(item)}>Edit</button><button className="danger" onClick={() => deleteItem(item.id)}>Delete</button></div></li>)}
          </ul>
        </article>

        <article className="panel">
          <h2>Spring Security</h2>
          {!user.authenticated ? <form onSubmit={login} className="login-form">
            <label>Username<input value={username} onChange={event => setUsername(event.target.value)} /></label>
            <label>Password<input type="password" value={password} onChange={event => setPassword(event.target.value)} /></label>
            <button type="submit">Log in</button><small>Demo credentials: <code>demo</code> / <code>demo123</code></small>
          </form> : <div className="signed-in"><p>You are authenticated as <strong>{user.username}</strong>.</p><button onClick={logout}>Log out</button></div>}
          <div className="protected">
            <h3>Protected REST endpoint</h3><p>This button calls <code>GET /api/protected/message</code>. Spring Security rejects it until you log in.</p>
            <button disabled={!user.authenticated} onClick={() => api("/api/protected/message").then(data => setProtectedMessage(data.message)).catch(reason => setError(reason.message))}>Call protected endpoint</button>
            {protectedMessage && <output>{protectedMessage}</output>}
          </div>
        </article>
      </section>
    </main>
  );
}
