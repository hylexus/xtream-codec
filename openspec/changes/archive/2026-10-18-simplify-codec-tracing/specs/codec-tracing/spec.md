## MODIFIED Requirements

### Requirement: Structured codec trace
The system SHALL produce a structured trace for debug encode and decode operations.

- The trace SHALL identify the operation direction as encode or decode.
- The trace SHALL expose the root entity type when known.
- The trace SHALL contain ordered trace nodes representing the encoded or decoded structure.
- Byte ranges SHALL use offsets relative to the traced input or output buffer segment.

#### Scenario: Decode simple entity
- **WHEN** a simple entity is decoded with tracking enabled
- **THEN** the resulting trace SHALL contain one root node and ordered child nodes for decoded fields
- **THEN** each decoded field node SHALL include the bytes consumed by that field
- **THEN** each decoded field node SHALL identify its FieldCodec as the processor type when available

#### Scenario: Encode simple entity
- **WHEN** a simple entity is encoded with tracking enabled
- **THEN** the resulting trace SHALL contain one root node and ordered child nodes for encoded fields
- **THEN** each encoded field node SHALL include the bytes written for that field

#### Scenario: Encode structured property
- **WHEN** a nested entity, collection, or map property is encoded with tracking enabled
- **THEN** the resulting trace SHALL contain a hierarchical node for that property
- **THEN** the property node SHALL identify the component that organized its child codec operations as the processor type when available

#### Scenario: Trace node metadata

- **WHEN** an application inspects a node from a debug encode or decode trace
- **THEN** each trace node SHALL expose a stable node id, parent relation, node kind, display name, field path when available, Java type when available, processor type when available, value summary when available, byte range, and local hex representation
- **THEN** the processor type SHALL identify the actual component responsible for the node's codec operation or structural orchestration
