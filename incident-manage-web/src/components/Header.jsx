import React from "react";

import Logout from "@/views/Logout";
import { Input } from "antd";

const Header = ({
  setIsAdding,
  setIsAuthenticated,
  searchText,
  setSearchText,
  handleSearch,
}) => {
  return (
    <header>
      <h1>Incident Management</h1>

      <div
        style={{ marginTop: "30px", marginBottom: "18px" }}
        className="operate-header"
      >
        <Input.Search
          placeholder="Search event by name"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          onSearch={handleSearch}
          style={{ width: 220 }}
        />
        <div>
          <button onClick={() => setIsAdding(true)}>Add Incident</button>
          <Logout setIsAuthenticated={setIsAuthenticated} />
        </div>
      </div>
    </header>
  );
};

export default Header;
