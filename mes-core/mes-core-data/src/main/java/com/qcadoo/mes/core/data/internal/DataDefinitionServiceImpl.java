package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Dictionary;
import com.qcadoo.mes.core.data.beans.DictionaryItem;
import com.qcadoo.mes.core.data.beans.TestBeanA;
import com.qcadoo.mes.core.data.beans.TestBeanB;
import com.qcadoo.mes.core.data.beans.TestBeanC;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.callbacks.CallbackFactory;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private CallbackFactory callbackFactory;

    @Autowired
    private FieldValidatorFactory fieldValidationFactory;

    @Override
    public void save(final DataDefinition dataDefinition) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DataDefinition get(final String entityName) {
        DataDefinition dataDefinition = null;
        if ("products.product".equals(entityName)) {
            dataDefinition = createProductDefinition();
        } else if ("products.substitute".equals(entityName)) {
            dataDefinition = createSubstituteDefinition();
        } else if ("products.substituteComponent".equals(entityName)) {
            dataDefinition = createSubstituteComponentDefinition();
        } else if ("users.user".equals(entityName)) {
            dataDefinition = createUserDefinition();
        } else if ("users.group".equals(entityName)) {
            dataDefinition = createUserGroupDefinition();
        } else if ("products.order".equals(entityName)) {
            dataDefinition = createOrderDefinition();
        } else if ("products.instruction".equals(entityName)) {
            dataDefinition = createInstructionDefinition();
        } else if ("core.dictionary".equals(entityName)) {
            dataDefinition = createDictionaryDefinition();
        } else if ("core.dictionaryItem".equals(entityName)) {
            dataDefinition = createDictionaryItemDefinition();
        } else if ("test.testBeanA".equals(entityName)) {
            dataDefinition = createTestBeanAItemDefinition();
        } else if ("test.testBeanB".equals(entityName)) {
            dataDefinition = createTestBeanBItemDefinition();
        } else if ("test.testBeanC".equals(entityName)) {
            dataDefinition = createTestBeanCItemDefinition();
        } else if ("plugins.plugin".equals(entityName)) {
            dataDefinition = createPluginDefinition();
        }

        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);

        return dataDefinition;
    }

    private DataDefinition createTestBeanAItemDefinition() {
        DataDefinition dataDefinition = new DataDefinition("test.testBeanA");
        dataDefinition.setFullyQualifiedClassName(TestBeanA.class.getCanonicalName());

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        DataFieldDefinition fieldBeanB = createFieldDefinition("beanB",
                fieldTypeFactory.eagerBelongsToType("test.testBeanB", "name"));
        DataFieldDefinition fieldBeansC = createFieldDefinition("beansC", fieldTypeFactory.hasManyType("test.testBeanC", "beanA"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeanB);
        dataDefinition.addField(fieldBeansC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanBItemDefinition() {
        DataDefinition dataDefinition = new DataDefinition("test.testBeanB");
        dataDefinition.setFullyQualifiedClassName(TestBeanB.class.getCanonicalName());

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        DataFieldDefinition fieldBeansA = createFieldDefinition("beansA", fieldTypeFactory.hasManyType("test.testBeanA", "beanB"));
        DataFieldDefinition fieldBeanC = createFieldDefinition("beanC",
                fieldTypeFactory.eagerBelongsToType("test.testBeanC", "name"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeansA);
        dataDefinition.addField(fieldBeanC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanCItemDefinition() {
        DataDefinition dataDefinition = new DataDefinition("test.testBeanC");
        dataDefinition.setFullyQualifiedClassName(TestBeanC.class.getCanonicalName());

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        DataFieldDefinition fieldBeanA = createFieldDefinition("beanA",
                fieldTypeFactory.eagerBelongsToType("test.testBeanA", "name"));
        DataFieldDefinition fieldBeansB = createFieldDefinition("beansB", fieldTypeFactory.hasManyType("test.testBeanB", "beanC"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeanA);
        dataDefinition.addField(fieldBeansB);

        return dataDefinition;
    }

    private DataDefinition createProductDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.product");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        DataFieldDefinition fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        DataFieldDefinition fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());
        DataFieldDefinition fieldSubstitutes = createFieldDefinition("substitutes",
                fieldTypeFactory.hasManyType("products.substitute", "product"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Product");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldEan);
        dataDefinition.addField(fieldCategory);
        dataDefinition.addField(fieldUnit);
        dataDefinition.addField(fieldSubstitutes);

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substitute");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldComponents = createFieldDefinition("components",
                fieldTypeFactory.hasManyType("products.substituteComponent", "substitute"));

        DataFieldDefinition fieldPriority = createFieldDefinition("priority", fieldTypeFactory.priorityType(fieldProduct))
                .readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Substitute");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldComponents);

        dataDefinition.setPriorityField(fieldPriority);

        dataDefinition.addValidator(fieldValidationFactory.customEntity("productService", "checkSubstituteDates")
                .customErrorMessage("products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substituteComponent");

        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products.substitute", "name")).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.SubstituteComponent");
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldSubstitute);
        dataDefinition.addField(fieldQuantity);

        return dataDefinition;
    }

    private DataDefinition createUserDefinition() {
        DataDefinition dataDefinition = new DataDefinition("users.user");

        DataFieldDefinition fieldUserName = createFieldDefinition("userName", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        DataFieldDefinition fieldUserGroup = createFieldDefinition("userGroup",
                fieldTypeFactory.eagerBelongsToType("users.group", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldEmail = createFieldDefinition("email", fieldTypeFactory.stringType());
        DataFieldDefinition fieldFirstName = createFieldDefinition("firstName", fieldTypeFactory.stringType());
        DataFieldDefinition fieldLastName = createFieldDefinition("lastName", fieldTypeFactory.stringType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        DataFieldDefinition fieldPassword = createFieldDefinition("password", fieldTypeFactory.passwordType()).withValidator(
                fieldValidationFactory.requiredOnCreate());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Users");

        dataDefinition.addField(fieldUserName);
        dataDefinition.addField(fieldUserGroup);
        dataDefinition.addField(fieldEmail);
        dataDefinition.addField(fieldFirstName);
        dataDefinition.addField(fieldLastName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldPassword);

        return dataDefinition;
    }

    private DataDefinition createUserGroupDefinition() {
        DataDefinition dataDefinition = new DataDefinition("users.group");

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType())
                .withValidator(fieldValidationFactory.requiredOnCreate()).withValidator(fieldValidationFactory.unique())
                .readOnly();
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        DataFieldDefinition fieldRole = createFieldDefinition("role", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Groups");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldRole);

        return dataDefinition;
    }

    private DataDefinition createInstructionDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.instruction");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldMaster = createFieldDefinition("master", fieldTypeFactory.booleanType());
        DataFieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductInstruction");

        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldMaster);
        dataDefinition.addField(fieldDateFrom);
        dataDefinition.addField(fieldDateTo);
        dataDefinition.addField(fieldDescription);

        dataDefinition.addValidator(fieldValidationFactory.customEntity("productService", "checkInstructionDefault")
                .customErrorMessage("products.validation.error.default"));
        dataDefinition.addValidator(fieldValidationFactory.customEntity("productService", "checkInstructionDates")
                .customErrorMessage("products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createOrderDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.order");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldState = createFieldDefinition("state", fieldTypeFactory.enumType("pending", "done"))
                .withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldMachine = createFieldDefinition("machine", fieldTypeFactory.enumType("Maszyna 1", "Maszyna 2"));
        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        DataFieldDefinition fieldDefaultInstruction = createFieldDefinition("defaultInstruction",
                fieldTypeFactory.eagerBelongsToType("products.instruction", "name")).readOnly();
        DataFieldDefinition fieldInstruction = createFieldDefinition("instruction",
                fieldTypeFactory.eagerBelongsToType("products.instruction", "name"));
        DataFieldDefinition fieldPlannedQuantity = createFieldDefinition("plannedQuantity", fieldTypeFactory.decimalType());
        DataFieldDefinition fieldDoneQuantity = createFieldDefinition("doneQuantity", fieldTypeFactory.decimalType());
        DataFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateType())
                .readOnly();
        DataFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateType())
                .readOnly();
        DataFieldDefinition fieldStartWorker = createFieldDefinition("startWorker", fieldTypeFactory.textType()).readOnly();
        DataFieldDefinition fieldEndWorker = createFieldDefinition("endWorker", fieldTypeFactory.textType()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductOrder");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDateFrom);
        dataDefinition.addField(fieldDateTo);
        dataDefinition.addField(fieldState);
        dataDefinition.addField(fieldMachine);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldDefaultInstruction);
        dataDefinition.addField(fieldInstruction);
        dataDefinition.addField(fieldPlannedQuantity);
        dataDefinition.addField(fieldDoneQuantity);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);
        dataDefinition.addField(fieldStartWorker);
        dataDefinition.addField(fieldEndWorker);

        dataDefinition.addValidator(fieldValidationFactory.customEntity("productService", "checkOrderDates").customErrorMessage(
                "products.validation.error.datesOrder"));
        dataDefinition.setOnSave(callbackFactory.getCallback("productService", "fillOrderDatesAndWorkers"));

        return dataDefinition;
    }

    private DataDefinition createDictionaryDefinition() {
        DataDefinition dataDefinition = new DataDefinition("core.dictionary");
        dataDefinition.setFullyQualifiedClassName(Dictionary.class.getCanonicalName());
        dataDefinition.setDeletable(false);

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.addField(fieldName);
        return dataDefinition;
    }

    private DataDefinition createDictionaryItemDefinition() {
        DataDefinition dataDefinition = new DataDefinition("core.dictionaryItem");
        dataDefinition.setFullyQualifiedClassName(DictionaryItem.class.getCanonicalName());

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        DataFieldDefinition fieldDictionary = createFieldDefinition("dictionary",
                fieldTypeFactory.eagerBelongsToType("core.dictionary", "name"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldDictionary);
        return dataDefinition;
    }

    private DataDefinition createPluginDefinition() {
        DataDefinition dataDefinition = new DataDefinition("plugins.plugin");

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType()).readOnly();
        DataFieldDefinition fieldPublisher = createFieldDefinition("publisher", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        DataFieldDefinition fieldVersion = createFieldDefinition("version", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        DataFieldDefinition fieldActive = createFieldDefinition("active", fieldTypeFactory.booleanType()).withValidator(
                fieldValidationFactory.requiredOnCreate());
        DataFieldDefinition fieldBase = createFieldDefinition("base", fieldTypeFactory.booleanType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        DataFieldDefinition fieldCodeId = createFieldDefinition("codeId", fieldTypeFactory.stringType()).readOnly();
        DataFieldDefinition fieldPackageName = createFieldDefinition("packageName", fieldTypeFactory.stringType()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.plugins.Plugin");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldVersion);
        dataDefinition.addField(fieldPublisher);
        dataDefinition.addField(fieldActive);
        dataDefinition.addField(fieldBase);
        dataDefinition.addField(fieldCodeId);
        dataDefinition.addField(fieldPackageName);

        return dataDefinition;
    }

    private DataFieldDefinition createFieldDefinition(final String name, final FieldType type) {
        return new DataFieldDefinition(name).withType(type);
    }

    @Override
    public void delete(final String entityName) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public List<DataDefinition> list() {
        throw new UnsupportedOperationException("implement me");
    }

}
