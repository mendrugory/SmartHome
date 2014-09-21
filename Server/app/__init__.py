__author__ = 'mendrugory'

from flask import Flask

app = Flask(__name__)

from app import views
