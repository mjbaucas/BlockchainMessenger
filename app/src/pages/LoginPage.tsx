import React, { useState } from "react";
import httpClient from "../httpClient.ts";

const LoginPage: React.FC = () => {
    const [userInput, setUserInput] = useState<string>("");
    const [passwordInput, setPasswordInput] = useState<string>("");

    const loginUser = async () => {
        try {
          const resp = await httpClient.post("/login", {
            username: userInput,
            password: passwordInput,
          });
          console.log(resp)
          if (resp.data.status === 401) {
            alert("Unknown/Wrong Login");
          }
          window.location.href ="/";
        } catch (error: any) {
          if (error.response.status === 401) {
            alert("Unknown/Wrong Login");
          }
        }
      };

  return (
    <div className="Login-container">
        <h2 className="Login-header">You are not logged in. Please login.</h2>
        <div>
        <form>
        <div>
            <label className="Login-label">Username: </label>
            <input
            type="text"
            className="Login-input"
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            id="username"
            />
        </div>
        <div>
            <label className="Login-label">Password: </label>
            <input
            type="password"
            className="Login-input"
            value={passwordInput}
            onChange={(e) => setPasswordInput(e.target.value)}
            id="password"
            />
        </div>
        <button className="Login-button" type="button" onClick={() => loginUser()}>
            Login
        </button>
        </form>
    </div>
    </div>
    )
};

export default LoginPage;