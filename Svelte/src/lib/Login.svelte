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

  function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  function getUsers() {
    return JSON.parse(localStorage.getItem('users') || '{}');
  }

  function saveUsers(users) {
    localStorage.setItem('users', JSON.stringify(users));
  }

  function handleLogin() {
    if (!username || !password) {
      error = 'Username and password required';
      return;
    }
    const users = getUsers();
    if (!users[username]) {
      error = 'User not found, try to register';
      return;
    }
    if (users[username].password !== password) {
      error = 'Incorrect password';
      return;
    }
    error = '';
    dispatch('login', { username, voterUserId: users[username].id });
  }

  function handleRegister() {
    if (!username || !password || !email) {
      error = 'All fields required';
      return;
    }
    const users = getUsers();
    if (users[username]) {
      error = 'Username already exists';
      return;
    }
    const id = generateUUID();
    users[username] = { id, password, email };
    saveUsers(users);
    error = '';
    dispatch('login', { username, voterUserId: id });
  }
</script>

<div class="login-box">
  <h2>{isLogin ? 'Login' : 'Register'}</h2>
  <div class="toggle-box">
    <button class:active={isLogin} on:click={() => { isLogin = true; error = ''; }}>Login</button>
    <button class:active={!isLogin} on:click={() => { isLogin = false; error = ''; }}>Register</button>
  </div>
  <input type="text" bind:value={username} placeholder="Username" />
  <input type="password" bind:value={password} placeholder="Password" />
  {#if !isLogin}
    <input type="email" bind:value={email} placeholder="Email" />
  {/if}
  <button on:click={isLogin ? handleLogin : handleRegister}>{isLogin ? 'Login' : 'Register'}</button>
  {#if error}
    <div class="error">{error}</div>
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
</style>
