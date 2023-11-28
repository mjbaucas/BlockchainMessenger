import hashlib
import json

class Block:
    def __init__(self, index, timestamp, transactions, previous_hash, smart_contracts, public_key, nonce=0):
        self.public_key = public_key
        self.index = index
        self.timestamp = timestamp
        self.transactions = transactions
        self.previous_hash = previous_hash
        self.smart_contracts = smart_contracts
        self.nonce = nonce
        self.hash = self.gen_hashed_block()
    
    def gen_hashed_block(self):
        sha = hashlib.sha256()
        sha.update((str(self.index) + str(self.timestamp) + str(self.transactions) + str(self.previous_hash)).encode('utf-8'))
        return sha.hexdigest()
    
    def compute_hash(self):
        block_str = json.dumps(self.__dict__, indent=4, sort_keys=True, default=str)
        return hashlib.sha256(block_str.encode()).hexdigest()

    def validate_private_key(self, private_key):
        pub_key = self.public_key
        sha = hashlib.sha256()
        sha.update(str(private_key).encode('utf-8'))
        hash_key = sha.hexdigest()
        return hash_key == pub_key

    def disp_block_info(self):
        print('Index: {}'.format(self.index))
        print('Timestamp: {}'.format(self.timestamp))
        print('Public Key: {}'.format(self.public_key))
        
        print('Transactions:')
        for transaction in self.transactions:
            transaction.display_details()
    
    def search_contracts(self, source, destination):
        for contract in self.smart_contracts:
            if contract['source'] == source and contract['destination'] == destination and contract['allow'] == True:
                return True
        return False
    
    def search_user(self, username, password):
        for transaction in self.transactions:
            if transaction['type'] == "account" and transaction['username'] == username and transaction['password'] == password:
                return True
        return False

    def search_friends(self, username):
        temp_list = []
        for transaction in self.transactions:
            if transaction['type'] == "friends" and (transaction["account_one"] == username or transaction["account_two"] == username):
                temp_list.append(transaction)
        return temp_list

    def search_messages(self, user_one, user_two):
        temp_list = []
        for transaction in self.transactions:
            if transaction['type'] == "message" and (transaction["from"] == user_one or transaction["to"] == user_one) and (transaction["from"] == user_two or transaction["to"] == user_two):
                temp_list.append(transaction)
        return temp_list

    def get_block_transactions(self):
        return self.transactions