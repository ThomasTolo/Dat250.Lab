
<script>
	// --- Main App Component: Imports poll creation and voting components ---
	import CreatePoll from './lib/CreatePoll.svelte';
	import Poll from './lib/Poll.svelte';

	// --- State: List of polls and loading indicator ---
	let polls = [];
	let loading = true;

	// --- API Integration: Fetch all polls from backend ---
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

	// --- Initial Data Load: Fetch polls when app starts ---
	fetchPolls(true);
</script>


<main class="main-layout">
	<h1 class="app-title">PollApp</h1>
	<div class="components-wrapper">
		<CreatePoll on:pollCreated={() => fetchPolls(true)} />
		{#if loading}
			<p>Loading polls...</p>
		{:else}
			{#each polls as poll}
				<Poll {poll} on:voted={() => fetchPolls()} on:pollDeleted={() => fetchPolls(true)} />
			{/each}
		{/if}
	</div>
</main>
