<script>
	// Main App Component: Imports poll creation, voting, and login components
	import CreatePoll from './lib/CreatePoll.svelte';
	import Poll from './lib/Poll.svelte';
	import Login from './lib/Login.svelte';

	// State: List of polls, loading indicator, and logged-in user
	let polls = [];
	let loading = true;
	let username = localStorage.getItem('username') || '';

	// API Integration: Fetch all polls from backend
	async function fetchPolls(isInitial = false) {
		if (isInitial) loading = true;
		try {
			const res = await fetch('/api/polls');
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
</script>


<main class="main-layout">
	<div class="top-bar">
		<h1 class="app-title">PollApp</h1>
		{#if username}
			<button class="logout-btn" on:click={() => { username = ''; localStorage.removeItem('username'); }}>Log out</button>
		{/if}
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
				{#each polls as poll}
					<Poll {poll} on:voted={() => fetchPolls()} on:pollDeleted={() => fetchPolls(true)} />
				{/each}
			{/if}
		{/if}
	</div>
</main>
