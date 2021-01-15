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

import { fireEvent, render } from "@testing-library/react";
import { usingTestingBoxedExpressionI18nContext } from "../test-utils";
import { Table } from "../../../components/Table";
import * as _ from "lodash";
import * as React from "react";
import { DataType, TableHandlerConfiguration, TableOperation } from "../../../api";
import { Column, ColumnInstance, DataRecord } from "react-table";
import { act } from "react-dom/test-utils";

jest.useFakeTimers();
const flushPromises = () => new Promise((resolve) => process.nextTick(resolve));

const EDIT_EXPRESSION_NAME: string = "[data-ouia-component-id='edit-expression-name']";
const EDIT_EXPRESSION_DATA_TYPE: string = "[data-ouia-component-id='edit-expression-data-type'] input";
const EXPRESSION_COLUMN_HEADER: string = "[data-ouia-component-type='expression-column-header']";
const EXPRESSION_POPOVER_MENU: string = "[data-ouia-component-id='expression-popover-menu']";
const EXPRESSION_POPOVER_MENU_TITLE: string = "[data-ouia-component-id='expression-popover-menu-title']";
const EXPRESSION_TABLE_HANDLER_MENU: string = "[data-ouia-component-id='expression-table-handler-menu']";
const expressionRow = (index: number) => {
  return "[data-ouia-component-id='expression-row-" + index + "']";
}

const expressionCell = (rowIndex: number, columnIndex: number) => {
  return "[data-ouia-component-id='expression-row-" + rowIndex + "'] [data-ouia-component-id='expression-column-" + columnIndex + "']";
}

const expressionTableHandlerMenuEntry = (menuEntry: string) => {
  return "[data-ouia-component-id='expression-table-handler-menu-" + menuEntry + "']";
}

