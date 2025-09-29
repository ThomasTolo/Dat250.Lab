<script>
  let question = '';
  let options = ['', ''];
  let publicPoll = true;
  let publishedAt = '';
  let validUntil = '';
  let maxVotesPerUser = 1;
  let invitedUsernames = '';

  function addOption() {
    options = [...options, ''];
  }

  // Svelte Event Dispatcher: Notify parent when poll is created
  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();

  export let voterUserId = '';

  async function createPoll() {
    if (!voterUserId) {
      alert('You must be logged in to create a poll');
      return;
    }
    // Ensure creatorUserId is a number (Long)
    const creatorUserId = Number(voterUserId);
    // Filter out empty options before sending
    const filteredOptions = options
      .map((caption) => caption)
      .filter(opt => opt && opt.trim() !== '');
    if (filteredOptions.length < 2) {
      alert('Please provide at least two options');
      return;
    }
    const pollData = {
      creatorUserId,
      question,
      publicPoll,
      publishedAt: publishedAt ? new Date(publishedAt).toISOString() : null,
      validUntil: validUntil ? new Date(validUntil).toISOString() : null,
      maxVotesPerUser: publicPoll ? null : maxVotesPerUser,
      invitedUsernames: publicPoll ? [] : invitedUsernames.split(',').map(u => u.trim()).filter(u => u),
      options: filteredOptions.map((caption, i) => ({ caption, presentationOrder: i }) )
    };
    try {
      const res = await fetch(`/api/polls?userId=${voterUserId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(pollData)
      });
      if (res.ok) {
        question = '';
  options = ['', ''];
        publicPoll = true;
        publishedAt = '';
        validUntil = '';
        maxVotesPerUser = 1;
        invitedUsernames = '';
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
  <label><input type="checkbox" bind:checked={publicPoll} /> {publicPoll ? 'Public poll' : 'Private poll'}</label>
  <div>
    <label>Published at: <input type="datetime-local" bind:value={publishedAt} /></label>
    <label>Deadline: <input type="datetime-local" bind:value={validUntil} /></label>
  </div>
  {#if !publicPoll}
    <label>Max votes per user: <input type="number" min="1" bind:value={maxVotesPerUser} /></label>
    <label>Invited usernames (comma separated): <input type="text" bind:value={invitedUsernames} /></label>
  {/if}
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
