<script>
  function getTotalVotes() {
    let total = 0;
    if (!poll.options) return 0;
    for (const option of poll.options) {
      total += Math.abs(getNetVotes(option.id));
    }
    return total;
  }
  function getVotePercent(optionId) {
    const total = getTotalVotes();
    if (total === 0) return 0;
    return Math.round((Math.abs(getNetVotes(optionId)) / total) * 100);
  }
  function getUserVote(optionId) {
    const optionVotes = votes.filter(v => (v.optionId ?? (v.option && v.option.id)) === optionId);
    // Match both anonymous and non-anonymous votes for this user
    const vote = optionVotes.find(v => {
      // If anonymous, voterUserId may be null
      if (v.anonymous) {
        return v.voterUserId === null;
      } else {
        return v.voterUserId === voterUserId;
      }
    });
    if (vote && typeof vote.upvote === 'string') {
      vote.upvote = vote.upvote === 'true';
    }
    return vote;
  }
  // Anonymous User Handling: Generate a new UUID for each browser session

  import { createEventDispatcher, onMount } from 'svelte';
  const dispatch = createEventDispatcher();

  // voterUserId is now passed as a prop from App.svelte

  // Always fetch votes on mount and whenever poll.id changes
  let lastPollId = '';
  onMount(() => {
    fetchVotes();
    lastPollId = poll.id;
  });
  $: if (poll && poll.id && poll.id !== lastPollId) {
    fetchVotes();
    lastPollId = poll.id;
  }
  // Vote Counting: Calculate net votes for each option, using only the latest vote per user
  function getNetVotes(optionId) {
    // Map: voterUserId (or 'anon') -> latest vote
    const optionVotes = votes.filter(v => (v.optionId ?? (v.option && v.option.id)) === optionId);
    const latestVotes = {};
    for (const v of optionVotes) {
      const key = v.voterUserId || 'anon';
      if (!latestVotes[key] || (v.publishedAt && v.publishedAt > latestVotes[key].publishedAt)) {
        latestVotes[key] = v;
      }
    }
    let net = 0;
    for (const k in latestVotes) {
      // Robustly handle both boolean and string values for upvote
      const up = latestVotes[k].upvote === true || String(latestVotes[k].upvote) === 'true';
      net += up ? 1 : -1;
    }
    return net;
  }
  // Logic for voting on a poll will go here
  // Poll Data: Default poll object to ensure safe rendering
  export let poll = {
    id: '',
    question: '',
    options: [],
    creatorUserId: ''
  };

  export let voterUserId = '';

  // State: Store all votes for the current poll
  let votes = [];

  // API Integration: Fetch votes for the current poll from backend
  async function fetchVotes() {
    try {
      // Ensure poll.id and voterUserId are numbers (Long)
      const pollId = Number(poll.id);
      const userId = Number(voterUserId);
      const res = await fetch(`/api/polls/${pollId}/votes?userId=${userId}`);
      if (!res.ok) throw new Error('Failed to fetch votes');
      votes = await res.json();
      console.log('Fetched votes for poll', pollId, votes);
    } catch (e) {
      votes = [];
      console.error('Error fetching votes', e);
    }
  }

  // Svelte Lifecycle: Fetch votes when component mounts
  onMount(fetchVotes);


  // Poll Management: Delete the current poll via backend API
  async function deletePoll() {
    if (poll.creatorUserId !== voterUserId) {
      alert('Only the creator can delete this poll');
      return;
    }
    if (!confirm('Er du sikker på at du vil slette denne poll-en?')) return;
    try {
      const res = await fetch(`/api/polls/${poll.id}?userId=${voterUserId}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        dispatch('pollDeleted');
      } else {
        alert('Feil ved sletting av poll');
      }
    } catch (e) {
      alert('Feil ved sletting av poll');
    }
  }
  // Voting: Send upvote/downvote for a poll option to backend
  async function vote(index, isUpvote) {
    if (!voterUserId) {
      alert('You must be logged in to vote');
      return;
    }
    if (poll.validUntil && new Date() > new Date(poll.validUntil)) {
      alert('Voting is closed for this poll');
      return;
    }
    if (!poll.options || !poll.options[index]) {
      alert('Alternativ mangler!');
      return;
    }
    try {
      // Ensure poll.id, optionId, and voterUserId are numbers (Long)
  const pollId = Number(poll.id);
  const optionId = Number(poll.options[index].id);
  const userId = Number(voterUserId);
      const res = await fetch(`/api/polls/${pollId}/votes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          optionId,
          voterUserId: userId,
          anonymous: !userId,
          isUpvote
        })
      });
      if (!res.ok) throw new Error('Failed to vote');
      await fetchVotes();
      dispatch('voted');
    } catch (e) {
      await fetchVotes();
      alert('Feil ved stemming');
    }
  }
</script>

<div class="poll fade-in">
  <div class="poll-header">
    <span class="poll-id">P#{poll.id || '—'}</span>
    <div class="poll-question">{poll.question || 'Untitled poll'}</div>
    <div class="poll-actions">
      {#if Number(poll.creatorUserId) === Number(voterUserId)}
        <button class="delete-poll" type="button" on:click={deletePoll} aria-label="Delete poll">Delete</button>
      {/if}
    </div>
  </div>
  <div class="poll-options">
    {#if poll.options && poll.options.length > 0}
      {#each poll.options as option, i (option.id)}
        {#key option.id}
        <div class="poll-option-row fade-in">
          <div class="option-text">{option.caption || option.text || '—'}</div>
          <div class="vote-buttons">
            <button class="upvote" type="button" on:click={() => vote(i, true)}>Upvote</button>
            <button class="downvote" type="button" on:click={() => vote(i, false)}>Downvote</button>
          </div>
          <div class="votes">
            <span>{getNetVotes(option.id)}{Math.abs(getNetVotes(option.id)) === 1 ? ' vote' : ' votes'}</span>
            <span>{getVotePercent(option.id)}%</span>
            {#if getUserVote(option.id)}
              <span>You {getUserVote(option.id).upvote ? 'upvoted' : 'downvoted'}</span>
            {/if}
          </div>
          <div class="progress-shell">
            <div class="progress-fill" style={`--w:${getVotePercent(option.id)}%`}></div>
          </div>
        </div>
        {/key}
      {/each}
    {:else}
      <div class="poll-option-row">No options available.</div>
    {/if}
  </div>
  <div class="poll-deadline">Deadline: {poll.validUntil ? new Date(poll.validUntil).toLocaleString() : 'N/A'}</div>
</div>

<style>
/* Removed old styles in favor of global system; keep minimal overrides if needed */
.delete-poll { background: linear-gradient(92deg, var(--danger), #7d0a18); color:#fff; }
.delete-poll:hover { filter: brightness(1.05); }
</style>
