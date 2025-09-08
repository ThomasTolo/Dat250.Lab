<script>
  // Logic for voting on a poll will go here
  export let poll = {
    id: 42,
    question: 'Pineapple on Pizza: Is it ok?',
    options: [
      { text: 'Oh yammy!', votes: 2 },
      { text: 'Mamma Mia! Hell no!', votes: 12 },
      { text: 'IDK... I do not like pizza acutally...', votes: 1 }
    ]
  };

  function upvote(index) {
    poll.options[index].votes += 1;
  }

  function downvote(index) {
    poll.options[index].votes -= 1;
  }
</script>

<div class="poll">
  <div class="poll-header">
    <span class="poll-id">Poll#{poll.id}</span>
    <div class="poll-question">"{poll.question}"</div>
  </div>
  <div class="poll-options">
    {#each poll.options as option, i}
      <div class="poll-option-row">
        <div class="option-text">{option.text}</div>
        <div class="vote-buttons">
          <button class="upvote" on:click={() => upvote(i)}>upvote</button>
          <button class="downvote" on:click={() => downvote(i)}>downvote</button>
        </div>
        <div class="votes">{option.votes} Votes</div>
      </div>
    {/each}
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
  margin-bottom: 1em;
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
