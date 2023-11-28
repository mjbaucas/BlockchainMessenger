import datetime
from .block import Block

class Chain:
    def __init__(self):
        self.chain = [gen_genesis_block()]

    def gen_next_block(self, public_key, transactions):
        prev_block = self.chain[-1]
        index = prev_block.index + 1
        timestamp = datetime.datetime.now()
        data = transactions
        hashed_block = prev_block.gen_hashed_block()
        self.chain.append(Block(index, timestamp, data, hashed_block, [], public_key))

    def display_contents(self):
        for block in self.chain:
            block.disp_block_info()

    def output_ledger(self):
        main_transactions = []
        for block in self.chain:
            if block.index != 0:
                temp_transactions = block.get_block_transactions()
                for temp_transaction in temp_transactions:
                    main_transactions.append(temp_transaction)
        return main_transactions

    def search_ledger(self, key):
        for i in self.chain[::-1]:
            if(i.validate_private_key(key)):
                return i
        return None

    def search_smart_contracts(self, source, destination):
        for i in self.chain[::-1]:
            if i.search_contracts(source, destination):
                return True
        return False

    def search_user(self, username, password):
        for i in self.chain[::-1]:
            if i.search_user(username, password):
                return True
        return False

    def search_friends(self, username):
        temp_list = []
        for i in self.chain[::-1]:
            result = i.search_friends(username)
            if len(result) > 0:
                if len(temp_list) > 0:
                    temp_list = result
                else: 
                    temp_list.extend(result)
        return temp_list

    def search_messages(self, user_one, user_two):
        temp_list = []
        for i in self.chain[::-1]:
            result = i.search_messages(user_one, user_two)
            if len(result) > 0:
                if len(temp_list) == 0:
                    temp_list = result
                else: 
                    temp_list.extend(result)
        return temp_list

def gen_genesis_block():
    transaction = [{"type": "dummy"}]
    return Block(0, datetime.datetime.now(), transaction, "0", [], "0")