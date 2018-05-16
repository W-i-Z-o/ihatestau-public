#!/usr/bin/env python3

import numpy as np
import tensorflow as tf
import argparse
import os, os.path
import glob
import sys

IMAGE_W = 320
IMAGE_H = 240
N_CLASSES = 2
LABELS = {0: 'jam', 1:'fluid'}
BATCH_SIZE = 10
TRAINING_STEPS = 2000

tf.logging.set_verbosity(tf.logging.ERROR)

def cnn_model_fn(features, labels, mode):
    """Model function for CNN."""

    # JPEG layer
    def jpeg_decode(filename):
        jpeg = tf.image.decode_jpeg(tf.read_file(filename), channels=3)
        return tf.image.convert_image_dtype(jpeg, dtype=tf.float32)

    jpeg_input_a = tf.map_fn(jpeg_decode, features['image-a'], dtype=tf.float32, back_prop=False, parallel_iterations=10)
    jpeg_input_b = tf.map_fn(jpeg_decode, features['image-b'], dtype=tf.float32, back_prop=False, parallel_iterations=10)

    # downscale
    downscaled_a = tf.image.resize_images(jpeg_input_a, (IMAGE_W, IMAGE_H))
    downscaled_b = tf.image.resize_images(jpeg_input_b, (IMAGE_W, IMAGE_H))

    # Input Layer
    input_layer_a = tf.reshape(downscaled_a, [-1, IMAGE_W, IMAGE_H, 3])
    input_layer_b = tf.reshape(downscaled_b, [-1, IMAGE_W, IMAGE_H, 3])

    # Convolutional Layer #1
    conv1_a = tf.layers.conv2d(
        inputs=input_layer_a,
        filters=32,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)
    conv1_b = tf.layers.conv2d(
        inputs=input_layer_b,
        filters=32,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)

    # Pooling Layer #1
    pool1_a = tf.layers.max_pooling2d(inputs=conv1_a, pool_size=[4, 4], strides=4)
    pool1_b = tf.layers.max_pooling2d(inputs=conv1_b, pool_size=[4, 4], strides=4)

    # Convolutional Layer #2 and Pooling Layer #2
    conv2_a = tf.layers.conv2d(
        inputs=pool1_a,
        filters=64,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)
    conv2_b = tf.layers.conv2d(
        inputs=pool1_b,
        filters=64,
        kernel_size=[5, 5],
        padding="same",
        activation=tf.nn.relu)
    pool2_a = tf.layers.max_pooling2d(inputs=conv2_a, pool_size=[4, 4], strides=4)
    pool2_b = tf.layers.max_pooling2d(inputs=conv2_b, pool_size=[4, 4], strides=4)

    # Dense Layer
    pool2_flat_a = tf.reshape(pool2_a, [-1, IMAGE_W//16 * IMAGE_H//16 * 64])
    pool2_flat_b = tf.reshape(pool2_b, [-1, IMAGE_W//16 * IMAGE_H//16 * 64])
    dense = tf.layers.dense(inputs=(pool2_flat_a + pool2_flat_b), units=1024, activation=tf.nn.relu)
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
        return tf.estimator.EstimatorSpec(mode=mode, predictions=predictions)

    # Calculate Loss (for both TRAIN and EVAL modes)
    onehot_labels = tf.one_hot(indices=tf.cast(labels, tf.int32), depth=N_CLASSES)
    loss = tf.losses.softmax_cross_entropy(
        onehot_labels=onehot_labels, logits=logits)

    # Configure the Training Op (for TRAIN mode)
    if mode == tf.estimator.ModeKeys.TRAIN:
        optimizer = tf.train.GradientDescentOptimizer(learning_rate=0.001)
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

def train(args):
    print('INFO: Scanning training data...')

    # Load training and eval data
    train_filelist_a = []
    train_filelist_b = []
    train_labellist = []
    for i in [0,1]:
        for f in glob.glob(os.path.join(args.datadir, 'train', args.spot, LABELS[i], '*-MAP.png')):
            train_filelist_a.append(f.replace('-MAP.png', '-A.png'))
            train_filelist_b.append(f.replace('-MAP.png', '-B.png'))
            train_labellist.append(i)

    train_data_a = np.asarray(train_filelist_a, dtype=np.str)
    train_data_b = np.asarray(train_filelist_b, dtype=np.str)
    train_labels = np.asarray(train_labellist, dtype=np.int32)

    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=args.output)

    # Set up logging for predictions
    tensors_to_log = {"probabilities": "softmax_tensor"}
    logging_hook = tf.train.LoggingTensorHook(
      tensors=tensors_to_log, every_n_iter=50)

    print('INFO: Training the model...')
    # Train the model
    train_input_fn = tf.estimator.inputs.numpy_input_fn(
        x={'image-a': train_data_a, 'image-b': train_data_b},
        y=train_labels,
        batch_size=BATCH_SIZE,
        num_epochs=None,
        shuffle=True)
    classifier.train(
        input_fn=train_input_fn,
        steps=TRAINING_STEPS,
        hooks=[logging_hook])

def evaluate(args):
    print('INFO: Scanning evaluation data...')

    # Load training and eval data
    eval_filelist_a = []
    eval_filelist_b = []
    eval_labellist = []
    for i in [0,1]:
        for f in glob.glob(os.path.join(args.datadir, 'eval', args.spot, LABELS[i], '*-MAP.png')):
            eval_filelist_a.append(f.replace('-MAP.png', '-A.png'))
            eval_filelist_b.append(f.replace('-MAP.png', '-B.png'))
            eval_labellist.append(i)

    eval_data_a = np.asarray(eval_filelist_a, dtype=np.str)
    eval_data_b = np.asarray(eval_filelist_b, dtype=np.str)
    eval_labels = np.asarray(eval_labellist, dtype=np.int32)

    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=args.output)

    print('INFO: Evaluating the model...')
    # Evaluate the model and print results
    eval_input_fn = tf.estimator.inputs.numpy_input_fn(
        x={'image-a': eval_data_a, 'image-b': eval_data_b},
        y=eval_labels,
        num_epochs=1,
        batch_size=BATCH_SIZE,
        shuffle=False)
    eval_results = classifier.evaluate(input_fn=eval_input_fn)

    # HACK: calc f1 measure
    eval_results['f1 measure'] = 2*(eval_results['precision']*eval_results['recall'])/(eval_results['precision'] + eval_results['recall'])

    print(eval_results)


def run(model_dir, imagefile_prefixes):
    # Create the Estimator
    classifier = tf.estimator.Estimator(model_fn=cnn_model_fn, model_dir=model_dir)

    ifn = tf.estimator.inputs.numpy_input_fn(
        x = {
            'image-a': np.asarray([i + '-A.png' for i in imagefile_prefixes], dtype=np.str),
            'image-b': np.asarray([i + '-B.png' for i in imagefile_prefixes], dtype=np.str)
        },
        batch_size = BATCH_SIZE,
        shuffle = False
    )

    return classifier.predict(ifn)

def run_ui(args):
    results = run(args.model, [args.imagefile_prefix])
    result = next(results)
    print('---')
    for i in [0,1]:
        print('{0: <10}: {1:.6f}'.format(LABELS[i], result['probabilities'][i]))

if __name__ == "__main__":
    tf.logging.set_verbosity(tf.logging.INFO)

    ap = argparse.ArgumentParser(description="Frist Model")
    sap = ap.add_subparsers()
    sap.required=True
    sap.dest='command'

    ap_train = sap.add_parser('train')
    ap_train.add_argument('--datadir', required=True, help='Directory with data')
    ap_train.add_argument('--spot', required=True, help='Camera spot (or * for all spots)')
    ap_train.add_argument('--output', '-o', required=True, help='Output directory for the model')
    ap_train.set_defaults(func=train)

    ap_train = sap.add_parser('eval')
    ap_train.add_argument('--datadir', required=True, help='Directory with data')
    ap_train.add_argument('--spot', required=True, help='Camera spot (or * for all spots)')
    ap_train.add_argument('--output', '-o', required=True, help='Output directory for the model')
    ap_train.set_defaults(func=evaluate)

    ap_eval = sap.add_parser('run')
    ap_eval.add_argument('--model', '-m', required=True, help='Directory containing the trained model')
    ap_eval.add_argument('imagefile_prefix', help='Prefix of file to run model against')
    ap_eval.set_defaults(func=run_ui)

    args = ap.parse_args()
    args.func(args)
