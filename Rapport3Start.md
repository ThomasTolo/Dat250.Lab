DAT250 Experiment Assignment 6 Report

## Technical Problems Encountered

During the completion of this assignment, I encountered several technical challenges:

- **When pressing upvote it returned downvote instead** 
  - The upvote button returned a downvote because the backend used the property name upvote, but the frontend checked for isUpvote (Thus returning False). Changing the frontend to use upvote fixed the issue
- **Large File in Git History:**
  - Accidentally committed a large binary file over 100 MB, which caused GitHub to reject all pushes. This required cleaning the git history using `git filter-branch` and updating `.gitignore` to prevent future issues.
- **Frontend/Backend Integration:**
  - Needed to update all fetch URLs in the Svelte frontend from absolute (`http://localhost:8080/...`) to relative (`/api/...`) paths for production deployment with Spring Boot.
- **Automated Build/Deploy:**
  - Set up a `build.gradle.kts` in the Svelte project to automate building the frontend and copying static assets to the backend, but had to ensure build and dependency folders were not tracked by git.

Link to Code

[GitHub Repository - DAT250.Lab](https://github.com/ThomasTolo/Dat250.Lab)

Pending Issues

- No major unresolved issues remain after cleaning the git history and updating the deployment process.
- Did not implement the optional microservice/reverse-proxy architecture (e.g., SvelteKit + Caddy) but understand the approach for future projects.

---

*This report summarizes the main technical challenges and solutions for Experiment Assignment 6.*
