<script>
	// Main App Component: Imports poll creation, voting, and login components
	import CreatePoll from './lib/CreatePoll.svelte';
	import Poll from './lib/Poll.svelte';
	import Login from './lib/Login.svelte';
	// Using default export of svelte-confetti (named export may not exist depending on package version)
	import Confetti from "svelte-confetti";

	// State: List of polls, loading indicator, and logged-in user
		let polls = [];
		let loading = true;
		let user = null;
		let voterUserId = '';
		$: {
			if (user && user.id) {
				voterUserId = user.id;
			} else {
				voterUserId = '';
			}
		}

	// API Integration: Fetch all polls from backend (REST)
		async function fetchPolls(isInitial = false) {
			if (isInitial) loading = true;
				try {
					let url = '/api/polls';
					if (user && user.username) {
						url += `?username=${encodeURIComponent(user.username)}`;
					}
					const res = await fetch(url);
					if (!res.ok) throw new Error('Failed to fetch polls');
					polls = await res.json();
				} catch (e) {
					console.error('Failed to fetch polls', e);
				}
				if (isInitial) loading = false;
			}

	// Initial Data Load: Fetch polls when app starts (after login)
		$: if (user && user.id) fetchPolls(true);

		function handleLogin(event) {
			user = event.detail.user;
			fetchPolls(true);
		}

	let ws;
	let wsMessages = [];
	let pollComponents = new Map();

	function connectWebSocket() {
		ws = new WebSocket('ws://localhost:8080/rawws');
		ws.onopen = () => {
			ws.send('Hello from Svelte frontend!');
		};
		ws.onmessage = (event) => {
			let raw = event.data;
			wsMessages = [...wsMessages, raw];
			try {
				const parsed = JSON.parse(raw);
				if (parsed.type === 'vote-delta') {
					const target = pollComponents.get(parsed.pollId);
					if (target && target.handleDelta) {
						target.handleDelta(parsed);
					} else {
						// fallback: refresh only that poll
						fetchPolls();
					}
					return;
				} else if (parsed.type === 'poll-created') {
					fetchPolls(true);
					fireConfetti();
					return;
				}
			} catch (_) {
				// not JSON, continue legacy handling
			}
			if (raw === 'pollsUpdated') {
				fetchPolls(true);
			} else if (raw === 'votesUpdated') {
				fetchPolls();
			}
		};
		ws.onerror = (err) => {
			console.error('WebSocket error:', err);
		};
	}

	// Connect when app loads
	connectWebSocket();

	function registerPoll(el, id) {
		if (el) pollComponents.set(id, el);
		return {
			destroy() { pollComponents.delete(id); }
		}
	}

		let confettiRef;
		function fireConfetti(cfg = {}) {
			// svelte-confetti provides an imperative API via the component reference
			if (confettiRef && confettiRef.fire) confettiRef.fire({ particles: 120, ...cfg });
		}
</script>


<main class="main-layout">
		<div class="top-bar">
			<h1 class="app-title">PollApp</h1>
			{#if user}
				<button class="logout-btn" on:click={() => { user = null; }}>Log out</button>
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
			{#if !user}
				<Login on:login={handleLogin} />
			{:else}
				<h2 style="text-align:center; color:#fff; margin-bottom:1em;">Hello {user.username}!</h2>
				<CreatePoll on:pollCreated={() => { fetchPolls(true); fireConfetti({ particles: 180, spread: 70 }); }} voterUserId={voterUserId} />
				{#if loading}
					<p>Loading polls...</p>
				{:else}
					{#each polls as poll (poll.id)}
						<Poll {poll} voterUserId={voterUserId}
							on:ready={(e) => registerPoll(e.currentTarget, e.detail.id)}
							on:firstVote={() => fireConfetti({ particles: 90, spread: 55 })}
							on:voted={() => fetchPolls()} on:pollDeleted={() => fetchPolls(true)} />
					{/each}
				{/if}
			{/if}
		</div>
</main>
<Confetti bind:this={confettiRef} />
