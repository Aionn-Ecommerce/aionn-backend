# Service Architecture Fixes - Bugfix Design

## Overview

This design addresses 38 critical issues across the Identity module's service layer that violate Clean Architecture principles, introduce security vulnerabilities, create performance bottlenecks, and deviate from best practices. The fixes will transform the service layer to properly follow hexagonal architecture by removing direct infrastructure dependencies, moving transaction boundaries to UseCase level, correcting error codes, adding missing validations, refactoring god classes, enhancing security, and optimizing performance.

The approach is systematic: create missing Port interfaces and domain models, refactor services to use only ports, move @Transactional annotations from services to UseCases, correct all error codes, add comprehensive validations, extract duplicated logic, implement proper security measures, and optimize database queries.

## Glossary

- **Bug_Condition (C)**: The condition that triggers architectural violations, logic errors, or performance issues - when services directly depend on infrastructure, use wrong error codes, lack validations, or perform inefficient operations
- **Property (P)**: The desired behavior - services should only depend on Port interfaces and domain models, use correct error codes, validate all inputs, follow single responsibility principle, implement proper security, and optimize database access
- **Preservation**: Existing business logic and API contracts that must remain unchanged by the refactoring
- **Port Interface**: An interface in the application layer that defines operations for external dependencies (persistence, messaging, etc.)
- **Adapter**: An implementation in the infrastructure layer that fulfills a Port interface
- **Service Layer**: Application services that orchestrate business logic using domain models and ports
- **UseCase**: Entry point classes that define transaction boundaries and coordinate service calls
- **Clean Architecture**: Architectural pattern where dependencies point inward (Infrastructure → Application → Domain)
- **Hexagonal Architecture**: Synonym for Clean Architecture emphasizing ports and adapters pattern

## Bug Details

### Bug Condition

The bugs manifest across five categories: architecture violations, transaction boundary issues, logic errors, missing validations, and code smells. Services in the application layer directly import and use infrastructure entities and repositories, violating the dependency rule. Transaction annotations are placed at the service level instead of UseCase level, creating improper boundaries. Services throw incorrect error codes (USER_NOT_FOUND for address/agent/KYC errors). Critical validations are missing (phone format, status transitions, authorization checks). Services contain duplicated logic, god classes, and inefficient database operations.

**Formal Specification:**

```
FUNCTION isBugCondition(serviceClass)
  INPUT: serviceClass of type JavaClass
  OUTPUT: boolean

  RETURN (serviceClass imports InfrastructureEntity OR InfrastructureRepository)
         OR (serviceClass.methods contain @Transactional)
         OR (serviceClass throws WrongErrorCode)
         OR (serviceClass.methods lack RequiredValidation)
         OR (serviceClass violates SingleResponsibilityPrinciple)
         OR (serviceClass contains SecurityVulnerability)
         OR (serviceClass performs InMemoryFiltering OR N+1Query)
END FUNCTION
```

### Examples

- **Architecture Violation**: PreferenceService directly imports UserPreferenceEntity and UserPreferenceRepository from infrastructure layer instead of using UserPreferencePersistencePort
- **Transaction Boundary**: AddressService.createAddress() has @Transactional annotation instead of CreateAddressUseCase having it
- **Logic Error**: AddressService.getAddressOrThrow() throws USER_NOT_FOUND when address is not found instead of ADDRESS_NOT_FOUND
- **Missing Validation**: AddressService.createAddress() accepts phone number without validating format using PhoneNumber value object
- **Code Smell**: UserService handles profile management, email operations, phone operations, avatar updates, account deletion, and data export (god class)
- **Security Issue**: AgentService.create() uses IdGenerator.ulid() for keyHash instead of cryptographically secure key generation
- **Performance Issue**: AgentService.getAgentAuditLogs() fetches 100 audit logs then filters in memory by description.contains(agentId)
- **Edge Case**: PreferenceService.getOrCreate() can cause race condition if two concurrent requests create preferences simultaneously

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**

