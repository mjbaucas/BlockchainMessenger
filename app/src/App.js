import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component{
  constructor(props) {
    super(props);
    this.state = {
      currentUser: "None",
      userInput: "",
      passwordInput: ""
    }

    this.checkUser = this.checkUser.bind(this);
    this.logoutClick = this.logoutClick.bind(this);
    this.loginClick = this.loginClick.bind(this);
    this.handleUsernameChange = this.handleUsernameChange.bind(this);
    this.handlePasswordChange = this.handlePasswordChange.bind(this);
  }

  checkUser = () => {
    fetch('/check').then(res => res.json()).then(data => { 
     if(data.response === 200) {
        this.setState({
          currentUser: data.username
        });
      } else {
        this.setState({
          currentUser: "None"
        });
      }
    })
  }
    
  logoutClick = () => {
    fetch('/logout', {method:'POST'}).then(this.checkUser())
  }

  loginClick = () => {
    fetch('/login', {
      method:'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: this.state.userInput,
        password: this.state.passwordInput
      })
    }).then(this.checkUser())
  }

  handleUsernameChange(event) {
    this.setState({userInput: event.target.value});
  }

  handlePasswordChange(event) {
    this.setState({passwordInput: event.target.value});
  }

  renderLoginScreen = () => {
    this.checkUser();
    if(this.state.currentUser === "None"){
      return(
        <div>
          <h1> Please login</h1>
          <input name="username" onChange={this.handleUsernameChange} />
          <input name="password" onChange={this.handlePasswordChange} />
          <button onClick={this.loginClick}>Login</button>
        </div>
      );
    } else {
      return(
        <div>
          <h1> Welcome {this.state.currentUser}</h1>
          <button onClick={this.logoutClick}>Logout</button>
        </div>
      );
    }
  }

  render() {
    return (
      <div className="App">
        {this.renderLoginScreen()}
      </div>
    );
  }
}

export default App;
