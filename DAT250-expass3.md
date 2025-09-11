DAT250 Experiment Assignment 6 Report - Thomas Tolo Jensen

## Technical Problems Encountered

- **Visual issue in frontend**
  - When I started writing the javascript code for the GUI, visual part of the Poll app. It was just that everything looked so unsymmetrical, the option-text/upvote/downvote and vote-count were not linear for each option. 
  Fix: Used flexbox and custom CSS to align option text, buttons, and vote count in a straight line.

- **When pressing upvote it returned downvote instead** 
  - The upvote button returned a downvote because the backend used the property name upvote, but the frontend checked for isUpvote (Thus returning False). 
  Fix: Changing the frontend to use upvote fixed the issue

- **Large File in Git History:**
  - Accidentally committed a large binary file over 100 MB, which caused GitHub to reject all pushes. 
  Fix: Clean the git history using `git filter-branch` and updating `.gitignore` to prevent future issues. (https://www.reddit.com/r/learnprogramming/comments/z09zhy/how_do_you_apply_gitignore_after_you_already/)

- **Spring Boot Not Showing Latest Frontend:**
  - After updating Svelte code, changes were not reflected on `localhost:8080`.
  - Fix: Automated build and copy process using `./gradlew copyWebApp` in Svelte folder, followed by restarting Spring Boot backend.

- **LocalStorage State Issues:**
  - App sometimes started at the wrong page due to leftover localStorage state.
  - Fix: Added clear login state on logout and instructions to clear localStorage if needed.

- **Only Able to Vote for One Option (Latest Vote Count Issue):**
  - Initially, the backend logic deduplicated votes by user, so only the latest vote per user was counted for the entire poll. This meant users could only vote for one option per poll, and voting for a new option would overwrite their previous vote.
  - Fix: Changed the backend to return all votes for a poll, allowing users to vote for multiple options (but only once per option). Updated frontend logic to match this behavior.

**Link to Code For 1-2:**
[GitHub Repository - DAT250.Lab](https://github.com/ThomasTolo/Dat250.Lab)

**Pending Issues:**
- Did not implement the optional microservice/reverse-proxy architecture

