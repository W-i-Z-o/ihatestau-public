#!/usr/bin/env python3

import numpy as np
import tensorflow as tf
import argparse
import os, os.path
import glob
import sys

tf.logging.set_verbosity(tf.logging.INFO)

IMAGE_W = 320
IMAGE_H = 240
N_CLASSES = 2
LABELS = {0: 'jam', 1:'fluid'}
REVERSE_LABELS = {v:k for k,v in LABELS.items()}
BATCH_SIZE = 10
TRAINING_STEPS = 50000
def cnn_model_fn(features, labels, mode):
    """Model function for CNN."""

    # JPEG layer
    def jpeg_decode(data):
        jpeg = tf.image.decode_jpeg(data, channels=3)
        return tf.image.convert_image_dtype(jpeg, dtype=tf.float32)

    jpeg_input = tf.map_fn(jpeg_decode, features['image-b'], dtype=tf.float32, back_prop=False, parallel_iterations=10)

    resized_input = tf.image.resize_images(jpeg_input, (IMAGE_H, IMAGE_W))

    input_layer = tf.reshape(resized_input, [-1, IMAGE_H, IMAGE_W, 3])

    # Convolutional Layer #1
    conv1 = tf.layers.conv2d(
        inputs=input_layer,
        filters=32,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)

    # Pooling Layer #1
    pool1 = tf.layers.max_pooling2d(inputs=conv1, pool_size=[4, 4], strides=4)

    # Convolutional Layer #2 and Pooling Layer #2
    conv2 = tf.layers.conv2d(
        inputs=pool1,
        filters=64,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)
    pool2 = tf.layers.max_pooling2d(inputs=conv2, pool_size=[4, 4], strides=4)

    # Dense Layer
    pool2_flat = tf.reshape(pool2, [-1, IMAGE_H//16 * IMAGE_W//16 * 64])
    dense = tf.layers.dense(inputs=pool2_flat, units=1024, activation=tf.nn.relu)
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

    train_filelist  = [b for a,b,m,l in training_images]
    train_labellist = [REVERSE_LABELS[l] for a,b,m,l in training_images]

    train_data   = np.asarray(train_filelist, dtype=np.str)
    train_labels = np.asarray(train_labellist, dtype=np.int32)

    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=model_dir)

    # Set up logging for predictions
    tensors_to_log = {"probabilities": "softmax_tensor"}
    logging_hook = tf.train.LoggingTensorHook(
      tensors=tensors_to_log, every_n_iter=50)

    # Train the model
    train_input_fn = tf.estimator.inputs.numpy_input_fn(
        x={'image-b': train_data},
        y=train_labels,
        batch_size=BATCH_SIZE,
        num_epochs=None,
        shuffle=True)
    classifier.train(
        input_fn=filename_to_data_input_fn(train_input_fn),
        steps=steps,
        hooks=[logging_hook])


# images = [(img_a, img_b, img_map), ...]
# --> [class, ...]
def run(images, model_dir):
    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=model_dir)

    filelist = np.asarray([b for a,b,m in images], dtype=np.str)

    ifn = filename_to_data_input_fn(tf.estimator.inputs.numpy_input_fn(
        x={'image-b': filelist},
        y=np.asarray([0] * len(filelist), dtype=np.int32),
        batch_size=BATCH_SIZE,
        num_epochs=1,
        shuffle=False))

    retval = []
    for result in classifier.predict(input_fn=ifn):
        retval.append(result['probabilities'][0])

    return retval

def export(directory, output):
    features = { 'image-b': tf.placeholder(dtype=tf.string, shape=[1], name='image-b-data') }

    es = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=directory)

    es.export_savedmodel(output, tf.estimator.export.build_raw_serving_input_receiver_fn(features))



