package com.example.taskmanager;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

class ArchitectureTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.taskmanager");
    }

    @Test
    void regra_R_REL_01_sem_OneToMany_inverso() {
        ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..entity..")
                .should().notBeAnnotatedWith(OneToMany.class)
                .because("R-JPA-02: Relacionamentos devem ser estritamente unidirecionais (@ManyToOne apenas).");
        rule.check(importedClasses);
    }

    @Test
    void regra_R_JPA_01_lazy_loading_obrigatorio() {
        ArchRule ruleManyToOne = fields()
                .that().areAnnotatedWith(ManyToOne.class)
                .should(new LazyLoadingCondition())
                .because("R-JPA-01: Todo relacionamento @ManyToOne deve ser FetchType.LAZY.");

        ruleManyToOne.check(importedClasses);
    }

    @Test
    void regra_R_ESC_01_categoria_sem_camadas_proprias() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .or().resideInAPackage("..service..")
                .or().resideInAPackage("..dto..")
                .should(new NoCategoriaInPackageCondition())
                .because("R-ESC-01: Categoria é gerenciada internamente pela service de Tarefa.");
        rule.check(importedClasses);
    }

    @Test
    void regra_R_ESTILO_01_services_devem_implementar_interfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service.impl..")
                .should(new ImplementServiceCondition())
                .because("R-ESTILO-01: A camada de serviço deve ser desenvolvida contra interfaces no pacote service.");
        rule.check(importedClasses);
    }

    private static class LazyLoadingCondition extends com.tngtech.archunit.lang.ArchCondition<com.tngtech.archunit.core.domain.JavaField> {
        public LazyLoadingCondition() {
            super("usar FetchType.LAZY");
        }

        @Override
        public void check(com.tngtech.archunit.core.domain.JavaField field, com.tngtech.archunit.lang.ConditionEvents events) {
            if (field.isAnnotatedWith(ManyToOne.class)) {
                ManyToOne annotation = field.getAnnotationOfType(ManyToOne.class);
                boolean isLazy = annotation.fetch() == FetchType.LAZY;
                String message = String.format("Campo %s.%s não usa FetchType.LAZY", 
                        field.getOwner().getName(), field.getName());
                events.add(new com.tngtech.archunit.lang.SimpleConditionEvent(field, isLazy, message));
            }
        }
    }

    private static class NoCategoriaInPackageCondition extends com.tngtech.archunit.lang.ArchCondition<com.tngtech.archunit.core.domain.JavaClass> {
        public NoCategoriaInPackageCondition() {
            super("não ter classes de Categoria");
        }

        @Override
        public void check(com.tngtech.archunit.core.domain.JavaClass clazz, com.tngtech.archunit.lang.ConditionEvents events) {
            boolean isCategoriaClass = clazz.getSimpleName().startsWith("Categoria");
            String message = String.format("Classe de Categoria encontrada no pacote proibido: %s", clazz.getName());
            events.add(new com.tngtech.archunit.lang.SimpleConditionEvent(clazz, !isCategoriaClass, message));
        }
    }

    private static class ImplementServiceCondition extends com.tngtech.archunit.lang.ArchCondition<com.tngtech.archunit.core.domain.JavaClass> {
        public ImplementServiceCondition() {
            super("implementar uma interface de serviço correspondente");
        }

        @Override
        public void check(com.tngtech.archunit.core.domain.JavaClass clazz, com.tngtech.archunit.lang.ConditionEvents events) {
            boolean implementsInterface = false;
            for (com.tngtech.archunit.core.domain.JavaType intf : clazz.getInterfaces()) {
                if (intf.toErasure().getPackageName().equals("com.example.taskmanager.service")) {
                    implementsInterface = true;
                    break;
                }
            }
            String message = String.format("Classe %s não implementa nenhuma interface no pacote com.example.taskmanager.service", clazz.getName());
            events.add(new com.tngtech.archunit.lang.SimpleConditionEvent(clazz, implementsInterface, message));
        }
    }
}
