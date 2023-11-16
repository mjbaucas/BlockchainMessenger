from flask import Flask, request, session

app = Flask(__name__)
app.secret_key = 'BAD_SECRET_KEY'

accounts = [
    {"type":"account", "id":0, "username":"test", "password": "test"},
    {"type":"account", "id":1, "username":"test_two", "password": "test"},
]

friends = [
    {"type":"friends", "account_one":"test", "account_two": "test_two"}, 
    {"type":"friends", "account_one":"test_three", "account_two": "test"}
]

messages =[
    {"type":"message", "from": "test", "to": "test_two", "message": "Hello", "timestamp": 0},
    {"type":"message", "from": "test_two", "to": "test", "message": "Hi!", "timestamp": 1},
]

@app.route('/check', methods=['GET'])
def check_user():
    if 'username' in session:
        if session['username']:
            return {'username': session['username'], 'response': 200}
        else:
            return {'status': 500}
    else:
        return {'status': 403}

@app.route('/login', methods=['GET', 'POST'])
def login_user():
    if request.method == 'POST':
        data = request.get_json()
        try:
            search_result = [item for item in accounts if item["username"] == data['username'] and item["password"] == data['password']]
            if not search_result == False:    
                session['username'] = data['username']
                return {'status': 200}
            else:
                return {'status': 401}
        except:
            return {'status': 401}
    else:
        return {'status': 500}

@app.route('/logout', methods=['POST'])
def logout_user():
    if request.method == 'POST':
        if 'username' in session:
            if session['username']:
                session.pop('username', default=None)
                return {'status': 200}
            else:
                return {'status': 500}
        else:
            return {'status': 403}
    else:
        return {'status': 500}

@app.route('/friendslist', methods=['GET'])
def get_friends_list():
    if 'username' in session:
        try:
            search_results = [item for item in friends if item["account_one"] == session["username"] or item["account_two"] == session["username"]]
            if not search_results == False:
                temp_friend_list = []
                for result in search_results:
                    if result["account_one"] == session['username']:
                        temp_friend_list.append(result["account_two"])
                    elif result["account_two"] == session['username']:
                        temp_friend_list.append(result["account_one"])
                return {'friends': temp_friend_list, 'status': 200}
            else: 
                return {'status': 401}
        except:
            return {'status': 401}
    else:
        return {'status': 403}

@app.route('/messages', methods=['POST'])
def get_messages():
    if request.method == 'POST':
        data = request.get_json()
        if 'username' in session:
            try:
                search_results = [item for item in messages if (item["from"] == session["username"] or item["to"] == session["username"]) and (item["from"] == data["friend"] or item["to"] == data["friend"])]
                if not search_results == False:
                    temp_message_list = []
                    for result in search_results:
                        temp_message_list.append({"from": result["from"], "to": result["to"], "message": result["message"], "timestamp": result["timestamp"]})
                    return {'messages': temp_message_list, 'status': 200}
                else: 
                    return {'status': 401}
            except:
                return {'status': 401}
        else:
            return {'status': 403}
    else:
        return {'status': 500}

@app.route('/message/send', methods=['POST'])
def send_message():
    if request.method == 'POST':
        data = request.get_json()
        if 'username' in session:
            if data['from'] == session['username']:
                try:
                    print(data)
                    messages.append({"type":"message", "from": data['from'], "to": data['to'], "message": data['message'], "timestamp": 2})
                    return {'status': 200}
                except:
                    return {'status': 401}
            else:
                return {'status': 403}                        
        else:
            return {'status': 403}
    else:
        return {'status': 500}