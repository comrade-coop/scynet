import os
import threading
from . import parse_repositories

repositories = parse_repositories(os.path.join(os.path.dirname(__file__), '../../repositories3.json'))

for source_name, source in repositories.items():
    for reader_name, reader in source.items():
        print('Starting reader %s' % reader_name)
        reader.create_cache()
        print('Reader %s finished' % reader_name)
