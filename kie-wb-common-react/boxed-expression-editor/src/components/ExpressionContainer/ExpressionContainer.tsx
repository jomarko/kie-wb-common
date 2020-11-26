/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import * as React from "react";
import { useCallback, useState } from "react";
import * as _ from "lodash";
import "./ExpressionContainer.css";
import { useBoxedExpressionEditorI18n } from "../../i18n";
import {
  Dropdown,
  DropdownItem,
  KebabToggle,
  SimpleList,
  SimpleListItem,
  SimpleListItemProps,
} from "@patternfly/react-core";
import { PopoverMenu } from "../PopoverMenu";
import { LogicType } from "../../api/LogicType";

export interface ExpressionContainerProps {
  /** The name of the expression */
  name: string;
  /** Selected expression is already present */
  selectedExpression?: LogicType;
}

export const ExpressionContainer: ({ name, selectedExpression }: ExpressionContainerProps) => JSX.Element = (
  props: ExpressionContainerProps
) => {
  const { i18n } = useBoxedExpressionEditorI18n();

  const [logicTypeIsPresent, setLogicTypeSelected] = useState(!_.isEmpty(props.selectedExpression));
  const [actionDropdownIsOpen, setActionDropDownOpen] = useState(false);
  const [selectedExpression, setSelectedExpression] = useState(props.selectedExpression);

  const onLogicTypeSelect = useCallback(
    (currentItem: React.RefObject<HTMLButtonElement>, currentItemProps: SimpleListItemProps) => {
      setLogicTypeSelected(true);
      setSelectedExpression(currentItemProps.children as LogicType);
    },
    []
  );

  const executeClearAction = useCallback(() => {
    setLogicTypeSelected(false);
    setSelectedExpression(undefined);
  }, []);

  const renderExpressionActionsDropdown = useCallback(() => {
    return (
      <Dropdown
        onSelect={() => setActionDropDownOpen(!actionDropdownIsOpen)}
        toggle={<KebabToggle onToggle={(isOpen) => setActionDropDownOpen(isOpen)} id="expression-actions-toggle" />}
        isOpen={actionDropdownIsOpen}
        isPlain
        dropdownItems={[
          <DropdownItem key="clear" onClick={executeClearAction} isDisabled={!logicTypeIsPresent}>
            {i18n.clear}
          </DropdownItem>,
        ]}
      />
    );
  }, [i18n.clear, actionDropdownIsOpen, logicTypeIsPresent, executeClearAction]);

  const getLogicTypesWithoutUndefined = useCallback(() => {
    return Object.values(LogicType).filter((logicType) => logicType !== LogicType.Undefined);
  }, []);

  const renderLogicTypeItems = useCallback(() => {
    return _.map(getLogicTypesWithoutUndefined(), (key) => <SimpleListItem key={key}>{key}</SimpleListItem>);
  }, [getLogicTypesWithoutUndefined]);

  const buildLogicSelectorMenu = useCallback(() => {
    return (
      <PopoverMenu
        title={i18n.selectLogicType}
        arrowPlacement={() => document.getElementById("expression-container-box")!}
        body={<SimpleList onSelect={onLogicTypeSelect}>{renderLogicTypeItems()}</SimpleList>}
      />
    );
  }, [i18n.selectLogicType, onLogicTypeSelect, renderLogicTypeItems]);

  return (
    <div className="expression-container">
      <span id="expression-title">{props.name || ""}</span>
      <span id="expression-type">({selectedExpression || LogicType.Undefined})</span>
      <span id="expression-actions">{renderExpressionActionsDropdown()}</span>

      <div
        id="expression-container-box"
        className={logicTypeIsPresent ? "logic-type-selected" : "logic-type-not-present"}
      >
        {selectedExpression || i18n.selectExpression}
      </div>

      {!logicTypeIsPresent ? buildLogicSelectorMenu() : null}
    </div>
  );
};
