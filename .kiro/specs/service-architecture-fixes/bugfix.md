# Bugfix Requirements Document

## Introduction

The Identity module contains 38 critical issues across multiple service classes that violate Clean Architecture principles, introduce security vulnerabilities, create performance bottlenecks, and deviate from best practices. These issues span architecture violations (direct infrastructure dependencies), transaction boundary mismanagement, logic errors (wrong error codes, race conditions), missing validations, code smells (duplicated logic, god classes), security weaknesses (weak key generation, missing authorization), and performance problems (N+1 queries, in-memory filtering).

The fixes will ensure proper layering (application layer using ports instead of infrastructure), correct transaction management (at UseCase level), appropriate error handling, comprehensive validation, improved code organization, enhanced security, and optimized performance.

## Bug Analysis

### Current Behavior (Defect)

#### Architecture Violations

1.1 WHEN PreferenceService is instantiated THEN it directly imports and depends on UserPreferenceEntity, UserPreferenceRepository, and UserRepository from the infrastructure layer

1.2 WHEN KycService is instantiated THEN it directly imports and depends on KycProfileEntity, KycProfileRepository, and UserRepository from the infrastructure layer

1.3 WHEN GeographyService is instantiated THEN it directly imports and depends on CountryRepository, ProvinceRepository, DistrictRepository, and WardRepository from the infrastructure layer

1.4 WHEN AgentService is instantiated THEN it directly imports and depends on AgentIdentityEntity, SecurityAuditEntity, AgentIdentityRepository, SecurityAuditRepository, and UserRepository from the infrastructure layer

1.5 WHEN ConsentService is instantiated THEN it uses Port interfaces (ConsentPersistencePort) but also directly uses UserConsentEntity from the infrastructure layer

#### Transaction Boundary Issues

1.6 WHEN AddressService methods are invoked THEN @Transactional annotations are present at the service level instead of UseCase level

1.7 WHEN PreferenceService methods are invoked THEN @Transactional annotations are present at the service level instead of UseCase level

1.8 WHEN KycService methods are invoked THEN @Transactional annotations are present at the service level instead of UseCase level

1.9 WHEN GeographyService methods are invoked THEN @Transactional annotations are present at the service level instead of UseCase level

1.10 WHEN AgentService methods are invoked THEN @Transactional annotations are present at the service level instead of UseCase level

#### Logic Errors

1.11 WHEN AddressService.getAddressOrThrow() fails to find an address THEN it throws IdentityErrorCode.USER_NOT_FOUND instead of an address-specific error code

1.12 WHEN KycService.cancel() encounters an invalid cancellation THEN it throws IdentityErrorCode.VERIFICATION_TOKEN_INVALID instead of a KYC-specific error code

1.13 WHEN KycService methods fail to find a KYC profile THEN they throw IdentityErrorCode.USER_NOT_FOUND instead of KYC_NOT_FOUND

1.14 WHEN AgentService.getOwnedAgent() fails to find an agent THEN it throws IdentityErrorCode.USER_NOT_FOUND instead of an agent-specific error code

1.15 WHEN ConsentService methods are invoked THEN no @Transactional annotation is present, risking data inconsistency

1.16 WHEN PreferenceService.getOrCreate() is called concurrently by two requests for the same user THEN a race condition occurs where both may attempt to create default preferences simultaneously

1.17 WHEN AgentService.getAgentAuditLogs() is called THEN it fetches top 100 audit logs and filters in memory using description.contains(agentId) instead of using a database query

#### Missing Validations

1.18 WHEN AddressService.createAddress() accepts a phone number THEN no validation is performed to ensure it's a valid phone format

1.19 WHEN KycService methods transition KYC status THEN no validation is performed to ensure the transition is valid from the current status

1.20 WHEN AgentService.create() is called THEN no validation is performed to check if the owner has the capability to create agents

1.21 WHEN AdminUserService.updateRoles() is called THEN no validation is performed to check if the caller has admin privileges

#### Code Smells

1.22 WHEN RegistrationService handles OTP operations THEN OTP generation, validation, and expiry logic is duplicated across multiple methods

1.23 WHEN AuthService.login() is invoked THEN the method contains multiple responsibilities including user validation, password checking, session creation, and token generation

1.24 WHEN UserService is examined THEN it handles too many responsibilities including profile management, email operations, phone operations, avatar updates, account deletion, and data export

1.25 WHEN SecurityService is examined THEN it mixes concerns by handling both MFA operations and password reset operations

1.26 WHEN AdminUserService.listUsers() is called THEN it loads ALL users into memory and performs filtering and pagination in Java instead of at the database level