- All existing API contracts and method signatures must remain unchanged
- Business logic for valid inputs must produce identical results
- Domain model behavior and invariants must be preserved
- Existing caching configurations must continue to work
- Error handling for valid error scenarios must remain consistent
- All existing tests must continue to pass after refactoring

**Scope:**
All inputs that do NOT involve the buggy code paths should be completely unaffected by this fix. This includes:

- Successful operations with valid inputs
- Existing error handling for legitimate error cases
- Integration with controllers and external systems
- Domain model business rules and validations
- Existing transaction behavior for operations not being refactored

## Hypothesized Root Cause

Based on the comprehensive analysis, the root causes are:

1. **Lack of Port Interfaces**: Services were created without defining Port interfaces first, leading developers to directly use infrastructure repositories for convenience

2. **Misunderstanding of Transaction Boundaries**: Developers placed @Transactional at service level thinking it's the "business logic layer" without understanding that UseCases are the proper entry points

3. **Copy-Paste Error Handling**: Error codes were copied from similar methods without updating to context-specific codes (e.g., copying USER_NOT_FOUND everywhere)

4. **Incremental Feature Addition**: Services grew organically as features were added without refactoring, leading to god classes and duplicated logic

5. **Missing Validation Framework**: No systematic approach to validation led to inconsistent validation across services

6. **Security Oversight**: Security concerns like cryptographic key generation and authorization checks were not considered during initial implementation

7. **Premature Optimization Avoidance**: Developers avoided query optimization thinking "premature optimization is evil" without recognizing N+1 queries and in-memory filtering as obvious problems

## Correctness Properties

Property 1: Bug Condition - Clean Architecture Compliance

_For any_ service class in the application layer, the refactored service SHALL only depend on Port interfaces (defined in application.port.out) and domain models (defined in domain.model), never directly importing or using infrastructure entities or repositories.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

Property 2: Bug Condition - Proper Transaction Boundaries

_For any_ transactional operation, the @Transactional annotation SHALL be placed at the UseCase level (classes implementing InputPort interfaces) and removed from service methods, ensuring transaction boundaries are at entry points.

**Validates: Requirements 2.6, 2.7, 2.8, 2.9, 2.10**

Property 3: Bug Condition - Correct Error Codes

_For any_ error condition in services, the thrown IdentityException SHALL use the contextually appropriate error code (ADDRESS_NOT_FOUND for addresses, KYC_NOT_FOUND for KYC, AGENT_NOT_FOUND for agents) instead of generic USER_NOT_FOUND.

**Validates: Requirements 2.11, 2.12, 2.13, 2.14**

Property 4: Bug Condition - Comprehensive Validation

_For any_ service method accepting external input, the method SHALL validate all inputs using appropriate value objects (PhoneNumber), state machine logic (KYC status transitions), and authorization checks (admin role verification) before processing.

**Validates: Requirements 2.18, 2.19, 2.20, 2.21**

Property 5: Bug Condition - Single Responsibility

_For any_ service class, the class SHALL handle a single cohesive responsibility (e.g., ProfileService for profile operations, AccountManagementService for account lifecycle) instead of mixing multiple unrelated concerns.

**Validates: Requirements 2.22, 2.23, 2.24, 2.25**

Property 6: Bug Condition - Secure Operations

_For any_ security-sensitive operation (key generation, admin actions, consent recording), the operation SHALL use cryptographically secure methods (SecureRandom for keys), verify authorization (role checks), and validate inputs (IP address format).

**Validates: Requirements 2.28, 2.29, 2.30**

Property 7: Bug Condition - Optimized Database Access

_For any_ database query operation, the operation SHALL use database-level filtering, pagination, and JOIN FETCH to avoid N+1 queries and in-memory filtering, ensuring queries are executed at the database level.

**Validates: Requirements 2.17, 2.26, 2.31, 2.32, 2.33**

Property 8: Preservation - Business Logic Unchanged

