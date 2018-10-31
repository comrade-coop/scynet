# --------------------
# - Using This File: -
# --------------------

FROM kennethreitz/pipenv

# COPY . /app
COPY predictor_train.py /app
COPY ./datasets/ /app/datasets

# -- Replace with the correct path to your app's main executable
CMD python3 predictor_train.py --path models datasets/balanceLastSeenDistribution_log1_2_dist-highPrice_stani-24-w
