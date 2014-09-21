__author__ = 'mendrugory'


from app import app
from flask import jsonify
from device.arduino import Arduino, ArduinoRequest


@app.route('/device/<device>/status')
def get_status(device):
    arduino = get_ip_from_device(device)
    arduino_request = ArduinoRequest()
    arduino_request.action = ArduinoRequest.STATUS
    response = arduino.request(arduino_request)
    return jsonify(response.__dict__)


@app.route('/device/<device>/on')
def post_on(device):
    arduino = get_ip_from_device(device)
    arduino_request = ArduinoRequest()
    arduino_request.action = ArduinoRequest.ON
    response = arduino.request(arduino_request)
    return jsonify(response.__dict__)


@app.route('/device/<device>/off')
def post_off(device):
    arduino = get_ip_from_device(device)
    arduino_request = ArduinoRequest()
    arduino_request.action = ArduinoRequest.OFF
    response = arduino.request(arduino_request)
    return jsonify(response.__dict__)

@app.route('/device/all')
def get_devices():
    devices = {}
    devices["home"] = "arduinoMega"
    return jsonify(devices)


def get_ip_from_device(device):
    return Arduino("192.168.1.200", 80)


