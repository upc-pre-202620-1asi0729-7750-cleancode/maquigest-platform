# ADR: Serialize rental-request writes by requester and equipment

- **Status:** Proposed — awaiting MySQL integration verification
- **Date:** 2026-11-04
- **Bounded context:** Rentals
- **Reference:** Learning Center Platform `docs/adrs.md`, especially ADR-0002 and ADR-0007

## Context

A normal `existsOverlappingActiveRequest` followed by `save` inside `@Transactional`
can still race when two requests arrive together. Locking the conflicting request row is
insufficient when it does not exist yet. Overlapping date ranges cannot be enforced
by a simple MySQL unique constraint on `rental_requests`.

## Decision

The `RentalRequestRepository` domain port exposes the intention
`lockRequestSubmission(equipmentId, requesterUserId)`.
Its infrastructure adapter delegates to a Spring Data JPA repository executing an
InnoDB atomic INSERT/upsert against a technical table with UNIQUE
`(equipment_id, construction_user_id)`. The row is locked for the duration of the
Spring transaction. Both POST and PATCH acquire it before checking overlap.

## Rules

- Same requester + equipment + overlapping PENDING/APPROVED periods -> 409.
- Different requester + overlapping PENDING period -> allowed, subject to Inventory.
- APPROVED rental reserves equipment through Inventory ACL; existing approval workflow unchanged.
- Pending period edits preserve the request ID and require ownership and availability.
- `RentalPeriod` remains the domain value object; no JPA types leak into the aggregate.

## Consequences

- This is MySQL/InnoDB-specific infrastructure, not a new domain aggregate.
- Competing transactions for the same pair wait instead of creating duplicates.
- The guard table grows by one row per equipment/requester pair; it can be retained.
- Lock acquisition has to remain the first consistent-read-relevant step in the
  create transaction to avoid stale InnoDB REPEATABLE READ snapshots.
- Deadlocks/timeouts require normal operational retry/monitoring and do not indicate
  a duplicate by themselves.
- Cross-context access stays through `RentalEquipmentPort` / `RentalSubscriptionPort`.
- The pre-existing Rentals exception contract is preserved to avoid breaking REST
  clients/tests; architectural migration to Learning Center's
  `Result<T, ApplicationError>` is a separate, wider-scoped change.

## Verification

- Unit tests: lock before duplicate read, guard adapter delegation, invalid input.
- Opt-in test: two concurrent MySQL transactions confirm actual InnoDB blocking.
- Still required after deployment: two simultaneous authenticated REST POSTs,
  expect one 201, one 409, and exactly one new request row; repeat with PATCH.
