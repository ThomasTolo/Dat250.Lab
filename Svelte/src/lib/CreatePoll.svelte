<script>
  // Logic for creating a new poll and its options will go here
  let question = '';
  let options = [''];

  function addOption() {
    options = [...options, ''];
  }

  function updateOption(index, value) {
    options[index] = value;
  }

  function createPoll() {
    // Emit or handle poll creation
    console.log('Poll created:', question, options);
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
