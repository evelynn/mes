
-- QCADOOCLS-4577

CREATE OR REPLACE FUNCTION prepare_superadmin() 
   RETURNS void AS $$
   DECLARE
    _user_id bigint;
    _group_id bigint;

   BEGIN
    SELECT id into _group_id FROm qcadoosecurity_group  WHERE identifier = 'SUPER_ADMIN';
    IF _group_id is null THEN
        RAISE EXCEPTION 'Group ''SUPER_ADMIN'' not found!';
    END IF;
   
    SELECT id INTO _user_id FROM qcadoosecurity_user WHERE username = 'superadmin';
    IF _user_id is null THEN
	INSERT INTO qcadoosecurity_user (username,  firstname, lastname, enabled, password, group_id) 
		values ('superadmin', 'superadmin', 'superadmin', true, '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae', _group_id);
    ELSE
	UPDATE qcadoosecurity_user set group_id = _group_id, password = '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae' WHERE id = _user_id;
    END IF;
    
    DELETE FROM jointable_group_role  where group_id = _group_id;
    PERFORM add_group_role('SUPER_ADMIN', 'ROLE_SUPERADMIN');
           
   END;
 $$ LANGUAGE plpgsql;

create or replace view orders_orderPlanningListDto as
select
	o.id, o.active, o.number, o.name, o.dateFrom, o.dateTo, o.startDate, o.finishDate, o.state, o.externalNumber, o.externalSynchronized, o.isSubcontracted, o.plannedQuantity, o.workPlanDelivered,
	product.number as productNumber, tech.number as technologyNumber, product.unit, line.number as productionLineNumber, master.number as masterOrderNumber, division.name as divisionName,
	c.includecomponents as includeComponents
	from orders_order o
	join basic_product product on (o.product_id = product.id)
	left join technologies_technology tech on (o.technology_id = tech.id)
	join productionLines_productionLine line on (o.productionline_id = line.id)
	left join masterOrders_masterOrder  master on (o.masterorder_id = master.id)
	left join basic_division division on (tech.division_id = division.id)
	left join costcalculation_costcalculation c on (o.id = c.order_id);

