<script>
  // Poll Creation Component: Handles poll question and options input 
  let question = '';
  let options = [''];

  // Add Option: Add a new empty option field
  function addOption() {
    options = [...options, ''];
  }

  // Svelte Event Dispatcher: Notify parent when poll is created
  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();

  export let voterUserId = '';

  // Create Poll: Send new poll data to backend API
  async function createPoll() {
    // Filter out empty options before sending
    const filteredOptions = options
      .map((caption) => caption)
      .filter(opt => opt && opt.trim() !== '');

    try {
      const res = await fetch('/api/polls', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          creatorUserId: voterUserId,
          question,
          publicPoll: true,
          publishedAt: null,
          validUntil: null,
          maxVotesPerUser: null,
          invitedUsernames: [],
          options: filteredOptions.map((caption, i) => ({ caption, presentationOrder: i }))
        })
      });
      if (res.ok) {
        question = '';
        options = [''];
        dispatch('pollCreated');
      } else {
        alert('Failed to create poll');
      }
    } catch (e) {
      alert('Error creating poll');
    }
  }
</script>

<div class="create-poll">
  <h2>Create a New Poll</h2>
  <input type="text" bind:value={question} placeholder="Poll question" />
  <div>
    {#each options as option, i}
      <input type="text" bind:value={options[i]} placeholder={`Option ${i + 1}`} />
    {/each}
    <button on:click={addOption}>Add Option</button>
  </div>
  <button on:click={createPoll}>Create Poll</button>
</div>

<style>
.create-poll {
  border: 1px solid #ccc;
  padding: 1em;
  border-radius: 8px;
  max-width: 400px;
  margin: 2em auto;
}
.create-poll input {
  display: block;
  margin-bottom: 0.5em;
  width: 100%;
  padding: 0.5em;
}
.create-poll button {
  margin-top: 1em;
}
</style>