describe("Table tests", () => {

  const columnName = "column-1";
  const handlerConfiguration: TableHandlerConfiguration = [];

  describe("when rendering it", () => {
    test("should show a table element", () => {
      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[]}
            rows={[]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      expect(container.querySelector(".table-component table")).toBeTruthy();
    });

    test("should show a table head with only one default column (#)", () => {
      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[]}
            rows={[]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      expect(container.querySelector(".table-component table thead")).toBeTruthy();
      expect(container.querySelector(".table-component table thead tr")).toBeTruthy();
      expect(container.querySelectorAll(EXPRESSION_COLUMN_HEADER).length).toBe(1);
      expect(container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[0].innerHTML).toContain("#");
    });

    test("should show a table head with one configured column", () => {
      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ accessor: columnName, label: columnName, dataType: DataType.Undefined } as ColumnInstance]}
            rows={[]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      expect(container.querySelector(".table-component table thead")).toBeTruthy();
      expect(container.querySelector(".table-component table thead tr")).toBeTruthy();
      expect(container.querySelectorAll(EXPRESSION_COLUMN_HEADER).length).toBe(2);
      expect(container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1].innerHTML).toContain(columnName);
    });

    test("should show a table body with no rows", () => {
      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[]}
            rows={[]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      expect(container.querySelector(".table-component table tbody")).toBeTruthy();
      expect(container.querySelector(".table-component table tbody tr")).toBeFalsy();
    });

    test("should show a table body with one configured row", () => {
      const row: DataRecord = {};
      const cellValue = "cell value";
      row[columnName] = cellValue;
      const rows: DataRecord[] = [row];

      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ accessor: columnName, dataType: DataType.Undefined } as ColumnInstance]}
            rows={rows}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      expect(container.querySelector(".table-component table tbody")).toBeTruthy();
      expect(container.querySelector(expressionRow(0))).toBeTruthy();
      expect(container.querySelectorAll(expressionRow(0) + " td")).toHaveLength(2);
      expect(container.querySelector(expressionCell(0, 0))!.innerHTML).toContain("1");
      expect(container.querySelector(expressionCell(0, 1))!.innerHTML).toContain(cellValue);
    });
  });

  describe("when interacting with header", () => {
    test("should render popover with column name and dataType, when clicking on header cell", async () => {
      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Boolean } as ColumnInstance]}
            rows={[]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );

      await activateNameAndDataTypePopover(
        container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1] as HTMLTableHeaderCellElement
      );

      expect(baseElement.querySelector(EXPRESSION_POPOVER_MENU)).toBeTruthy();
      expect(baseElement.querySelector(EXPRESSION_POPOVER_MENU_TITLE)?.innerHTML).toBe("Edit Relation");
      expect(
        (baseElement.querySelector(EDIT_EXPRESSION_NAME)! as HTMLInputElement).value
      ).toBe(columnName);
      expect(
        (baseElement.querySelector(EDIT_EXPRESSION_DATA_TYPE)! as HTMLInputElement).value
      ).toBe(DataType.Boolean);
    });

    test("should trigger onColumnUpdate, when changing column name via popover", async () => {
      const newColumnName = "changed";
      const onColumnUpdate = (columns: Column[]) => {
        _.identity(columns);
      };
      const mockedOnColumnUpdate = jest.fn(onColumnUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Boolean } as ColumnInstance]}
            rows={[]}
            onColumnsUpdate={mockedOnColumnUpdate}
            onRowsUpdate={_.identity}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );
      await updateElementViaPopover(container, baseElement, newColumnName, mockedOnColumnUpdate);

      expect(mockedOnColumnUpdate).toHaveBeenCalledWith([
        { label: newColumnName, accessor: newColumnName, dataType: DataType.Boolean } as ColumnInstance,
      ]);
    });

    test("should trigger onRowsUpdate, when changing the column name, via popover, of a column with a value in an existing row", async () => {
      const newColumnName = "changed";
      const row: DataRecord = {};
      const newRow: DataRecord = {};
      const rowValue = "value";
      row[columnName] = rowValue;
      newRow[newColumnName] = rowValue;
      const orRowsUpdate = (rows: DataRecord[]) => {
        _.identity(rows);
      };
      const mockedOnRowsUpdate = jest.fn(orRowsUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Boolean } as ColumnInstance]}
            rows={[row]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={mockedOnRowsUpdate}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );
      await updateElementViaPopover(container, baseElement, newColumnName, mockedOnRowsUpdate);

      expect(mockedOnRowsUpdate).toHaveBeenCalledWith([newRow]);
    });
  });

  describe("when interacting with body", () => {
    test("should trigger onRowsUpdate, when changing cell value", async () => {
      const row: DataRecord = {};
      const newRow: DataRecord = {};
      const rowValue = "value";
      const newRowValue = "new value";
      row[columnName] = rowValue;
      newRow[columnName] = newRowValue;
      const orRowsUpdate = (rows: DataRecord[]) => {
        _.identity(rows);
      };
      const mockedOnRowsUpdate = jest.fn(orRowsUpdate);

      const { container } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Boolean } as ColumnInstance]}
            rows={[row]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={mockedOnRowsUpdate}
            handlerConfiguration={handlerConfiguration}
          />
        ).wrapper
      );
      fireEvent.change(container.querySelector("table tbody tr td textarea")! as HTMLTextAreaElement, {
        target: { value: newRowValue },
      });
      fireEvent.blur(container.querySelector("table tbody tr td textarea")! as HTMLTextAreaElement);

      expect(mockedOnRowsUpdate).toHaveBeenCalled();
      expect(mockedOnRowsUpdate).toHaveBeenCalledWith([newRow]);
    });
  });

  describe("when interacting with context menu", () => {
    test("should trigger onColumnUpdate, when inserting a new column on the left", async () => {
      const firstColumn = { label: "column-2", accessor: "column-2", dataType: DataType.Undefined } as ColumnInstance;
      const secondColumn = { label: "column-3", accessor: "column-3", dataType: DataType.Undefined } as ColumnInstance;
      const onColumnUpdate = (columns: Column[]) => {
        _.identity(columns);
      };
      const mockedOnColumnUpdate = jest.fn(onColumnUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[firstColumn]}
            rows={[]}
            onColumnsUpdate={mockedOnColumnUpdate}
            onRowsUpdate={_.identity}
            handlerConfiguration={[
              {
                group: "COLUMNS",
                items: [{ name: "Insert Column Left", type: TableOperation.ColumnInsertLeft }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1]);
      await selectMenuEntry(baseElement, "Insert Column Left");

      expect(mockedOnColumnUpdate).toHaveBeenCalledWith([secondColumn, firstColumn]);
    });

    test("should trigger onColumnUpdate, when inserting a new column on the right", async () => {
      const firstColumn = { label: "column-2", accessor: "column-2", dataType: DataType.Undefined } as ColumnInstance;
      const secondColumn = { label: "column-3", accessor: "column-3", dataType: DataType.Undefined } as ColumnInstance;
      const onColumnUpdate = (columns: Column[]) => {
        _.identity(columns);
      };
      const mockedOnColumnUpdate = jest.fn(onColumnUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[firstColumn]}
            rows={[]}
            onColumnsUpdate={mockedOnColumnUpdate}
            onRowsUpdate={_.identity}
            handlerConfiguration={[
              {
                group: "COLUMNS",
                items: [{ name: "Insert Column right", type: TableOperation.ColumnInsertRight }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1]);
      await selectMenuEntry(baseElement, "Insert Column right");

      expect(mockedOnColumnUpdate).toHaveBeenCalledWith([firstColumn, secondColumn]);
    });

    test("should trigger onColumnUpdate, when deleting a column", async () => {
      const firstColumn = { label: "column-2", accessor: "column-2", dataType: DataType.Undefined } as ColumnInstance;
      const secondColumn = { label: "column-3", accessor: "column-3", dataType: DataType.Undefined } as ColumnInstance;
      const onColumnUpdate = (columns: Column[]) => {
        _.identity(columns);
      };
      const mockedOnColumnUpdate = jest.fn(onColumnUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[firstColumn, secondColumn]}
            rows={[]}
            onColumnsUpdate={mockedOnColumnUpdate}
            onRowsUpdate={_.identity}
            handlerConfiguration={[
              {
                group: "COLUMNS",
                items: [{ name: "Delete", type: TableOperation.ColumnDelete }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1]);
      await selectMenuEntry(baseElement, "Delete");

      expect(mockedOnColumnUpdate).toHaveBeenCalledWith([secondColumn]);
    });

    test("should trigger onRowsUpdate, when inserting a new row above", async () => {
      const row: DataRecord = {};
      row[columnName] = "value";
      const onRowsUpdate = (rows: DataRecord[]) => {
        _.identity(rows);
      };
      const mockedOnRowsUpdate = jest.fn(onRowsUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Undefined } as ColumnInstance]}
            rows={[row]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={mockedOnRowsUpdate}
            handlerConfiguration={[
              {
                group: "ROWS",
                items: [{ name: "Insert row above", type: TableOperation.RowInsertAbove }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelector(expressionCell(0, 1))!);
      await selectMenuEntry(baseElement, "Insert row above");

      expect(mockedOnRowsUpdate).toHaveBeenCalledWith([{}, row]);
    });

    test("should trigger onRowsUpdate, when inserting a new row below", async () => {
      const row: DataRecord = {};
      row[columnName] = "value";
      const onRowsUpdate = (rows: DataRecord[]) => {
        _.identity(rows);
      };
      const mockedOnRowsUpdate = jest.fn(onRowsUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Undefined } as ColumnInstance]}
            rows={[row]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={mockedOnRowsUpdate}
            handlerConfiguration={[
              {
                group: "ROWS",
                items: [{ name: "Insert row below", type: TableOperation.RowInsertBelow }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelector(expressionCell(0, 1))!);
      await selectMenuEntry(baseElement, "Insert row below");

      expect(mockedOnRowsUpdate).toHaveBeenCalledWith([row, {}]);
    });

    test("should trigger onRowsUpdate, when deleting a row", async () => {
      const firstRow: DataRecord = {};
      const secondRow: DataRecord = {};
      firstRow[columnName] = "value";
      secondRow[columnName] = "another value";
      const onRowsUpdate = (rows: DataRecord[]) => {
        _.identity(rows);
      };
      const mockedOnRowsUpdate = jest.fn(onRowsUpdate);

      const { container, baseElement } = render(
        usingTestingBoxedExpressionI18nContext(
          <Table
            columnPrefix="column-"
            columns={[{ label: columnName, accessor: columnName, dataType: DataType.Undefined } as ColumnInstance]}
            rows={[firstRow, secondRow]}
            onColumnsUpdate={_.identity}
            onRowsUpdate={mockedOnRowsUpdate}
            handlerConfiguration={[
              {
                group: "ROWS",
                items: [{ name: "Delete", type: TableOperation.RowDelete }],
              },
            ]}
          />
        ).wrapper
      );
      await openContextMenu(container.querySelector(expressionCell(0, 1))!);
      await selectMenuEntry(baseElement, "Delete");

      expect(mockedOnRowsUpdate).toHaveBeenCalledWith([secondRow]);
    });
  });
});

async function selectMenuEntry(baseElement: Element, menuEntry: string) {
  await act(async () => {
    expect(baseElement.querySelector(EXPRESSION_TABLE_HANDLER_MENU)).toBeTruthy();
    (baseElement.querySelector(expressionTableHandlerMenuEntry(menuEntry) + " button") as HTMLButtonElement).click();
    await flushPromises();
    jest.runAllTimers();
  });
}

async function openContextMenu(element: Element) {
  await act(async () => {
    fireEvent.contextMenu(element);
    await flushPromises();
    jest.runAllTimers();
  });
}

async function activateNameAndDataTypePopover(element: HTMLElement): Promise<void> {
  await act(async () => {
    element.click();
    await flushPromises();
    jest.runAllTimers();
  });
}

async function updateElementViaPopover(
  container: Element,
  baseElement: Element,
  newName: string,
  updateFn: jest.Mock<void, [Column[] | DataRecord[]]>
) {
  await activateNameAndDataTypePopover(
    container.querySelectorAll(EXPRESSION_COLUMN_HEADER)[1] as HTMLTableHeaderCellElement
  );
  (baseElement.querySelector(EDIT_EXPRESSION_NAME)! as HTMLInputElement).value = newName;
  (baseElement.querySelector(EDIT_EXPRESSION_NAME)! as HTMLInputElement).dispatchEvent(
    new Event("change")
  );
  (baseElement.querySelector(EDIT_EXPRESSION_NAME)! as HTMLInputElement).dispatchEvent(
    new Event("blur")
  );

  expect(baseElement.querySelector(EXPRESSION_POPOVER_MENU)).toBeTruthy();
  expect(updateFn).toHaveBeenCalled();
}
