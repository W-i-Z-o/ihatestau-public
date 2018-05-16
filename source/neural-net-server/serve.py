#!/usr/bin/env python3

import os
from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np

app = Flask(__name__)

import logging
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

tf_sess = tf.Session(graph=tf.Graph())
tf_model = tf.saved_model.loader.load(tf_sess,
                                      ['serve'],
                                      os.environ.get('SAVED_MODEL_DIR', os.path.normpath(__file__ + '/../model')))

sdef = tf_model.signature_def['serving_default']
# TODO: assert inputs are like we expect

@app.route('/classify', methods=['POST'])
def do_classify():
    inputs = dict()
    for k in list(sdef.inputs):
        inputs[sdef.inputs[k].name] = [ f.read() for f in request.files.getlist(k) ]

    outputs = { k: sdef.outputs[k].name  for k in list(sdef.outputs) }

    result = tf_sess.run(outputs, inputs)

    return jsonify({ k:v.tolist() for k,v in result.items()})

if __name__ == '__main__':
    app.run(port=8742)