1.27 WHEN GeographyService contains four toResult() methods THEN they are nearly identical with only the entity type differing

#### Security Issues

1.28 WHEN AgentService.create() generates a key hash THEN it uses IdGenerator.ulid() which is not cryptographically secure for key generation

1.29 WHEN KycService admin methods (review, approve, reject) are called THEN they only check if the admin user exists but do not verify the user has admin role

1.30 WHEN ConsentService accepts an IP address THEN no validation is performed to ensure it's a valid IP address format

#### Performance Issues

1.31 WHEN GeographyService.resolveLocation() is examined THEN the comment claims "SINGLE database query" but the implementation may trigger lazy-loading for relationships

1.32 WHEN AdminUserService.listUsers() calls findAll() THEN it may trigger N+1 queries for user relationships

1.33 WHEN AgentService.getAgentAuditLogs() fetches audit logs THEN it retrieves 100 records and filters in Java instead of using an optimized database query

#### Best Practice Violations

1.34 WHEN service classes are examined THEN many lack @Slf4j annotations for logging important operations like user creation, status changes, and KYC approval

1.35 WHEN KycService, AgentService, and ConsentService use status strings THEN they use hardcoded strings like "DRAFT", "ACTIVE", "TERMS" instead of enums or constants

1.36 WHEN AgentService.create() sets expiry THEN it uses a magic number .plusYears(1) instead of a configuration property

1.37 WHEN services handle null values THEN GeographyService uses Optional return types while other services throw exceptions, creating inconsistent error handling

1.38 WHEN service classes are examined THEN complex business logic methods lack Javadoc documentation explaining business rules and constraints

### Expected Behavior (Correct)

#### Architecture Violations

2.1 WHEN PreferenceService is instantiated THEN it SHALL only depend on Port interfaces (UserPreferencePersistencePort) and use domain models or Result DTOs

2.2 WHEN KycService is instantiated THEN it SHALL only depend on Port interfaces (KycPersistencePort) and use domain models (KycProfile)

2.3 WHEN GeographyService is instantiated THEN it SHALL only depend on Port interfaces (GeographyPersistencePort) and use domain models

2.4 WHEN AgentService is instantiated THEN it SHALL only depend on Port interfaces and use domain models for agents and audit logs

2.5 WHEN ConsentService is instantiated THEN it SHALL only use Port interfaces and domain models, removing direct UserConsentEntity usage

#### Transaction Boundary Issues

2.6 WHEN AddressService methods are invoked THEN @Transactional annotations SHALL be removed from the service and added to UseCase classes

2.7 WHEN PreferenceService methods are invoked THEN @Transactional annotations SHALL be removed from the service and added to UseCase classes

2.8 WHEN KycService methods are invoked THEN @Transactional annotations SHALL be removed from the service and added to UseCase classes

2.9 WHEN GeographyService methods are invoked THEN @Transactional annotations SHALL be removed from the service and added to UseCase classes

2.10 WHEN AgentService methods are invoked THEN @Transactional annotations SHALL be removed from the service and added to UseCase classes

#### Logic Errors

2.11 WHEN AddressService.getAddressOrThrow() fails to find an address THEN it SHALL throw IdentityErrorCode.ADDRESS_NOT_FOUND

2.12 WHEN KycService.cancel() encounters an invalid cancellation THEN it SHALL throw IdentityErrorCode.KYC_CANNOT_BE_CANCELLED (which already exists)

2.13 WHEN KycService methods fail to find a KYC profile THEN they SHALL throw IdentityErrorCode.KYC_NOT_FOUND

2.14 WHEN AgentService.getOwnedAgent() fails to find an agent THEN it SHALL throw IdentityErrorCode.AGENT_NOT_FOUND

2.15 WHEN ConsentService methods are invoked THEN @Transactional annotations SHALL be added at the UseCase level

2.16 WHEN PreferenceService.getOrCreate() is called concurrently THEN it SHALL use database-level unique constraints or distributed locking to prevent race conditions

2.17 WHEN AgentService.getAgentAuditLogs() is called THEN it SHALL use a repository method with a WHERE clause to filter audit logs by agent ID

#### Missing Validations

2.18 WHEN AddressService.createAddress() accepts a phone number THEN it SHALL validate the phone using PhoneNumber value object from shared-kernel

2.19 WHEN KycService methods transition KYC status THEN they SHALL validate that the transition is valid from the current status using state machine logic

2.20 WHEN AgentService.create() is called THEN it SHALL validate that the owner has the capability to create agents

