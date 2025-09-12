<script>
	// Main App Component: Imports poll creation, voting, and login components
	import CreatePoll from './lib/CreatePoll.svelte';
	import Poll from './lib/Poll.svelte';
	import Login from './lib/Login.svelte';

	// State: List of polls, loading indicator, and logged-in user
	let polls = [];
	let loading = true;
	let username = localStorage.getItem('username') || '';

	// API Integration: Fetch all polls from backend (REST)
	async function fetchPolls(isInitial = false) {
		if (isInitial) loading = true;
		try {
			const res = await fetch('/api/polls');
			if (!res.ok) throw new Error('Failed to fetch polls');
			polls = await res.json();
		} catch (e) {
			console.error('Failed to fetch polls', e);
		}
		if (isInitial) loading = false;
	}

	// Initial Data Load: Fetch polls when app starts (after login)
	$: if (username) fetchPolls(true);

	function handleLogin(event) {
		username = event.detail.username;
		localStorage.setItem('username', username);
	}

	let ws;
	let wsMessages = [];

	function connectWebSocket() {
		ws = new WebSocket('ws://localhost:8080/rawws');
		ws.onopen = () => {
			ws.send('Hello from Svelte frontend!');
		};
		ws.onmessage = (event) => {
			wsMessages = [...wsMessages, event.data];
			console.log('WebSocket received:', event.data);
			if (event.data === 'pollsUpdated' || event.data === 'votesUpdated') {
				fetchPolls();
			}
		};
		ws.onerror = (err) => {
			console.error('WebSocket error:', err);
		};
	}

	// Connect when app loads
	connectWebSocket();
</script>


<main class="main-layout">
	<div class="top-bar">
		<h1 class="app-title">PollApp</h1>
		{#if username}
			<button class="logout-btn" on:click={() => { username = ''; localStorage.removeItem('username'); }}>Log out</button>
		{/if}
	</div>
	<!-- WebSocket Messages Display -->
	<div class="ws-messages">
		<h2>WebSocket Messages</h2>
		<ul>
			{#each wsMessages as msg}
				<li>{msg}</li>
			{/each}
		</ul>
	</div>
	<div class="components-wrapper">
		{#if !username}
			<Login on:login={handleLogin} />
		{:else}
			<h2 style="text-align:center; color:#fff; margin-bottom:1em;">Hello {username}!</h2>
			<CreatePoll on:pollCreated={() => fetchPolls(true)} />
			{#if loading}
				<p>Loading polls...</p>
			{:else}
				{#each polls as poll (poll.id)}
					<Poll {poll} on:voted={() => fetchPolls()} on:pollDeleted={() => fetchPolls(true)} />
				{/each}
			{/if}
		{/if}
	</div>
</main>
