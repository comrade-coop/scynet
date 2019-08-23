#!/bin/sh
set -ex
git clone https://github.com/obecto/gattakka.git
cd gattakka
gradle build
