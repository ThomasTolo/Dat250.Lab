<script>
  // Voting Logic: Get the current user's vote for a poll option 
  function getUserVote(optionId) {
    const optionVotes = votes.filter(v => v.optionId === optionId);
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
  function generateUUID() {
    // RFC4122 version 4 compliant UUID
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }
  import { createEventDispatcher, onMount } from 'svelte';
  const dispatch = createEventDispatcher();

  let voterUserId = '';
  // Get voterUserId from localStorage using username
  $: {
    const username = localStorage.getItem('username');
    if (username) {
      const key = `voterId_${username}`;
      voterUserId = localStorage.getItem(key) || '';
    }
  }
  import { afterUpdate } from 'svelte';

  let lastPollId = '';
  let lastVoterUserId = '';

  afterUpdate(() => {
    if (poll && poll.id && voterUserId) {
      if (poll.id !== lastPollId || voterUserId !== lastVoterUserId) {
        fetchVotes();
        lastPollId = poll.id;
        lastVoterUserId = voterUserId;
      }
    }
  });

  // Also fetch votes reactively when voterUserId changes
  $: if (poll && poll.id && voterUserId) {
    fetchVotes();
  }
  // Vote Counting: Calculate net votes for each option, using only the latest vote per user
  function getNetVotes(optionId) {
    // Map: voterUserId (or 'anon') -> latest vote
    const optionVotes = votes.filter(v => v.optionId === optionId);
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
    options: []
  };


  // State: Store all votes for the current poll
  let votes = [];

  // API Integration: Fetch votes for the current poll from backend
  async function fetchVotes() {
    try {
  const res = await fetch(`/api/polls/${poll.id}/votes`);
      if (res.ok) {
        votes = await res.json();
      } else {
        votes = [];
        console.error('Failed to fetch votes');
      }
    } catch (e) {
      votes = [];
      console.error('Error fetching votes', e);}
  }

  // Svelte Lifecycle: Fetch votes when component mounts
  onMount(fetchVotes);


  // Poll Management: Delete the current poll via backend API
  async function deletePoll() {
    if (!confirm('Er du sikker på at du vil slette denne poll-en?')) return;
      try {
  const res = await fetch(`/api/polls/${poll.id}`, {
          method: 'DELETE'
        });
        if (res.ok) {
          dispatch('pollDeleted');
        } else {
          alert('Kunne ikke slette poll');
        }
      } catch (e) {
        alert('Feil ved sletting av poll');
      }
    }
  // Voting: Send upvote/downvote for a poll option to backend
  async function vote(index, isUpvote) {
    if (!poll.options || !poll.options[index]) {
      alert('Alternativ mangler!');
      return;
    }
    const payload = {
      optionId: poll.options[index].id,
      voterUserId,
      anonymous: false, // ensure votes are tracked per user
      isUpvote: isUpvote // true for upvote, false for downvote
    };
    try {
  const res = await fetch(`/api/polls/${poll.id}/votes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      await fetchVotes();
      dispatch('voted');
      if (!res.ok) {
        alert('Kunne ikke stemme');
      }
    } catch (e) {
      await fetchVotes();
      alert('Feil ved stemming');
    }
  }
</script>

<div class="poll">
  <div class="poll-header">
    <span class="poll-id">Poll#{poll.id}</span>
    <div class="poll-question">"{poll.question}"</div>
    <div class="poll-actions">
      <button class="delete-poll" type="button" on:click={deletePoll}>Slett poll</button>
    </div>
  </div>
  <div class="poll-options">
    {#if poll.options && poll.options.length > 0}
      {#each poll.options as option, i}
        <div class="poll-option-row">
          <div class="option-text">{option.text || option.caption}</div>
          <div class="vote-buttons">
            <button class="upvote" type="button" on:click={() => vote(i, true)}>upvote</button>
            <button class="downvote" type="button" on:click={() => vote(i, false)}>downvote</button>
          </div>
          <div class="votes">
            <span style="color:#2196f3; font-weight:bold">
              {getNetVotes(option.id)}{Math.abs(getNetVotes(option.id)) === 1 ? ' Vote' : ' Votes'}
            </span>
            {#if getUserVote(option.id)}
              <span style="margin-left:1em; color:#888; font-size:0.95em;">
                You {getUserVote(option.id).upvote ? 'upvoted' : 'downvoted'}
              </span>
            {/if}
          </div>
        </div>
      {/each}
    {:else}
      <div class="poll-option-row">Ingen alternativer tilgjengelig.</div>
    {/if}
  </div>
</div>

<style>
.poll {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 1em;
  max-width: 500px;
  margin: 2em auto;
}
.poll-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1em;
}
.poll-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
}
.poll-id {
  background: #2196f3;
  color: #fff;
  padding: 0.3em 1em;
  border-radius: 8px 8px 0 0;
  font-weight: bold;
  margin-right: 1em;
}
.poll-question {
  background: #fff3cd;
  color: #856404;
  padding: 0.5em 1em;
  border-radius: 8px;
  font-style: italic;
}
.poll-options {
  margin-top: 1em;
}
.poll-option-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  color: #222;
  border-radius: 8px;
  margin-bottom: 0.5em;
  padding: 0.5em 1em;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.option-text {
  flex: 2;
  font-size: 1.1em;
  font-weight: 500;
  color: #222;
}
.vote-buttons {
  display: flex;
  gap: 0.5em;
}
.upvote, .downvote {
  min-width: 90px;
  padding: 0.3em 0;
  font-size: 1em;
  font-weight: 600;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}
.upvote {
  background: #4caf50;
  color: #fff;
  box-shadow: 0 2px 8px rgba(76,175,80,0.15);
}
.downvote {
  background: #f44336;
  color: #fff;
  box-shadow: 0 2px 8px rgba(244,67,54,0.15);
}
.votes {
  flex: 1;
  text-align: right;
  color: #2196f3;
  font-weight: bold;
  font-size: 1em;
}
.upvote {
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 4px;
  margin-right: 0.5em;
  padding: 0.3em 1em;
}
.downvote {
  background: #f44336;
  color: #fff;
  border: none;
  border-radius: 4px;
  margin-right: 0.5em;
  padding: 0.3em 1em;
}
.votes {
  color: #2196f3;
  font-weight: bold;
}
</style>
