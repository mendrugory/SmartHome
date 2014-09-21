#!virtualenv/bin/python
from app import app
app.run(host="192.168.1.12", port=5002, debug = True)
