import axios from "axios";

let baseURL = "/api";
const service = axios.create({
  baseURL: baseURL,
  timeout: 10000,
});

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// response
service.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          break;
        case 403:
          break;
      }
    }
    return Promise.reject(error);
  }
);

export function fetchList(params) {
  return service({
    url: "/incidents",
    method: "GET",
    params,
  });
}

export function addItem(data) {
  return service({
    url: "/incidents",
    method: "POST",
    data,
  });
}

export function updateItem(data) {
  return service({
    url: `/incidents/${data.id}`,
    method: "PUT",
    data,
  });
}

export function deleteItem(id) {
  return service({
    url: `/incidents/${id}`,
    method: "DELETE",
  });
}
