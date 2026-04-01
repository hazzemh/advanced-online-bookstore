package com.example.bookstore.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE = "com.example.bookstore";
    private static final String[] DOMAIN_MODULES = new String[] {
            BASE + ".admin",
            BASE + ".book",
            BASE + ".cart",
            BASE + ".order",
            BASE + ".recommendation",
            BASE + ".review",
            BASE + ".user",
            BASE + ".wishlist"
    };

    @Test
    void noCyclesBetweenTopLevelModules() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(DOMAIN_MODULES);

        // Enforce no cyclic dependencies between top-level packages (modules).
        SlicesRuleDefinition.slices()
                .matching(BASE + ".(*)..")
                .should()
                .beFreeOfCycles()
                .check(classes);
    }

    @Test
    void bookRepositoryOnlyUsedInsideBookModule() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        noClasses()
                .that()
                .resideOutsideOfPackage(BASE + ".book..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(BASE + ".book.repository..")
                .check(classes);
    }

    @Test
    void userRepositoryOnlyUsedInsideUserModule() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        noClasses()
                .that()
                .resideOutsideOfPackage(BASE + ".user..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(BASE + ".user.repository..")
                .check(classes);
    }
}
