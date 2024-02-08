from flask import Flask, request, session

import operator
from datetime import timezone 
import datetime 

app = Flask(__name__)
app.secret_key = 'BAD_SECRET_KEY'

from blockchain.private import Chain as PrivateBlockChain
private_chain = PrivateBlockChain()

entries = [
    {"type":"account", "id":0, "username":"test", "password": "test"},
    {"type":"account", "id":1, "username":"test_two", "password": "test"},
    {"type":"account", "id":2, "username":"test_three", "password": "test"},
    {"type":"friends", "account_one":"test", "account_two": "test_two"}, 
    {"type":"friends", "account_one":"test_three", "account_two": "test"},
    {"type":"message", "from": "test", "to": "test_two", "message": "Hello", "timestamp": 0},
    {"type":"message", "from": "test_two", "to": "test", "message": "Hi!", "timestamp": 1},
]
private_chain.gen_next_block("temp", entries)

@app.route('/check', methods=['GET'])
def check_user():
    if 'username' in session:
        if session['username'] and private_chain.search_user(session['username'], session['password']):
            return {'username': session['username'], 'status': 200}
        else:
            return {'status': 500}
    else:
        return {'status': 403}

@app.route('/login', methods=['GET', 'POST'])
def login_user():
    if request.method == 'POST':
        data = request.get_json()
        try:
            if private_chain.search_user(data['username'], data['password']):    
                session['username'] = data['username']
                session['password'] = data['password']                
                return {'username': session['username'], 'status': 200}
            else:
                return {'status': 401}
        except:
            print("An error has occured in logging in")
            return {'status': 401}
    else:
        return {'status': 500}

@app.route('/logout', methods=['POST'])
def logout_user():
    if request.method == 'POST':
        if 'username' in session and private_chain.search_user(session['username'], session['password']):
            if session['username']:
                session.pop('username', default=None)
                session.pop('password', default=None)
                return {'status': 200}
            else:
                return {'status': 500}
        else:
            return {'status': 403}
    else:
        return {'status': 500}

@app.route('/friendslist', methods=['GET'])
def get_friends_list():
    if 'username' in session and private_chain.search_user(session['username'], session['password']):
        try:
            friends = private_chain.search_friends(session['username'])
            temp_friend_list = []
            if len(friends) > 0:         
                for friend in friends:
                    if friend["account_one"] == session['username']:
                        temp_friend_list.append(friend["account_two"])
                    elif friend["account_two"] == session['username']:
                        temp_friend_list.append(friend["account_one"])
            return {'friends': temp_friend_list, 'status': 200}
        except:
            print("An error has occured in finding friends")
            return {'status': 401}
    else:
        return {'status': 403}

@app.route('/messages', methods=['POST'])
def get_messages():
    if request.method == 'POST':
        data = request.get_json()
        if 'username' in session and private_chain.search_user(session['username'], session['password']):
            try:
                messages = private_chain.search_messages(session['username'], data["friend"])
                temp_message_list = []
                if len(messages) > 0:
                    for message in messages:
                        temp_message_list.append({"from": message["from"], "to": message["to"], "message": message["message"], "timestamp": message["timestamp"]})
                    temp_message_list.sort(key=operator.itemgetter('timestamp'))
                return {'messages': temp_message_list, 'status': 200}
            except:
                print("An error has occured in searching messages")
                return {'status': 401}
        else:
            return {'status': 403}
    else:
        return {'status': 500}

@app.route('/message/send', methods=['POST'])
def send_message():
    if request.method == 'POST':
        data = request.get_json()
        if 'username' in session and private_chain.search_user(session['username'], session['password']):
            if data['from'] == session['username']:
                try:
                    print(data)
                    # Temporary code for adding messages
                    dt = datetime.datetime.now(timezone.utc) 
                    message = {"type":"message", "from": data['from'], "to": data['to'], "message": data['message'], "timestamp": dt.replace(tzinfo=timezone.utc).timestamp()}
                    private_chain.gen_next_block("temp", [message])
                    return {'status': 200}
                except:
                    print("Error in saving message")
                    return {'status': 401}
            else:
                return {'status': 403}                        
        else:
            return {'status': 403}
    else:
        return {'status': 500}