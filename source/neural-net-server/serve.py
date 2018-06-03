#!/usr/bin/env python3

import os
import tempfile
import zipfile
import io
from glob import glob
from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np

app = Flask(__name__)

import logging
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

tf_sess = tf.Session(graph=tf.Graph())

model_dir = os.environ.get('SAVED_MODEL_DIR', os.path.normpath(__file__ + '/../model'))
model_zip = os.environ.get('SAVED_MODEL_ZIP', os.path.normpath(__file__ + '/../model.zip'))

if os.path.isdir(model_dir):
    tf_model = tf.saved_model.loader.load(tf_sess, ['serve'], model_dir)
elif os.path.isfile(model_zip):
    with tempfile.TemporaryDirectory() as tempdir:
        with zipfile.ZipFile(model_zip) as z:
            for name in z.namelist():
                z.extract(name, tempdir + '/')

        tf_model = tf.saved_model.loader.load(tf_sess, ['serve'], tempdir + '/model')
elif os.path.isfile(model_zip + '.part00'):
    with io.BytesIO() as b:
        for name in sorted(glob(model_zip + '.part*')):
            with open(name, 'rb') as f:
                b.write(f.read())

        b.seek(0)

        with tempfile.TemporaryDirectory() as tempdir:
            with zipfile.ZipFile(b) as z:
                for name in z.namelist():
                    z.extract(name, tempdir + '/')

            tf_model = tf.saved_model.loader.load(tf_sess, ['serve'], tempdir + '/model')
else:
    assert False

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
