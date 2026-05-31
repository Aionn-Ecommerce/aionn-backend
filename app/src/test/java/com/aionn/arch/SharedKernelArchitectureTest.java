package com.aionn.arch;

import com.aionn.sharedkernel.testing.arch.SharedKernelArchRules;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * Single-source-of-truth architecture test (Requirement 9 / R9.3, R9.4).
 *
 * <p>
 * This test lives in {@code :app:test} on purpose: {@code app} declares an
 * {@code implementation} dependency on every {@code com.aionn.*} module, so its
 * test classpath is the only place that "sees" the bytecode of every consumer
 * module (catalog, identity, ucp, ...) alongside the kernel itself. The kernel
 * must not depend on its consumers, so the same check could not run inside
 * {@code :shared-kernel:test}.
 *
 * <p>
 * The actual rules are the reusable {@link SharedKernelArchRules} constants
 * shipped through the shared-kernel test fixtures. They forbid any type outside
 * {@code com.aionn.sharedkernel..} from re-declaring the kernel's three
 * cross-cutting value objects: {@code Money}, {@code PhoneNumber} and
 * {@code ULID}/{@code Ulid}.
 *
 * <p>
 * There are currently no violations, so this test passes. If a consumer
 * module later re-defines one of those concepts outside the kernel, ArchUnit
 * fails the build and names the offending class (R9.4).
 */
class SharedKernelArchitectureTest {

    /**
     * Import every {@code com.aionn} class once for the whole test class. The
     * {@code app} test classpath contains the kernel plus all consumer modules.
     */
    private static final JavaClasses IMPORTED_CLASSES = new ClassFileImporter().importPackages("com.aionn");

    @Test
    void noModuleRedefinesSharedKernelValueObjects() {
        // Runs NO_DUPLICATE_MONEY, NO_DUPLICATE_PHONE_NUMBER and NO_DUPLICATE_ULID;
        // throws an AssertionError naming the offending class on the first violation
        // (R9.4).
        SharedKernelArchRules.checkAll(IMPORTED_CLASSES);
    }

    @Test
    void noModuleRedefinesMoney() {
        SharedKernelArchRules.NO_DUPLICATE_MONEY.check(IMPORTED_CLASSES);
    }

    @Test
    void noModuleRedefinesPhoneNumber() {
        SharedKernelArchRules.NO_DUPLICATE_PHONE_NUMBER.check(IMPORTED_CLASSES);
    }

    @Test
    void noModuleRedefinesUlid() {
        SharedKernelArchRules.NO_DUPLICATE_ULID.check(IMPORTED_CLASSES);
    }

    @Test
    void noModuleRedefinesAnySharedValueObject() {
        SharedKernelArchRules.NO_DUPLICATE_SHARED_VALUE_OBJECTS.check(IMPORTED_CLASSES);
    }
}
