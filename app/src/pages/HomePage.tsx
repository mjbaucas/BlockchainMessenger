import React, { useState, useEffect } from "react";
import Select from "react-select";
import httpClient from "../httpClient.ts";
import LoginPage from "./LoginPage.tsx";
import { User } from "../types";
import '../App.css';

const HomePage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [friendOptions, setFriends] = useState([]);
  const [messagesList, setMessages] = useState([]);
  const [selectedFriend, setFriend] = useState(null);
  const [messageInput, setMessageInput] = useState<string>("");

  const logoutUser = async () => {
    await httpClient.post("/logout");
    window.location.href = "/";
  };

  const handleSelectedFriend = async (selectedOption) => {
    setFriend(selectedOption)
    if(selectedOption.value != null){
      const resp = await httpClient.post("/messages", {
        friend: selectedOption.value
      });
      setMessages(resp.data.messages);  
    }
  }

  const generateMessages = () => {
    console.log(messagesList)
    const messages = messagesList.map((message, index) => { 
      if (user != null){
        if (message['from'] === user.username) {
          return <div className="Message-main" key={index}><div className="Message-right">{message['message']}</div></div>
        } else {
          return <div className="Message-main" key={index}><div className="Message-left">{message['message']}</div></div>
        }
      }
    })
    return <div>{messages}</div>
  }

  const sendMessage = async () => {
    try {
      const resp = await httpClient.post("/message/send", {
        from: user?.username,
        to: selectedFriend?.['value'],
        message: messageInput
      });

      if(selectedFriend?.['value'] != null){
        const resp = await httpClient.post("/messages", {
          friend: selectedFriend?.['value']
        });
        setMessages(resp.data.messages); 
      }
      setMessageInput("")
      if (resp.data.status === 403) {
        alert("Error Verifying Message Details");
      }
    } catch (error: any) {
      if (error.response.status === 401) {
        alert("Error Verifying Message Details");
      }
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const check_resp = await httpClient.get("/check");
        if (check_resp.data.username === undefined){
          setUser(null);
        } else {
          setUser({username: check_resp.data.username});
          const friend_resp = await httpClient.get("/friendslist");
          if (friend_resp.data.friends.length > 0){
            for (let i = 0; i<friend_resp.data.friends.length; i++){
              const temp = friend_resp.data.friends[i];
              friend_resp.data.friends[i] = { value: temp, label: temp}
            }
            setFriends(friend_resp.data.friends);
          }
        }
      } catch (error) {
        console.log("Not Authenticated");
      }
    })();
  }, []);
  
  return (
    <div>
      <div className="Home-header">
        <h1 className="Home-header-override" >Welcome to Blockchain Messenger</h1>
      </div>
      {user != null ? (
        <div>
          <div className="Home-user-container">
            <h2 className="Home-details">Logged in user: {user.username}</h2>
            <button className="Home-logout" onClick={logoutUser}>Logout</button>
          </div>
          <div className="Home-content">
            {friendOptions.length > 0 ? (
              <div>
                <div>
                  <h3 className="Friendlist-header">
                    Friend List: 
                  </h3>
                  <div >
                    <Select className="Friendlist-select" options={friendOptions} onChange={handleSelectedFriend} />  
                  </div>  
                </div>  
                <div className="Home-selected-friend">
                  {selectedFriend != null ? (
                    <div>
                      <h2 className="Message-header">{selectedFriend['label']}</h2>
                      <div className="Home-message-box"> 
                          {messagesList.length > 0 ? (
                            generateMessages()
                          ):(
                            <div> No Messages to Load... </div>
                          )}
                      </div>
                      <div className="Message-send-box">
                        <input
                          type="text"
                          className="Message-input"
                          value={messageInput}
                          onChange={(e) => setMessageInput(e.target.value)}
                          id="message"
                        />
                        <button type="button" className="Message-send-button"  onClick={() => sendMessage()}>
                          Send
                        </button>
                      </div>
                    </div>
                  ):(
                    <div> No one selected... </div>
                  )}
                </div>
               </div>
            ) : (
              <div>
                Nothing to display...
              </div>
            )}
          </div>
        </div>
      ) : (
        <LoginPage />
      )}
    </div>
  )
};

export default HomePage;