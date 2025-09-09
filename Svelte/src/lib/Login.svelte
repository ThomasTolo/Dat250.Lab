<script>
  // --- Login Component: Handles user login/registration ---
  // --- Register Component: Handles user registration ---
  let username = '';
  let error = '';

  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();

  function getOrCreateVoterId(username) {
    const key = `voterId_${username}`;
    let id = localStorage.getItem(key);
    if (!id) {
      id = generateUUID();
      localStorage.setItem(key, id);
    }
    return id;
  }

  function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  async function register() {
    if (!username) {
      error = 'Username required';
      return;
    }
    // Always let user in, even if backend returns error
    try {
      const res = await fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password: '', email: '' })
      });
      const voterUserId = getOrCreateVoterId(username);
      dispatch('login', { username, voterUserId });
    } catch (e) {
      const voterUserId = getOrCreateVoterId(username);
      dispatch('login', { username, voterUserId });
    }
  }
</script>

<div class="login-box">
  <h2>Login</h2>
  <input type="text" bind:value={username} placeholder="Username" />
  <button on:click={register}>Login</button>
  {#if error}
    <div class="error">{error}</div>
  {/if}
</div>

<style>
.login-box {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 1em;
  max-width: 350px;
  margin: 2em auto;
  background: #181818;
  color: #fff;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
}
.login-box input {
  display: block;
  width: 100%;
  margin-bottom: 0.5em;
  padding: 0.5em;
  background: #222;
  color: #fff;
  border: 1px solid #2196f3;
  border-radius: 4px;
}
.login-box button {
  background: #2196f3;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.7em 1.5em;
  font-size: 1em;
  font-weight: 600;
  margin-top: 1em;
  cursor: pointer;
  transition: background 0.2s;
}
.login-box button:hover {
  background: #1769aa;
}
.error {
  color: #f44336;
  margin-top: 0.5em;
}
</style>
