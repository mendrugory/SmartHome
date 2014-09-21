__author__ = 'mendrugory'

from socket import socket
import time


class Arduino(socket):

    HEADER = "h"
    FOOTER = "f"
    STATUS = "s"
    ON = "n"
    OFF = "o"
    SUCCESS = "S"
    ERROR = "F"
    WAIT_SECONDS = 1
    BUFFER_RECEIVER = 1024
    WRONG_HEADER_MESSAGE = "Wrong received header"
    WRONG_FOOTER_MESSAGE = "Wrong received footer"
    WRONG_MESSAGE = "Wrong receive result"

    def __init__(self, ip, port):
        super(Arduino, self).__init__()
        self.ip = ip
        self.port = port

    def request(self, arduino_request):
        try:
            self.connect((self.ip, self.port))
            the_request = Arduino.build_request(arduino_request)
            self.send(the_request)
            time.sleep(Arduino.WAIT_SECONDS)
            response = self.recv(Arduino.BUFFER_RECEIVER)
            result = Arduino.treat_response(response)
        except Exception, e:
            raise Exception(e)
        finally:
            self.close()
        return result

    @staticmethod
    def treat_response(response):
        header = response[0]
        result = response[1]
        footer = response[2]
        arduino_response = ArduinoResponse()
        if header != Arduino.HEADER:
            arduino_response.message = Arduino.WRONG_HEADER_MESSAGE
            arduino_response.code = ArduinoResponse.ERROR
        elif footer != Arduino.FOOTER:
            arduino_response.message = Arduino.WRONG_FOOTER_MESSAGE
            arduino_response.code = ArduinoResponse.ERROR
        else:
            if result in [Arduino.SUCCESS, Arduino.ERROR, Arduino.ON, Arduino.OFF]:
                if result == Arduino.SUCCESS:
                    arduino_response.code = ArduinoResponse.SUCCESS
                elif result == Arduino.ERROR:
                    arduino_response.code = ArduinoResponse.FAILURE
                elif result == Arduino.ON:
                    arduino_response.code = ArduinoResponse.STATUS
                    arduino_response.status = True
                elif result == Arduino.OFF:
                    arduino_response.code = ArduinoResponse.STATUS
                    arduino_response.status = False
            else:
                arduino_response.message = Arduino.WRONG_MESSAGE
                arduino_response.code = ArduinoResponse.ERROR
        return arduino_response

    @staticmethod
    def build_request(arduino_request):
        action = arduino_request.action
        request = []
        request.append(Arduino.HEADER)
        if action == ArduinoRequest.ON:
            request.append(Arduino.ON)
        elif action == ArduinoRequest.OFF:
            request.append(Arduino.OFF)
        else:
            request.append(Arduino.STATUS)
        request.append(Arduino.FOOTER)
        return "".join(request)


class ArduinoRequest():

    ON = 0
    OFF = 1
    STATUS = 2

    def __init__(self):
        self.action = ArduinoRequest.STATUS


class ArduinoResponse():

    ERROR = 0
    FAILURE = 1
    SUCCESS = 2
    STATUS = 4

    def __init__(self):
        self.status = None
        self.message = None
        self.code = None



