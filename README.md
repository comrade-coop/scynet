# Hive

This is the source code for the hive project.

## Installation

1. Install [sbt][sbt], [Python 3.6.x][python36], [pipenv][pipenv], and [node][node]:
    * Ubuntu (16.04):
        ```bash
        echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
        sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
        sudo add-apt-repository ppa:deadsnakes/ppa # Unneeded on 16.10 and above
        sudo apt-get update
        sudo apt-get install sbt python3.6 nodejs-legacy
        pip3 install pipenv
        ```
    * Arch Linux:
        ```bash
        pacman -S python sbt python-pipenv nodejs npm
        # If latest python is not 3.6, you might have to install the python36 package
        ```
    * Windows (`choco`), untested:
        ```bash
        choco install sbt
        choco install python --version 3.6.5
        choco install nodejs.install
        pip install pipenv
        ```

2. Clone this repository using git:
    ```bash
    git clone git@bitbucket.org:obecto/hive.git
    cd hive/
    ```

3. Install needed dependencies:
    ```bash
    cd runner/
    pipenv install
    cd ../
    ```
    ```bash
    cd generator/
    sbt compile
    cd ../
    ```
    ```bash
    cd data_downloader/
    npm install
    cd ../
    ```

4. Download price data (~3 MiB):
    ```bash
    cd data_downloader/
    npm run download
    cd ../
    ```

5. Run the project:
    * Generator:
        ```bash
        cd generator/
        sbt "run -- pipenv run python -m runner"
        # (Linux) At this point, you can press Ctrl-Z to pause the process
        # Then run `bg` to make it run in the background
        # And finally `disown` to get it separated from the shell
        # To stop it, use `kill <pid>`, or `killall java`
        ```
    * Runner:
        ```bash
        pipenv run python -m runner
        ```
    * Parser (when testing):
        ```bash
        pipenv run python -m runner.parser.keras_parser runner/parser/examples/merge.json
        ```

6. In case you need TensorFlow with GPU support, follow the instructions on their site.
    * Refer to [tf_gpu_install_steps.txt](./tf_gpu_install_steps.txt) for information regarding CUDA installation.
    * Make sure to specify `/usr/bin/python3.6` as the python version.
    * When installing the wheel package, use pipenv (inside the cloned repo):
        ```bash
        cd hive/runner/
        pipenv run pip install /tmp/tensorflow_pkg/tensorflow.*.whl
        ```

[sbt]: https://www.scala-sbt.org/download.html
[python36]: https://www.python.org/downloads/release/python-365/
[pipenv]: https://docs.pipenv.org/#install-pipenv-today
[node]: https://nodejs.org/en/download/
