from flask import Flask, request, session

app = Flask(__name__)
app.secret_key = 'BAD_SECRET_KEY'

@app.route('/check', methods=['GET'])
def check_user():
    if 'username' in session:
        if session['username']:
            return {'username': session['username'], 'response': 200}
        else:
            return {'response': 500}
    else:
        return {'response': 400}

@app.route('/login', methods=['GET', 'POST'])
def login_user():
    if request.method == 'POST':
        data = request.get_json()
        session['username'] = data['username']
        return {'response': 200}

@app.route('/logout', methods=['POST'])
def logout_user():
    if request.method == 'POST':
        if 'username' in session:
            if session['username']:
                session.pop('username', default=None)
                return {'response': 200}
            else:
                return {'response': 500}
        else:
            return {'response': 400}

    