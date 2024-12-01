import React, { useState, useEffect } from "react";
import { Table, Divider, Pagination } from "antd";
import dayjs from "dayjs";

const MyTable = ({
  dataSource,
  handleEdit,
  handleDelete,
  pagination,
  setPagination,
}) => {
  const columns = [
    {
      title: "Incident Name",
      dataIndex: "title",
      key: "title",
    },
    {
      title: "Incident Description",
      dataIndex: "description",
      key: "description",
      style: { minWidth: "200px" },
    },
    {
      title: "Create Date",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (text, record) => (
        <span>{dayjs(record.createdAt).format("DD/MM/YYYY HH:mm:ss")}</span>
      ),
      style: { minWidth: "200px" },
    },
    {
      title: "Update Date",
      dataIndex: "updatedAt",
      key: "updatedAt",
      render: (text, record) => (
        <span>{dayjs(record.updatedAt).format("DD/MM/YYYY HH:mm:ss")}</span>
      ),
      style: { minWidth: "200px" },
    },
    {
      title: "Incident Status",
      dataIndex: "status",
      key: "status",
    },

    {
      title: "Action",
      key: "action",
      fixed: "right",
      render: (text, record) => (
        <span>
          <button
            onClick={() => handleEdit(record.id)}
            className="button muted-button"
          >
            Edit
          </button>
          <Divider type="vertical" />
          <button
            onClick={() => handleDelete(record)}
            className="button muted-button"
          >
            Delete
          </button>
        </span>
      ),
    },
  ];

  return (
    <div className="contain-table">
      <Table
        columns={columns}
        dataSource={dataSource}
        pagination={false}
        rowKey={(record) => record.id}
      ></Table>
      <div className="pagination-wrap">
        <Pagination
          showSizeChanger
          showQuickJumper
          current={pagination.current}
          total={pagination.total}
          pageSize={pagination.pageSize}
          onChange={(current, size) =>
            setPagination({ ...pagination, current, pageSize: size })
          }
          onShowSizeChange={(current, size) =>
            setPagination({ ...pagination, current: 1, pageSize: size })
          }
        />
      </div>
    </div>
  );
};

export default MyTable;
