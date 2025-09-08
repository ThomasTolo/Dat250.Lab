<script>
	import CreatePoll from './lib/CreatePoll.svelte';
	import Poll from './lib/Poll.svelte';

	// Example poll data for Poll component
	let poll = {
		id: 42,
		question: 'Pineapple on Pizza: Is it ok?',
		options: [
			{ text: 'Oh yammy!', votes: 2 },
			{ text: 'Mamma Mia! Hell no!', votes: 12 },
			{ text: 'IDK... I do not like pizza acutally...', votes: 1 }
		]
	};
</script>


<main class="main-layout">
	<h1 class="app-title">PollApp</h1>
	<div class="components-wrapper">
		<CreatePoll />
		<Poll {poll} />
	</div>
</main>
