#!/usr/bin/env python3

import numpy as np
import tensorflow as tf
import argparse
import os, os.path
import glob
import sys
import hashlib

from tensorflow.python.platform import gfile


tf.logging.set_verbosity(tf.logging.INFO)

IMAGE_W = 299
IMAGE_H = 299
N_CLASSES = 2
LABELS = {0: 'jam', 1:'fluid'}
REVERSE_LABELS = {v:k for k,v in LABELS.items()}
BATCH_SIZE = 100
TRAINING_STEPS = 100000

# download from http://download.tensorflow.org/models/image/imagenet/inception-2015-12-05.tgz
INCEPTION_GRAPH_FILE = '/tmp/classify_image_graph_def.pb'

def cnn_model_fn(features, labels, mode):
    """Model function for CNN."""

    print('TRACE: running model fn')

    if 'image-a' in features:
        # features contain jpeg data - load and insert the whole inception pipeline here
        bottleneck_a = tf.map_fn(build_bottleneck_tensor_from_jpeg, features['image-a'], dtype=tf.float32, back_prop=False, parallel_iterations=10)
        bottleneck_b = tf.map_fn(build_bottleneck_tensor_from_jpeg, features['image-b'], dtype=tf.float32, back_prop=False, parallel_iterations=10)
    else:
        bottleneck_a = features['bottleneck-a']
        bottleneck_b = features['bottleneck-b']

    dense = tf.layers.dense(inputs=tf.concat([bottleneck_a, bottleneck_b], 1), units=1024, activation=tf.nn.relu)
    dropout = tf.layers.dropout(
        inputs=dense, rate=0.4, training=mode == tf.estimator.ModeKeys.TRAIN)

    # Logits Layer
    logits = tf.layers.dense(inputs=dropout, units=N_CLASSES)

    predictions = {
        # Generate predictions (for PREDICT and EVAL mode)
        "classes": tf.argmax(input=logits, axis=1),
        # Add `softmax_tensor` to the graph. It is used for PREDICT and by the
        # `logging_hook`.
        "probabilities": tf.nn.softmax(logits, name="softmax_tensor")
    }

    if mode == tf.estimator.ModeKeys.PREDICT:
        return tf.estimator.EstimatorSpec(mode=mode, predictions=predictions,
                                          export_outputs={'output': tf.estimator.export.PredictOutput(predictions)})

    # Calculate Loss (for both TRAIN and EVAL modes)
    onehot_labels = tf.one_hot(indices=tf.cast(labels, tf.int32), depth=N_CLASSES)
    loss = tf.losses.softmax_cross_entropy(
        onehot_labels=onehot_labels, logits=logits)

    # Configure the Training Op (for TRAIN mode)
    if mode == tf.estimator.ModeKeys.TRAIN:
        learning_rate = tf.train.exponential_decay(0.045, tf.train.get_global_step(), 1000, 0.96)
        optimizer = tf.train.GradientDescentOptimizer(learning_rate=learning_rate)
        train_op = optimizer.minimize(
            loss=loss,
            global_step=tf.train.get_global_step())
        return tf.estimator.EstimatorSpec(mode=mode, loss=loss, train_op=train_op)

    # Add evaluation metrics (for EVAL mode)
    eval_metric_ops = {
        "accuracy": tf.metrics.accuracy(
            labels=labels, predictions=predictions["classes"]),
        "precision": tf.metrics.precision(
            labels=labels, predictions=predictions["classes"]),
        "recall": tf.metrics.recall(
            labels=labels, predictions=predictions["classes"])}
    return tf.estimator.EstimatorSpec(
        mode=mode, loss=loss, eval_metric_ops=eval_metric_ops)

def build_bottleneck_tensor_from_jpeg(jpeg_data_tensor):
    print('TRACE: build_bottleneck_tensor_from_jpeg')

    MODEL_FILE = INCEPTION_GRAPH_FILE
    BOTTLENECK_TENSOR_NAME = 'pool_3/_reshape:0'
    RESIZED_INPUT_TENSOR_NAME = 'Mul:0'

    # first decode jpeg data and resize it for the target
    decoded_image_tensor = tf.image.convert_image_dtype(tf.image.decode_jpeg(jpeg_data_tensor, channels=3), dtype=tf.float32)

    resize_size = tf.constant([IMAGE_W, IMAGE_H])
    resized_image = tf.image.resize_bilinear(tf.expand_dims(decoded_image_tensor, 0), resize_size)

    with gfile.FastGFile(MODEL_FILE, 'rb') as f:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())

    bottleneck_tensor, = (tf.import_graph_def(
        graph_def,
        name='',
        input_map={RESIZED_INPUT_TENSOR_NAME: resized_image},
        return_elements=[
            BOTTLENECK_TENSOR_NAME,
        ]))

    return tf.reshape(bottleneck_tensor, [2048])

def build_bottleneck_tensor(filename_tensor):
    return build_bottleneck_tensor_from_jpeg(tf.read_file(filename_tensor))

