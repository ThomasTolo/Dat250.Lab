# DAT250 Experimental Assignment 6

## Technical problems encountered
- **Frontend build integration**: Getting the Svelte/Vite output copied into Spring Boot `static/` required Gradle task adjustments and fixing a Node version mismatch (switched to Node 20 LTS after issues with 22).
- **Real-time vote updates**: Ensuring immediate UI updates needed a custom WebSocket `vote-delta` payload and avoiding double broadcasts when the same event came from REST + broker.
- **RabbitMQ setup & connection errors**: Initial connection refused until the broker was running; clarified exchange (`poll-exchange`), routing key (`poll.<pollId>`), and single queue binding.
- **Event parsing failures**: Early JSON deserialization threw `UnrecognizedPropertyException`; fixed by configuring Jackson to ignore unknown fields.
- **Sentinel event misuse**: A placeholder “vote” with invalid option id caused listener rollbacks; removed and introduced explicit `PollCreated` and `Vote` event types.
- **Anonymous vote handling**: Duplicate anonymous votes appeared until a query with `IS NULL` branch was added in `PollManager`.
- **External votes not visible**: Realized the consumer modified the DB but didn’t broadcast; added a WebSocket broadcast only for externally originated events (`source != app`).

## Pending / not fully solved
- **No persistent event store**: Events are applied directly; raw event log not stored for replay/versioning.
- **No dead-letter / retry strategy**: Failed messages are logged and skipped, but not routed to a DLQ.
- **Schema versioning omitted**: Events lack a `version` field for forward compatibility.
- **Anonymous vote UX**: UI does not yet aggregate or label anonymous participation distinctly.
- **Security hardening**: Still using default RabbitMQ `guest/guest` credentials (acceptable for local dev only).

## Summary
Core assignment goals (publish/consume poll and vote events, external publishing, real-time UI updates) are working; remaining items are mainly hardening and optional enhancements.
