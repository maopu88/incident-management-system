import React, { useState } from "react";
import Swal from "sweetalert2";
import { updateItem } from "../utils/http";

const Edit = ({ selectedEvent, setIsEditing, getList }) => {
  const [title, setTitle] = useState(selectedEvent.title);
  const [description, setDescription] = useState(selectedEvent.description);
  const [status, setStatus] = useState(selectedEvent.status);

  const handleStatusChange = (event) => {
    setStatus(event.target.value);
  };

  const options = [
    { value: "RESOLVED", label: "resolved" },
    { value: "OPEN", label: "open" },
    { value: "CLOSED", label: "closed" },
    { value: "IN_PROGRESS", label: "in progress" },
  ];

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!title || !description || !status) {
      return Swal.fire({
        icon: "error",
        title: "Error!",
        text: "All fields are required.",
        showConfirmButton: true,
      });
    }

    const newEvent = {
      title,
      description,
      status,
      id: selectedEvent.id,
    };

    try {
      let res = await updateItem(newEvent);
      if (res.status === 200) {
        setIsEditing(false);
        getList();
        Swal.fire({
          icon: "success",
          title: "Updated!",
          text: `${title}' has been updated.`,
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
  };

  return (
    <div className="small-container">
      <form onSubmit={handleAdd}>
        <h1>Edit Incident</h1>
        <label htmlFor="title">Incident Name</label>
        <input
          id="title"
          type="text"
          name="title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <label htmlFor="description">Incident Description</label>
        <input
          id="description"
          type="text"
          name="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <label htmlFor="status">Incident Status</label>
        <select id="status" value={status} onChange={handleStatusChange}>
          <option value="">--Please choose an option--</option>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <div style={{ marginTop: "30px" }}>
          <input type="submit" value="Update" />
          <input
            style={{ marginLeft: "12px" }}
            className="muted-button"
            type="button"
            value="Cancel"
            onClick={() => setIsEditing(false)}
          />
        </div>
      </form>
    </div>
  );
};

export default Edit;
