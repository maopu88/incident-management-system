import React, { useState, useEffect } from "react";
import Swal from "sweetalert2";

import Header from "@/components/Header";
import MyTable from "@/components/Table";
import Add from "@/components/Add";
import Edit from "@/components/Edit";

import { fetchList, deleteItem } from "@/utils/http";

const Dashboard = ({ setIsAuthenticated }) => {
  const [dataSource, setDataSource] = useState();
  const [searchText, setSearchText] = useState("");
  const [selectedEvent, setselectedEvent] = useState(null);
  const [isAdding, setIsAdding] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 2,
    total: 0,
  });

  useEffect(() => {
    getList();
  }, [pagination.current, pagination.pageSize]);

  const getList = async () => {
    let params = {
      page: pagination.current-1,
      size: pagination.pageSize,
      title: searchText,
    };

    let res = await fetchList(params);
    console.log(res, "res===");
    setDataSource(res.data.content);
    setPagination({
      ...pagination,
      total: res.data.totalElements,
    });
  };

  const handleSearch = () => {
    getList();
  };

  const handleEdit = (id) => {
    const [event] = dataSource.filter((item) => item.id === id);
    setselectedEvent(event);
    setIsEditing(true);
  };

  const handleDelete = (data) => {
    Swal.fire({
      icon: "warning",
      title: "Are you sure?",
      text: "You won't be able to revert this!",
      showCancelButton: true,
      confirmButtonText: "Yes, delete it!",
      cancelButtonText: "No, cancel!",
      customClass: {
        confirmButton: "my-confirm-btn",
        cancelButton: "my-cancel-btn",
      },
    }).then(async (result) => {
      if (result.value) {
        try {
          let res = await deleteItem(data.id);
          if (res.status === 200) {
            setIsEditing(false);
            getList();
            Swal.fire({
              icon: "success",
              title: "Updated!",
              text: `${data.title}' has been deleted.`,
              showConfirmButton: false,
              timer: 1500,
            });
          }
        } catch (error) {
          console.log(error);
          Swal.fire({
            icon: "error",
            title: "Error!",
            text: error.response.data.message,
            showConfirmButton: true,
          });
        }
      }
    });
  };

  return (
    <div className="container">
      {!isAdding && !isEditing && (
        <>
          <Header
            setIsAdding={setIsAdding}
            setIsAuthenticated={setIsAuthenticated}
            setSearchText={setSearchText}
            searchText={searchText}
            handleSearch={handleSearch}
          />
          <MyTable
            dataSource={dataSource}
            handleEdit={handleEdit}
            handleDelete={handleDelete}
            pagination={pagination}
            setPagination={setPagination}
          />
        </>
      )}
      {isAdding && <Add setIsAdding={setIsAdding} getList={getList} />}
      {isEditing && (
        <Edit
          selectedEvent={selectedEvent}
          setIsEditing={setIsEditing}
          getList={getList}
        />
      )}
    </div>
  );
};

export default Dashboard;
