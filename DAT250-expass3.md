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

Link to Code

[GitHub Repository - DAT250.Lab](https://github.com/ThomasTolo/Dat250.Lab)

Pending Issues

- Did not implement the optional microservice/reverse-proxy architecture (e.g., SvelteKit + Caddy) but understand the approach for future projects.

