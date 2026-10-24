# Design Decisions

## Domain model
- **Vehicle** stores a normalized, unique registration number and vehicle type.
- **ParkingSpot** belongs to a floor, has a unique floor/spot number pair, a capacity, and an availability status.
- **ParkingTicket** records the vehicle, allocated spot, entry timestamp, exit timestamp, and final fee. A ticket is active while `exitTime` is null.

A ticket is a first-class transaction record: it captures a single parking stay and remains available after checkout for history and billing.

## Allocation policy
The allocator prefers the smallest spot that can fit the vehicle, then sorts by floor and spot number. Vehicle capacities are ordered MOTORCYCLE (1), CAR (2), BUS (3), so a larger spot can fit a smaller vehicle. This reduces unnecessary use of larger spots.

## Concurrency guarantee
A candidate read is not a reservation. Two requests can both observe the same spot as available. The actual claim is the conditional update:

```sql
UPDATE parking_spots
SET status = 'OCCUPIED'
WHERE id = :spotId AND status = 'AVAILABLE';
```

The update count is the winner signal: one row means the request claimed the spot; zero means it lost the race. The database serializes conflicting writes to the same row. The claim and ticket creation share one transaction, so a ticket persistence failure rolls the claim back.

For concurrent check-ins of the same existing vehicle, the vehicle row is selected with a pessimistic write lock before checking for an active ticket. This serializes the same-vehicle check-in path. A unique registration constraint handles concurrent first-time registration attempts, with one conflicting request receiving an error.

## Checkout
Checkout runs in one transaction: verify the ticket is active, calculate the fee, close the ticket, and change the occupied spot to available. The conditional release must update exactly one row; otherwise the operation fails rather than silently hiding inconsistent state.

## Pricing
FeeCalculator charges for each started hour and has a one-hour minimum. Rates are demonstration values and can be replaced independently from lifecycle logic.

## Production considerations
H2 helps local setup but is not a substitute for integration tests against the intended production database. Verify conditional-update semantics, deadlock behavior, transaction isolation, and concurrent load against PostgreSQL/MySQL. Production deployment also needs schema migrations, auth, observability, and API idempotency.
