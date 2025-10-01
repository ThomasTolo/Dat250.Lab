<script>
  // --- Login Component: Handles user login/registration ---
  // --- Register Component: Handles user registration ---
  let username = '';
  let password = '';
  let email = '';
  let error = '';
  let isLogin = true;

  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();



  async function handleLogin() {
    if (!username || !password) {
      error = 'Username and password required';
      return;
    }
    try {
      const res = await fetch('/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        error = 'Invalid username or password';
        return;
      }
      const user = await res.json();
      error = '';
  localStorage.setItem('userId', user.id);
  // Ensure user.id is a number (Long)
  user.id = Number(user.id);
  dispatch('login', { user });
    } catch (e) {
      error = 'Login failed';
    }
  }

  async function handleRegister() {
    if (!username || !password || !email) {
      error = 'All fields required';
      return;
    }
    try {
      const res = await fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email })
      });
      if (!res.ok) {
        const msg = await res.text();
        error = msg.includes('Username already exists') ? 'Username already exists' : 'Registration failed';
        return;
      }
      const user = await res.json();
      error = '';
  localStorage.setItem('userId', user.id);
  // Ensure user.id is a number (Long)
  user.id = Number(user.id);
  dispatch('login', { user });
    } catch (e) {
      error = 'Registration failed';
    }
  }
</script>

<div class="login-box fade-in" aria-labelledby="login-heading">
  <h2 id="login-heading">{isLogin ? 'Login' : 'Register'}</h2>
  <div class="toggle-box" role="tablist">
    <button class:active={isLogin} role="tab" aria-selected={isLogin} on:click={() => { isLogin = true; error = ''; }}>Login</button>
    <button class:active={!isLogin} role="tab" aria-selected={!isLogin} on:click={() => { isLogin = false; error = ''; }}>Register</button>
  </div>
  <div class="fields">
    <label class="sr-only" for="login-username">Username</label>
    <input id="login-username" type="text" bind:value={username} placeholder="Username" autocomplete="username" />
    <label class="sr-only" for="login-password">Password</label>
    <input id="login-password" type="password" bind:value={password} placeholder="Password" autocomplete="current-password" />
    {#if !isLogin}
      <label class="sr-only" for="login-email">Email</label>
      <input id="login-email" type="email" bind:value={email} placeholder="Email" autocomplete="email" />
    {/if}
  </div>
  <button on:click={isLogin ? handleLogin : handleRegister}>{isLogin ? 'Login' : 'Register'}</button>
  {#if error}
    <div class="error" role="alert">{error}</div>
  {/if}
</div>

<style>
.toggle-box {
  display: flex;
  justify-content: center;
  margin-bottom: 1em;
}
.toggle-box button {
  background: #222;
  color: #fff;
  border: 1px solid #2196f3;
  border-radius: 8px;
  padding: 0.5em 1em;
  margin: 0 0.5em;
  cursor: pointer;
  font-weight: 600;
}
.toggle-box button.active {
  background: #2196f3;
}
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
.fields { display:flex; flex-direction:column; gap:.55rem; }
.sr-only { position:absolute; width:1px; height:1px; padding:0; margin:-1px; overflow:hidden; clip:rect(0 0 0 0); white-space:nowrap; border:0; }
</style>
