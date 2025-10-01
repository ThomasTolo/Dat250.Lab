<script>
  // Confetti now handled globally via App.svelte (svelte-confetti)
  // Unique voter aggregation: latest vote per user per option; display number of unique voters and up-ratio percentage
  import { tweened } from 'svelte/motion';
  import { cubicOut } from 'svelte/easing';
  import { get } from 'svelte/store';
  // (Animation for voter count removed for immediacy; can be re-added if needed)

  function latestVotesForOption(optionId) {
    const key = String(optionId);
    const optionVotes = votes.filter(v => String(v.optionId ?? (v.option && v.option.id)) === key);
    const latest = new Map(); // userKey -> vote
    for (const v of optionVotes) {
      const userKey = v.voterUserId != null ? Number(v.voterUserId) : 'anon';
      const existing = latest.get(userKey);
      if (!existing || (v.publishedAt && v.publishedAt > existing.publishedAt)) {
        latest.set(userKey, v);
      }
    }
    return [...latest.values()];
  }

  function getCounts(optionId) {
    const list = latestVotesForOption(optionId);
    let up = 0, down = 0;
    for (const v of list) {
      const isUp = v.upvote === true || String(v.upvote) === 'true';
      if (isUp) up++; else down++;
    }
    return { up, down, total: up + down };
  }

  function getPercent(optionId) {
    const { up, total } = getCounts(optionId);
    if (total === 0) return 0;
    return Math.round((up / total) * 100); // pure up-ratio: 1 up =100%, 1 up 1 down =50%
  }

  function getBreakdown(optionId) {
    const { up, down, total } = getCounts(optionId);
    return { up, down, total, upPercent: total ? (up / total * 100) : 0, downPercent: total ? (down / total * 100) : 0 };
  }

  function getTotalVoters(optionId) {
    return getCounts(optionId).total;
  }

  function isUpMajority(optionId) {
    const { up, down } = getCounts(optionId);
    return up >= down; // ties green
  }

  // Removed animated voter total syncing for immediate value updates

  function getUserVote(optionId) {
    const uid = voterUserId ? Number(voterUserId) : null;
    const key = String(optionId);
    const optionVotes = votes.filter(v => String(v.optionId ?? (v.option && v.option.id)) === key);
    let latest = null;
    for (const v of optionVotes) {
      const sameUser = uid != null ? Number(v.voterUserId) === uid : v.voterUserId == null;
      if (!sameUser) continue;
      if (!latest || (v.publishedAt && v.publishedAt > latest.publishedAt)) latest = v;
    }
    if (latest && typeof latest.upvote === 'string') latest.upvote = latest.upvote === 'true';
    return latest;
  }
  // Anonymous User Handling: Generate a new UUID for each browser session

  import { createEventDispatcher, onMount } from 'svelte';
  // (imports moved above)
  const dispatch = createEventDispatcher();

  // voterUserId is now passed as a prop from App.svelte

  // Always fetch votes on mount and whenever poll.id changes
  let lastPollId = '';
  onMount(() => {
      fetchVotes();
      lastPollId = poll.id;
      // Notify parent we are ready so it can register our delta handler without relying on bind:this
      dispatch('ready', { id: poll.id, handleDelta });
    });
  $: if (poll && poll.id && poll.id !== lastPollId) {
    fetchVotes();
    lastPollId = poll.id;
  }
  // (removed net vote logic)
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
    const hadPrior = votes.some(v => Number(v.voterUserId) === userId);
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
      // optimistic local append to speed perceived response
      votes = [...votes, { optionId, voterUserId: userId, anonymous: !userId, upvote: isUpvote, publishedAt: new Date().toISOString() }];
      if (!hadPrior) dispatch('firstVote');
      dispatch('voted');
    } catch (e) {
      await fetchVotes(); // fallback refresh
      alert('Feil ved stemming');
    }
  }

  // Handle inbound vote-delta WebSocket messages from parent (App passes through or global listener modifies votes)
  export let handleDelta = (delta) => {
    if (!delta || delta.type !== 'vote-delta') return;
    if (String(delta.pollId) !== String(poll.id)) return;
    // Replace existing vote by same voter + option OR append
    let replaced = false;
    votes = votes.map(v => {
      if ((v.voterUserId === delta.voterUserId) && (v.optionId === delta.optionId)) { replaced = true; return { ...v, upvote: delta.upvote, publishedAt: new Date(delta.ts).toISOString() }; }
      return v;
    });
    if (!replaced) {
      votes = [...votes, { optionId: delta.optionId, voterUserId: delta.voterUserId, anonymous: delta.voterUserId == null, upvote: delta.upvote, publishedAt: new Date(delta.ts).toISOString() }];
    }
  };
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
          <div class="option-text">
            {option.caption || option.text || '—'}
          </div>
          <div class="vote-buttons">
            <button class="upvote" type="button" on:click={() => vote(i, true)}>Upvote</button>
            <button class="downvote" type="button" on:click={() => vote(i, false)}>Downvote</button>
          </div>
          <div class="votes">
            <span>{getTotalVoters(option.id)} {getTotalVoters(option.id) === 1 ? 'voter' : 'voters'}</span>
            <span>{getPercent(option.id)}%</span>
            {#if getBreakdown(option.id).total > 0}
              <span class="breakdown">{getBreakdown(option.id).up}↑ / {getBreakdown(option.id).down}↓</span>
            {/if}
            {#if getUserVote(option.id)}
              <span>You {getUserVote(option.id).upvote ? 'upvoted' : 'downvoted'}</span>
            {/if}
          </div>
          <div class="progress-shell">
            {#if getBreakdown(option.id).total > 0}
              {#key 'bar-' + option.id + '-' + getBreakdown(option.id).total}
              <div class="progress-split">
                <div class="up" style={`--w:${getBreakdown(option.id).upPercent}%`}></div>
                <div class="down" style={`--w:${getBreakdown(option.id).downPercent}%`}></div>
              </div>
              {/key}
            {:else}
              <div class="progress-fill neutral" style="--w:0%"></div>
            {/if}
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
/* Removed emoji & thumbnail styles */
/* Majority bar colors */
.progress-split .up { background: linear-gradient(90deg,#19c37d,#11a36a); }
.progress-split .down { background: linear-gradient(90deg,#d53543,#a90f24); }
.progress-fill.neutral { background: rgba(255,255,255,0.15); }
.progress-split { position:relative; width:100%; height:6px; background:rgba(255,255,255,0.12); border-radius:4px; overflow:hidden; }
.progress-split .up { position:absolute; left:0; top:0; bottom:0; width:var(--w); background: linear-gradient(90deg,#19c37d,#11a36a); }
.progress-split .down { position:absolute; right:0; top:0; bottom:0; width:var(--w); background: linear-gradient(90deg,#d53543,#a90f24); }
.votes .breakdown { color: var(--text-secondary); font-size:.65rem; letter-spacing:.5px; }
</style>
