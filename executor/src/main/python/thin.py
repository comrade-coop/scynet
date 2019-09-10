
from pyignite import Client

client = Client()
client.connect('127.0.0.1', 10800)

my_cache = client.get_or_create_cache('my_cache')

my_cache.put('my key', 42)

result = my_cache.get('my key')
print(result)  # 42

result = my_cache.get('non-existent key')
print(result)  # None

result = my_cache.get_all([
    'my key',
    'non-existent key',
    'other-key',
])
print(result)  # {'my key': 42}


client.close()