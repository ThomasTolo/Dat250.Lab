DAT250 Experiment Assignment 6 Report - Thomas Tolo Jensen

## Technical Problems Encountered

During the completion of this assignment, I encountered several technical challenges:

- **Visual issue in frontend**
  - When I started writing the javascript code for the GUI, visual part of the Poll app. It was just that everything looked so unsymmetrical, the option-text/upvote/downvote and vote-count were not linear for each option. 
  Fix: Used flexbox and custom CSS to align option text, buttons, and vote count in a straight line.
- **When pressing upvote it returned downvote instead** 
  - The upvote button returned a downvote because the backend used the property name upvote, but the frontend checked for isUpvote (Thus returning False). 
  Fix: Changing the frontend to use upvote fixed the issue
- **Large File in Git History:**
  - Accidentally committed a large binary file over 100 MB, which caused GitHub to reject all pushes. This required cleaning the git history using `git filter-branch` and updating `.gitignore` to prevent future issues.
- **Automated Build/Deploy:**
  - Set up a `build.gradle.kts` in the Svelte project to automate building the frontend and copying static assets to the backend, but had to ensure build and dependency folders were not tracked by git.

- **Frontend/Backend Synchronization:**
  - The Svelte dev server (`localhost:5175`) and Spring Boot backend (`localhost:8080`) behaved differently: dev started at register, backend skipped login and allowed poll creation.
  - Fix: Ensured all fetch URLs are relative, added Vite proxy config to forward `/api` requests to backend, and made login state persistent only on logout.

- **Spring Boot Not Showing Latest Frontend:**
  - After updating Svelte code, changes were not reflected on `localhost:8080`.
  - Fix: Automated build and copy process using `./gradlew copyWebApp` in Svelte folder, followed by restarting Spring Boot backend.

- **LocalStorage State Issues:**
  - App sometimes started at the wrong page due to leftover localStorage state.
  - Fix: Added clear login state on logout and instructions to clear localStorage if needed.

- **API Proxy for Dev Server:**
  - Svelte dev server could not create polls due to CORS issues.
  - Fix: Added proxy config in `vite.config.js` to forward `/api` requests to Spring Boot backend.

- **Only Able to Vote for One Option (Latest Vote Count Issue):**
  - Initially, the backend logic deduplicated votes by user, so only the latest vote per user was counted for the entire poll. This meant users could only vote for one option per poll, and voting for a new option would overwrite their previous vote.
  - Fix: Changed the backend to return all votes for a poll, allowing users to vote for multiple options (but only once per option). Updated frontend logic to match this behavior.

**Link to Code For 1-2:**
[GitHub Repository - DAT250.Lab](https://github.com/ThomasTolo/Dat250.Lab)

**Pending Issues:**
- Did not implement the optional microservice/reverse-proxy architecture