_For any_ input that produces correct behavior in the original code, the refactored code SHALL produce exactly the same result, preserving all business logic, domain rules, and API contracts.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 3.13, 3.14, 3.15, 3.16, 3.17, 3.18, 3.19, 3.20**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct, we need to systematically refactor all affected services:

**Phase 1: Create Missing Port Interfaces and Domain Models**

**Files to Create**:

1. `application/port/out/geography/GeographyPersistencePort.java` - Port for geography data access
2. `application/port/out/agent/AgentPersistencePort.java` - Port for agent identity operations
3. `application/port/out/agent/AgentAuditPort.java` - Port for agent audit logs
4. `domain/model/AgentIdentity.java` - Domain model for agent identity
5. `domain/model/SecurityAudit.java` - Domain model for security audit logs
6. `domain/model/UserConsent.java` - Domain model for user consent (already exists as entity, need domain version)
7. `domain/valueobject/ConsentType.java` - Enum for consent types (TERMS, PRIVACY, MARKETING)
8. `domain/valueobject/AgentStatus.java` - Enum for agent status (ACTIVE, SUSPENDED)
9. `infrastructure/adapter/GeographyPersistenceAdapter.java` - Already exists, verify implementation
10. `infrastructure/adapter/AgentPersistenceAdapter.java` - Adapter implementing AgentPersistencePort
11. `infrastructure/adapter/AgentAuditAdapter.java` - Adapter implementing AgentAuditPort

**Phase 2: Refactor Services to Use Ports**

**File**: `application/service/PreferenceService.java`

- Remove: Direct imports of UserPreferenceEntity, UserPreferenceRepository
- Already uses: UserPreferencePersistencePort (good)
- Remove: @Transactional annotations from all methods
- Add: Race condition handling in getOrCreate() using database unique constraint or distributed lock

**File**: `application/service/KycService.java`

- Already refactored: Uses KycPersistencePort and domain model KycProfile (good)
- Remove: @Transactional annotations from all methods
- Fix: validateAdminUser() to actually check admin role (currently has TODO comment)
- Add: Status transition validation in submit(), review(), approve(), reject() methods

**File**: `application/service/GeographyService.java`

- Remove: Direct imports of CountryRepository, ProvinceRepository, DistrictRepository, WardRepository
- Replace with: GeographyPersistencePort
- Remove: @Transactional annotations from all methods
- Consolidate: Four toResult() methods into single generic method or MapStruct mapper
- Verify: resolveLocation() actually uses JOIN FETCH or update comment

**File**: `application/service/AgentService.java`

- Remove: Direct imports of AgentIdentityEntity, SecurityAuditEntity, repositories
- Replace with: AgentPersistencePort, AgentAuditPort
- Use: Domain models AgentIdentity, SecurityAudit
- Remove: @Transactional annotations from all methods
- Fix: create() to use SecureRandom for key generation instead of IdGenerator.ulid()
- Fix: getOwnedAgent() to throw AGENT_NOT_FOUND instead of USER_NOT_FOUND
- Fix: getAgentAuditLogs() to use repository method with WHERE clause instead of in-memory filtering
- Add: Validation in create() to check owner capability
- Extract: Magic number .plusYears(1) to configuration property

**File**: `application/service/ConsentService.java`

- Remove: Direct import of UserConsentEntity
- Use: Domain model UserConsent
- Add: @Transactional annotations at UseCase level
- Replace: Hardcoded strings "TERMS", "PRIVACY", "MARKETING" with ConsentType enum
- Add: IP address format validation

**File**: `application/service/AddressService.java`

- Remove: @Transactional annotations from all methods
- Fix: getAddressOrThrow() to throw ADDRESS_NOT_FOUND instead of USER_NOT_FOUND
- Add: Phone number validation using PhoneNumber value object from shared-kernel

**File**: `application/service/RegistrationService.java`

- Extract: OTP generation, validation, expiry logic to separate OtpService or OtpValue value object
- Keep: Existing logic but refactor for reusability

**File**: `application/service/AuthService.java`

- Extract: Helper methods for validation, session creation, token generation from login() method
- Keep: Existing functionality but improve readability

