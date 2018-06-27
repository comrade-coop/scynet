import os
import threading
from . import parse_repositories

repositories = parse_repositories(os.path.join(os.path.dirname(__file__), '../../repositories.json'))

threads = []
for source_name, source in repositories.items():
    for reader_name, reader in source.items():
        thread = threading.Thread(None, lambda reader: reader.create_cache(), '%s/%s' % (source_name, reader_name), (reader,))
        print('Starting thread for %s' % thread.name)
        thread.start()
        threads.append(thread)

while len(threads) > 0:
    for thread in threads:
        thread.join(1.0)
        if not thread.is_alive():
            print('Thread for %s finished' % thread.name)
    threads = [thread for thread in threads if thread.is_alive()]
