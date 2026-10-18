## Purpose

允许实体声明一个独立的编码长度字段，由编码器按指定字段范围的实际编码字节数自动回填，避免业务代码手动维护消息体长度。

## Requirements

### Requirement: @EncodedLength annotation declaration

The system SHALL support an `@EncodedLength` annotation on unsigned integer length fields to declare the field's encoded value as the cumulative byte length of a range of subsequent encoded fields.

#### Scenario: Annotation declaration and defaults

- **WHEN** the system reads the `@EncodedLength` annotation declaration and its field usage
- **THEN** `@EncodedLength` MUST have `from()` attribute (String, default `""`) specifying the inclusive start field name
- **THEN** `@EncodedLength` MUST have `until()` attribute (String, default `""`) specifying the exclusive end field name
- **THEN** when `from` is empty, the range SHALL start from the field immediately after the `@EncodedLength` field in encoding order
- **THEN** when `until` is empty, the range SHALL extend to the last field in encoding order (inclusive)
- **THEN** the `@EncodedLength` field MUST also carry an unsigned integer wire-format annotation equivalent to u8, u16, or u32
- **THEN** the `@EncodedLength` annotation SHALL have `@Retention(RetentionPolicy.RUNTIME)` and `@Target(ElementType.FIELD)`

#### Scenario: @EncodedLength with explicit from and until

- **WHEN** a field `bodyLength` is annotated with `@EncodedLength(from = "username", until = "checkSum")` and `@Preset.RustStyle.u16`
- **THEN** the system SHALL resolve `username` and `checkSum` as field names in the same merged entity field list
- **THEN** on encode, `bodyLength` SHALL be written as the number of bytes from the start of `username`'s wire output to the start of `checkSum`'s wire output

#### Scenario: @EncodedLength with from only

- **WHEN** `@EncodedLength(from = "username")` with empty `until`
- **THEN** the range SHALL extend from the start of `username`'s wire output to the end of the last encoded field

#### Scenario: @EncodedLength with until only

- **WHEN** `@EncodedLength(until = "checkSum")` with empty `from`
- **THEN** the range SHALL start from the field immediately after `bodyLength` in encoding order

#### Scenario: @EncodedLength with both empty (full body range)

- **WHEN** `@EncodedLength()` with empty `from` and `until`
- **THEN** the range SHALL be all fields after the `@EncodedLength` field, from the next field to the last field inclusive

### Requirement: Compile-time metadata validation

The system SHALL validate `@EncodedLength` field references at metadata registration time.

#### Scenario: Invalid field metadata

- **WHEN** an entity's `@EncodedLength` metadata is registered
- **THEN** the system SHALL reject `from` field name that does not exist in the entity's field list
- **THEN** the system SHALL reject `until` field name that does not exist in the entity's field list
- **THEN** the system SHALL reject `from == until`
- **THEN** the system SHALL reject multiple `@EncodedLength` annotations on the same entity
- **THEN** the system SHALL reject `from` field appearing after `until` field in encoding order
- **THEN** the system SHALL reject `from` or `until` appearing before or at the `@EncodedLength` field in encoding order
- **THEN** the system SHALL reject an `@EncodedLength` field whose own wire format is not u8, u16, or u32

#### Scenario: Invalid from field name

- **WHEN** `@EncodedLength(from = "nonExistentField")` references a field not in the entity
- **THEN** metadata registration SHALL throw `IllegalArgumentException` with message containing the invalid field name

#### Scenario: from equals until

- **WHEN** `@EncodedLength(from = "foo", until = "foo")`
- **THEN** metadata registration SHALL throw `IllegalArgumentException`

#### Scenario: from after until in encoding order

- **WHEN** `from` field's order > `until` field's order
- **THEN** metadata registration SHALL throw `IllegalArgumentException`

#### Scenario: Multiple @EncodedLength fields

- **WHEN** an entity declares more than one `@EncodedLength` field
- **THEN** metadata registration SHALL throw `IllegalArgumentException`

#### Scenario: Unsupported length field wire format

- **WHEN** an `@EncodedLength` field uses a wire format other than u8, u16, or u32
- **THEN** metadata registration SHALL throw `IllegalArgumentException`

### Requirement: Encoding backfill

The encoder SHALL compute and backfill the range length during entity encoding.

#### Scenario: Backfill sequence

- **WHEN** an entity with an `@EncodedLength` field is encoded
- **THEN** the encoder SHALL write a placeholder value (0) for the `@EncodedLength` field during initial encoding pass
- **THEN** the encoder SHALL record the range start at the writer index immediately before encoding the `from` field, or immediately after the placeholder field when `from` is empty
- **THEN** the encoder SHALL, before encoding the `until` field (or after the last field if `until` is empty), compute `rangeEnd - rangeStart` as the range length
- **THEN** the encoder SHALL overwrite the placeholder bytes at the recorded writer index with the computed length
- **THEN** the encoder SHALL use the wire format specified by the field's unsigned integer annotation for the backfill (e.g., `setByte` for u8, `setShort` for u16, `setInt` for u32)

#### Scenario: Placeholder backfill with u16

- **WHEN** `bodyLength` is `@Preset.RustStyle.u16` and `@EncodedLength(from = "fieldA", until = "fieldZ")`, and the encoded range body is 42 bytes
- **THEN** the encoder SHALL first write 0x0000 as placeholder at `bodyLength`'s position
- **THEN** the encoder SHALL overwrite the placeholder with 42 as unsigned 16-bit big-endian

#### Scenario: Conditional or null fields in range

- **WHEN** fields inside the declared range are skipped because their condition evaluates to false or their value is null
- **THEN** the computed range length SHALL include only bytes actually written to the output buffer

### Requirement: Decoding passthrough

The `@EncodedLength` field SHALL decode normally as a regular numeric field.

- No special treatment during decoding
- The value read from the wire is stored in the field as-is

#### Scenario: Decode @EncodedLength u16 field

- **WHEN** decoding bytes `0x00 0x2A ...`
- **THEN** the `@EncodedLength` annotated field SHALL be set to 42 via normal decode path

### Requirement: Inheritance support

The `@EncodedLength` annotation SHALL work across parent-child inheritance hierarchies.

- A parent class SHALL declare `@EncodedLength` field
- A child's fields SHALL automatically participate in the range when they encode between `from` and `until` in encoding order
- Field resolution SHALL consider the full merged field list (parent + child fields ordered by `order` attribute)

#### Scenario: Parent declares bodyLength and checkSum, child declares body fields

- **WHEN** parent has `@EncodedLength(until = "checkSum") bodyLength` and child has fields that encode between `bodyLength` and `checkSum`
- **THEN** the encoder SHALL correctly compute the length of all child fields' encoded bytes

### Requirement: @since 0.7.0

All new public APIs introduced by this change SHALL carry `@since 0.7.0` in their Javadoc.

#### Scenario: @since on annotation

- **WHEN** viewing `@EncodedLength` annotation source
- **THEN** it SHALL have `@since 0.7.0` in its Javadoc
