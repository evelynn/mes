/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.listeners;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.utils.StaffTimeCalculator;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionTrackingDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTrackingDetailsListeners.class);

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private StaffTimeCalculator staffTimeCalculator;

    @Autowired
    private UnitConversionService unitConversionService;

    public void goToProductionTracking(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        if (!grid.getSelectedEntitiesIds().isEmpty()) {
            String url = "../page/productionCounting/productionTrackingDetails.html";
            Entity e = grid.getSelectedEntities().get(0).getDataDefinition().get(grid.getSelectedEntities().get(0).getId());

            view.redirectTo(url, false, true, ImmutableMap.of("form.id", e.getIntegerField("productiontracking_id")));
        }
    }

    public void calcTotalLaborTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Long id = form.getEntityId();
        if (id == null) {
            return;
        }
        Long totalLabor = staffTimeCalculator.countTotalLaborTime(id);
        FieldComponent laborTimeInput = (FieldComponent) view.getComponentByReference("laborTime");
        laborTimeInput.setFieldValue(totalLabor);
    }

    public void copyPlannedQuantityToUsedQuantity(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long productionRecordId = productionRecordForm.getEntityId();

        if (productionRecordId == null) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity().getDataDefinition().get(productionRecordId);

        copyPlannedQuantityToUsedQuantity(
                productionRecord.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS));
        copyPlannedQuantityToUsedQuantity(
                productionRecord.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyPlannedQuantityToUsedQuantity(List<Entity> recordOperationProductComponents) {
        for (Entity recordOperationProductComponent : recordOperationProductComponents) {
            Entity product = recordOperationProductComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
            BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(
                    recordOperationProductComponent.getDecimalField(TrackingOperationProductInComponentFields.PLANNED_QUANTITY));
            recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                    numberService.setScale(plannedQuantity));

            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String baseUnit = product.getStringField(ProductFields.UNIT);
            if (StringUtils.isEmpty(additionalUnit)) {
                recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                        numberService.setScale(plannedQuantity));
                recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, baseUnit);
            } else {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
                if (unitConversions.isDefinedFor(additionalUnit)) {
                    BigDecimal convertedQuantity = unitConversions.convertTo(plannedQuantity, additionalUnit);
                    recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                            convertedQuantity);
                } else {
                    recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                            numberService.setScale(plannedQuantity));
                }
                recordOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, additionalUnit);
            }
            recordOperationProductComponent.getDataDefinition().save(recordOperationProductComponent);
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        productionTrackingService.changeProducedQuantityFieldState(viewDefinitionState);
        Object recordingTypeValue = ((FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).getFieldValue();
        boolean recordingTypeEqualsCumulated = TypeOfProductionRecording.CUMULATED.getStringValue().equals(recordingTypeValue);
        boolean recordingTypeEqualsForEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingTypeValue);
        if (recordingTypeEqualsCumulated || recordingTypeEqualsForEach) {
            for (String componentName : Arrays.asList(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
                    OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.REGISTER_PRODUCTION_TIME, OrderFieldsPC.JUST_ONE,
                    OrderFieldsPC.ALLOW_TO_CLOSE, OrderFieldsPC.AUTO_CLOSE_ORDER, OrderFieldsPC.REGISTER_PIECEWORK)) {
                ComponentState component = viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void enableOrDisableFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("order is null");
            }
            return;
        }
        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);
        return lookup.getEntity();
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.LAST_TRACKING);
        boolean justOneRecord = order.getBooleanField(OrderFieldsPC.JUST_ONE);
        if (justOneRecord) {
            lastRecord.setFieldValue(justOneRecord);
            lastRecord.setEnabled(!justOneRecord);
            lastRecord.requestComponentUpdateState();
        } else {
            lastRecord.setEnabled(true);
        }
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent operation = (FieldComponent) view.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);
        operation.setFieldValue("");
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        GridComponent productsIn = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        GridComponent productOut = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        productOut.setEntities(new ArrayList<>());
        productsIn.setEntities(new ArrayList<>());
    }

    public void fillShiftAndDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.STAFF);
        LookupComponent shiftLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.SHIFT);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity staff = staffLookup.getEntity();

        if (staff == null) {
            shiftLookup.setFieldValue(null);

            return;
        }

        Entity shift = staff.getBelongsToField(ProductionTrackingFields.SHIFT);

        if (shift == null) {
            shiftLookup.setFieldValue(null);
        } else {
            shiftLookup.setFieldValue(shift.getId());
        }

        Entity division = staff.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    public void fillDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        LookupComponent workstationTypeLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.WORKSTATION_TYPE);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity workstationType = workstationTypeLookup.getEntity();

        if (workstationType == null) {
            divisionLookup.setFieldValue(null);

            return;
        }

        Entity division = workstationType.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    public void fillWorkstationTypeField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        LookupComponent tocLookup = (LookupComponent) view.getComponentByReference("technologyOperationComponent");
        LookupComponent workstationTypeLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.WORKSTATION_TYPE);
        Entity toc = tocLookup.getEntity();

        if (toc == null) {
            return;
        }

        Entity workstationType = toc.getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
        if (workstationType == null) {
            workstationTypeLookup.setFieldValue(null);
        } else {
            workstationTypeLookup.setFieldValue(workstationType.getId());
        }

    }
}