**File**: `application/service/UserService.java`

- Split into: ProfileService (profile, display name, avatar) and AccountManagementService (deletion, export, email/phone changes)
- Remove: Direct imports of UserEntity, AccountDeletionRequestEntity, DataExportRequestEntity, repositories
- Create: Ports for account deletion and data export operations
- Remove: @Transactional annotations (move to UseCases)

**File**: `application/service/SecurityService.java`

- Split into: MfaService (MFA operations, backup codes) and PasswordResetService (password reset operations)
- Remove: Direct imports of BackupCodeEntity, SecurityAuditEntity, repositories
- Create: Ports for MFA and password reset operations
- Remove: @Transactional annotations (move to UseCases)

**File**: `application/service/AdminUserService.java`

- Fix: listUsers() to use database-level pagination with Pageable instead of in-memory filtering
- Fix: listUsers() to use JOIN FETCH or DTO projection to avoid N+1 queries
- Add: Authorization check in updateRoles() to verify caller has admin privileges (should be at UseCase/Controller level)

**Phase 3: Add Missing Error Codes**

**File**: `domain/exception/IdentityErrorCode.java`

- Add: ADDRESS_NOT_FOUND (if not exists)
- Add: AGENT_NOT_FOUND (if not exists)
- Verify: KYC_NOT_FOUND exists
- Verify: KYC_CANNOT_BE_CANCELLED exists

**Phase 4: Move Transaction Boundaries to UseCases**

For each affected UseCase class:

- Add: @Transactional annotation at UseCase execute() method
- Verify: Service methods no longer have @Transactional

**Affected UseCases** (examples):

- CreateAddressUseCase
- UpdateAddressUseCase
- DeleteAddressUseCase
- SetDefaultAddressUseCase
- UpdateGeneralPreferenceUseCase
- UpdateNotificationPreferenceUseCase
- UpdateAiPrivacyPreferenceUseCase
- CreateKycUseCase
- SubmitKycUseCase
- ReviewKycUseCase
- ApproveKycUseCase
- RejectKycUseCase
- CreateAgentUseCase
- SuspendAgentUseCase
- RevokeAgentUseCase
- (And all other UseCases calling affected services)

**Phase 5: Add Comprehensive Logging**

For all service classes:

- Add: @Slf4j annotation if missing
- Add: log.info() for important operations (user creation, status changes, KYC approval, agent creation)
- Add: log.debug() for query operations
- Add: log.warn() for validation failures

**Phase 6: Add Javadoc Documentation**

For all service classes:

- Add: Class-level Javadoc explaining service responsibility
- Add: Method-level Javadoc for complex business logic explaining rules and constraints

## Testing Strategy

### Validation Approach

The testing strategy follows a three-phase approach: first, write exploratory tests on unfixed code to surface counterexamples demonstrating the bugs; second, implement fixes and verify they resolve the issues; third, run comprehensive preservation tests to ensure existing behavior is unchanged.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bugs BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that attempt to instantiate services and verify their dependencies, check transaction annotations, trigger error conditions, and observe performance characteristics. Run these tests on the UNFIXED code to observe failures and understand the root causes.

**Test Cases**:

1. **Architecture Violation Test**: Verify PreferenceService imports infrastructure classes (will fail on unfixed code showing direct dependencies)
2. **Transaction Boundary Test**: Verify AddressService methods have @Transactional annotations (will fail on unfixed code showing wrong placement)
3. **Wrong Error Code Test**: Call AddressService.getAddressOrThrow() with invalid address ID and verify it throws USER_NOT_FOUND (will fail on unfixed code)
4. **Missing Validation Test**: Call AddressService.createAddress() with invalid phone format and verify it's accepted (will fail on unfixed code showing no validation)
5. **God Class Test**: Count responsibilities in UserService (will fail on unfixed code showing multiple concerns)
6. **Security Test**: Verify AgentService.create() uses IdGenerator.ulid() for keyHash (will fail on unfixed code showing weak key generation)
7. **Performance Test**: Call AgentService.getAgentAuditLogs() and count database queries (will fail on unfixed code showing in-memory filtering)
8. **Race Condition Test**: Call PreferenceService.getOrCreate() concurrently and verify duplicate creation (will fail on unfixed code)