2.21 WHEN AdminUserService.updateRoles() is called THEN it SHALL validate that the caller has admin privileges (should be implemented at UseCase or Controller level)

#### Code Smells

2.22 WHEN RegistrationService handles OTP operations THEN OTP logic SHALL be extracted to a separate OTP service or value object

2.23 WHEN AuthService.login() is invoked THEN it SHALL extract helper methods for validation, session creation, and token generation to improve readability

2.24 WHEN UserService is examined THEN it SHALL be split into smaller services like ProfileService and AccountManagementService

2.25 WHEN SecurityService is examined THEN it SHALL be split into MfaService and PasswordResetService

2.26 WHEN AdminUserService.listUsers() is called THEN it SHALL use database-level pagination with Spring Data's Pageable

2.27 WHEN GeographyService contains toResult() methods THEN they SHALL be consolidated into a single generic method or use MapStruct mapper

#### Security Issues

2.28 WHEN AgentService.create() generates a key hash THEN it SHALL use proper cryptographic key generation (e.g., SecureRandom, BCrypt)

2.29 WHEN KycService admin methods are called THEN they SHALL verify the user has admin role using role-based authorization

2.30 WHEN ConsentService accepts an IP address THEN it SHALL validate the IP address format

#### Performance Issues

2.31 WHEN GeographyService.resolveLocation() is examined THEN the comment SHALL accurately reflect the implementation or JOIN FETCH SHALL be properly configured

2.32 WHEN AdminUserService.listUsers() calls findAll() THEN it SHALL use JOIN FETCH or DTO projection to avoid N+1 queries

2.33 WHEN AgentService.getAgentAuditLogs() fetches audit logs THEN it SHALL use an optimized repository method with proper WHERE clause

#### Best Practice Violations

2.34 WHEN service classes are examined THEN they SHALL include @Slf4j annotations and log important operations

2.35 WHEN services use status values THEN they SHALL use enums or constants instead of hardcoded strings

2.36 WHEN AgentService.create() sets expiry THEN it SHALL use a configuration property instead of a magic number

2.37 WHEN services handle errors THEN they SHALL use a consistent error handling approach across all services

2.38 WHEN service classes contain complex business logic THEN they SHALL include Javadoc documentation explaining business rules

### Unchanged Behavior (Regression Prevention)

3.1 WHEN PreferenceService methods are called with valid inputs THEN the system SHALL CONTINUE TO return correct preference results

3.2 WHEN KycService methods are called with valid inputs THEN the system SHALL CONTINUE TO process KYC operations correctly

3.3 WHEN GeographyService methods are called with valid inputs THEN the system SHALL CONTINUE TO return correct geography data

3.4 WHEN AgentService methods are called with valid inputs THEN the system SHALL CONTINUE TO manage agent identities correctly

3.5 WHEN ConsentService methods are called with valid inputs THEN the system SHALL CONTINUE TO record user consents correctly

3.6 WHEN AddressService methods are called with valid inputs THEN the system SHALL CONTINUE TO manage user addresses correctly

3.7 WHEN RegistrationService methods are called with valid inputs THEN the system SHALL CONTINUE TO handle user registration correctly

3.8 WHEN AuthService methods are called with valid inputs THEN the system SHALL CONTINUE TO authenticate users correctly

3.9 WHEN UserService methods are called with valid inputs THEN the system SHALL CONTINUE TO manage user profiles correctly

3.10 WHEN SecurityService methods are called with valid inputs THEN the system SHALL CONTINUE TO handle security operations correctly

3.11 WHEN AdminUserService methods are called with valid inputs THEN the system SHALL CONTINUE TO manage admin operations correctly

3.12 WHEN existing UseCase classes call service methods THEN they SHALL CONTINUE TO function correctly after transaction boundaries are moved

3.13 WHEN controllers call service methods THEN they SHALL CONTINUE TO receive correct responses after refactoring

3.14 WHEN domain models are used in services THEN they SHALL CONTINUE TO enforce business rules correctly

3.15 WHEN error codes are thrown THEN they SHALL CONTINUE TO be caught and handled by exception handlers correctly

3.16 WHEN caching is configured for GeographyService THEN it SHALL CONTINUE TO cache results correctly

3.17 WHEN audit logs are created THEN they SHALL CONTINUE TO be persisted correctly

3.18 WHEN OTP operations are performed THEN they SHALL CONTINUE TO generate and validate OTPs correctly

3.19 WHEN password hashing is performed THEN it SHALL CONTINUE TO use secure hashing algorithms

3.20 WHEN social login is performed THEN it SHALL CONTINUE TO authenticate users via social providers correctly
