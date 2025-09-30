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

<div class="create-poll fade-in" aria-labelledby="create-poll-heading">
  <h2 id="create-poll-heading">Create a New Poll</h2>
  <div class="options-group">
    <label class="sr-only" for="poll-question">Question</label>
    <input id="poll-question" type="text" bind:value={question} placeholder="Poll question" autocomplete="off" />
    <div class="options-stack">
      {#each options as option, i}
        <input type="text" bind:value={options[i]} placeholder={`Option ${i + 1}`} />
      {/each}
      <button type="button" class="add-option" on:click={addOption}>+ Add Option</button>
    </div>
  </div>
  <div class="inline toggles">
    <label class="toggle">
      <input type="checkbox" bind:checked={publicPoll} />
      <span>{publicPoll ? 'Public poll' : 'Private poll'}</span>
    </label>
  </div>
  <div class="inline datetimes">
    <label>Published at
      <input type="datetime-local" bind:value={publishedAt} />
    </label>
    <label>Deadline
      <input type="datetime-local" bind:value={validUntil} />
    </label>
  </div>
  {#if !publicPoll}
    <div class="inline private-meta">
      <label>Max votes/user
        <input type="number" min="1" bind:value={maxVotesPerUser} />
      </label>
      <label>Invited usernames
        <input type="text" bind:value={invitedUsernames} placeholder="user1, user2" />
      </label>
    </div>
  {/if}
  <div class="actions">
    <button type="button" on:click={createPoll}>Create Poll</button>
  </div>
</div>

<style>
.sr-only { position:absolute; width:1px; height:1px; padding:0; margin:-1px; overflow:hidden; clip:rect(0 0 0 0); white-space:nowrap; border:0; }
.options-stack { display:flex; flex-direction:column; gap:.55rem; }
.inline { gap:1rem; }
.inline label { display:flex; flex-direction:column; font-size:.7rem; letter-spacing:.75px; text-transform:uppercase; color:var(--text-secondary); font-weight:600; gap:.4rem; }
.toggle { display:flex; align-items:center; gap:.55rem; font-size:.8rem; letter-spacing:.5px; }
.toggle input { width:auto; }
.actions { display:flex; justify-content:flex-end; margin-top:.75rem; }
.add-option { font-size:.7rem; letter-spacing:.75px; background:rgba(255,255,255,0.08); color:var(--accent); box-shadow:none; padding:.5em .9em; }
.add-option:hover { background:rgba(255,255,255,0.15); }
</style>