**Expected Counterexamples**:

- Services directly import infrastructure entities and repositories
- @Transactional annotations present at service level instead of UseCase level
- Wrong error codes thrown for context-specific errors
- Missing validations allow invalid inputs
- God classes handle multiple unrelated responsibilities
- Weak key generation and missing authorization checks
- N+1 queries and in-memory filtering instead of database-level operations
- Race conditions in concurrent operations

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed services produce the expected behavior.

**Pseudocode:**

```
FOR ALL serviceClass WHERE isBugCondition(serviceClass) DO
  result := refactoredService.operation(input)
  ASSERT usesOnlyPortsAndDomainModels(refactoredService)
  ASSERT transactionAtUseCaseLevel(refactoredService)
  ASSERT correctErrorCode(result)
  ASSERT inputValidated(input)
  ASSERT singleResponsibility(refactoredService)
  ASSERT secureOperation(refactoredService)
  ASSERT optimizedDatabaseAccess(refactoredService)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (i.e., valid inputs producing correct behavior), the fixed services produce the same result as the original services.

**Pseudocode:**

```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT originalService.operation(input) = refactoredService.operation(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:

- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all valid inputs

**Test Plan**: Observe behavior on UNFIXED code first for valid operations, then write property-based tests capturing that behavior.

**Test Cases**:

1. **Preference Operations Preservation**: Observe that updateGeneral(), updateNotifications(), updateAiPrivacy() work correctly on unfixed code, then verify same results after refactoring
2. **KYC Operations Preservation**: Observe that createKyc(), uploadDocument(), submit() work correctly on unfixed code, then verify same results after refactoring
3. **Geography Operations Preservation**: Observe that listCountries(), listProvinces(), resolveLocation() work correctly on unfixed code, then verify same results after refactoring
4. **Agent Operations Preservation**: Observe that create(), updatePermissions(), suspend() work correctly on unfixed code, then verify same results after refactoring
5. **Address Operations Preservation**: Observe that createAddress(), updateAddress(), setDefaultAddress() work correctly on unfixed code, then verify same results after refactoring
6. **Auth Operations Preservation**: Observe that login(), socialLogin(), logout() work correctly on unfixed code, then verify same results after refactoring
7. **User Operations Preservation**: Observe that getMyProfile(), updateDisplayName(), updateAvatar() work correctly on unfixed code, then verify same results after refactoring
8. **Security Operations Preservation**: Observe that changePassword(), enableMfa(), regenerateBackupCodes() work correctly on unfixed code, then verify same results after refactoring
9. **Admin Operations Preservation**: Observe that updateRoles(), updateStatus(), listUsers() work correctly on unfixed code, then verify same results after refactoring

### Unit Tests

- Test each Port interface implementation (adapters) in isolation
- Test service methods with mocked ports to verify business logic
- Test error code throwing for each error condition
- Test validation logic for all input types
- Test state machine transitions for KYC status
- Test authorization checks for admin operations
- Test OTP generation and validation logic
- Test concurrent operations for race conditions

### Property-Based Tests

- Generate random valid inputs and verify services produce consistent results before and after refactoring
- Generate random invalid inputs and verify appropriate error codes are thrown
- Generate random user states and verify status transitions follow state machine rules
- Generate random concurrent requests and verify no race conditions or data corruption
- Generate random pagination parameters and verify database-level pagination works correctly

### Integration Tests

- Test full flow from controller → UseCase → service → adapter → repository
- Test transaction boundaries are properly maintained at UseCase level
- Test caching continues to work after refactoring
- Test audit logging is preserved after refactoring
- Test error handling flows from service through exception handler to API response
- Test performance improvements (fewer queries, no in-memory filtering)
- Test security enhancements (proper key generation, authorization checks)