def run_bottleneck(filename, sess, filename_tensor, bottleneck_tensor):
    # first decode jpeg data and resize it for the target
    bottleneck_values = sess.run(bottleneck_tensor,
                                {filename_tensor: filename})
    bottleneck_values = np.squeeze(bottleneck_values)
    return bottleneck_values


def bottleneck_filename(fname, bottleneck_dir):
    m = hashlib.sha256()
    m.update(fname.encode('utf-8'))
    d = m.hexdigest()
    return os.path.join(bottleneck_dir, d + '.npy')

def create_all_bottlenecks(training_images, bottleneck_dir):
    retval = [] # (bottleneck_a, bottleneck_b, label)

    with tf.Graph().as_default() as graph:
        filename_tensor = tf.placeholder(dtype=tf.string)
        bottleneck_tensor = build_bottleneck_tensor(filename_tensor)

        with tf.Session(graph=graph) as sess:
            i = 0
            for filename_a, filename_b, filename_map, label in training_images:
                i = i + 1

                bottleneck_file_a = bottleneck_filename(filename_a, bottleneck_dir)
                bottleneck_file_b = bottleneck_filename(filename_b, bottleneck_dir)

                try:
                    bottleneck_a = np.load(bottleneck_file_a)
                    bottleneck_b = np.load(bottleneck_file_b)
                except IOError:
                    bottleneck_a = run_bottleneck(filename_a, sess, filename_tensor, bottleneck_tensor)
                    bottleneck_b = run_bottleneck(filename_b, sess, filename_tensor, bottleneck_tensor)

                    np.save(bottleneck_file_a, bottleneck_a)
                    np.save(bottleneck_file_b, bottleneck_b)

                    print('created bottleneck {} of {}'.format(i, len(training_images)))


                retval.append((bottleneck_a, bottleneck_b, label))

    return retval

def filename_to_data_input_fn(filename_fn):
    def ifn(*args, **kwargs):
        features, labels = filename_fn(*args, **kwargs)

        def read_file(filename):
            return tf.read_file(filename)

        def create_data_multitensor(filenames):
            return tf.map_fn(read_file, filenames, dtype=tf.string, back_prop=False, parallel_iterations=10)

        return ({k: create_data_multitensor(v) for k, v in features.items()}, labels)

    return ifn

# training_images = [(img_a, img_b, img_map, class), ...]
# --> None
def train(training_images, model_dir, steps):
    if steps <= 0:
        steps = TRAINING_STEPS

    # make bottleneck
    bottleneck_dir = os.path.join(model_dir, 'bottlenecks')
    train_dir = os.path.join(model_dir, 'training')

    os.makedirs(bottleneck_dir, exist_ok=True)
    bottlenecks = create_all_bottlenecks(training_images, bottleneck_dir)

    train_data_a = np.asarray([a for a,b,l in bottlenecks])
    train_data_b = np.asarray([b for a,b,l in bottlenecks])
    train_labels = np.asarray([REVERSE_LABELS[l] for a,b,l in bottlenecks], dtype=np.int32)

    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=train_dir)

    # Set up logging for predictions
    tensors_to_log = {"probabilities": "softmax_tensor"}
    logging_hook = tf.train.LoggingTensorHook(
      tensors=tensors_to_log, every_n_iter=50)

    # Train the model
    train_input_fn = tf.estimator.inputs.numpy_input_fn(
        x={'bottleneck-a': train_data_a, 'bottleneck-b': train_data_b},
        y=train_labels,
        batch_size=BATCH_SIZE,
        num_epochs=None,
        shuffle=True)
    classifier.train(
        input_fn=train_input_fn,
        steps=steps,
        hooks=[logging_hook])

# images = [(img_a, img_b, img_map), ...]
# --> [class, ...]
def run(images, model_dir):
    train_dir = os.path.join(model_dir, 'training')
    bottleneck_dir = os.path.join(model_dir, 'bottlenecks')

    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=train_dir)

    filelist_a = np.asarray([a for a,b,m in images], dtype=np.str)
    filelist_b = np.asarray([b for a,b,m in images], dtype=np.str)

    bottlenecks = create_all_bottlenecks([(a,b,m,None) for a,b,m in images], bottleneck_dir)
    data_a = np.asarray([a for a,b,l in bottlenecks])
    data_b = np.asarray([b for a,b,l in bottlenecks])

    print(f'total images: {len(images)}')

    ifn = tf.estimator.inputs.numpy_input_fn(
        x={'bottleneck-a': data_a, 'bottleneck-b': data_b},
        y=np.asarray([0] * len(filelist_a), dtype=np.int32),
        batch_size=BATCH_SIZE,
        num_epochs=1,
        shuffle=False)

    retval = []
    for result in classifier.predict(input_fn=ifn):
        retval.append(result['probabilities'][0])

    return retval

def export(directory, output):
    train_dir = os.path.join(directory, 'training')

    features = { 'image-a': tf.placeholder(dtype=tf.string, shape=[1], name='image-a-data'),
                 'image-b': tf.placeholder(dtype=tf.string, shape=[1], name='image-b-data') }

    es = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=train_dir)

    es.export_savedmodel(output, tf.estimator.export.build_raw_serving_input_receiver_fn(features))
