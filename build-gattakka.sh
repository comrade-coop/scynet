#!/bin/bash
set -ex
git clone https://github.com/obecto/gattakka.git
cd gattakka
gradle build
gradle publishToMavenLocal
