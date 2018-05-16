#!/usr/bin/env python3

import argparse
import importlib
import glob
import os
import pickle
import matplotlib.pyplot as plt
import numpy as np
import sys

def train(args):
    model = importlib.import_module("models." + args.model)

    print('INFO: Scanning training data...')

    # Load training and eval data
    train_list = []
    for i in ['jam', 'fluid']:
        for f in glob.glob(os.path.join(args.datadir, 'train', args.spot, i, '*-MAP.png')):
            train_list.append((f.replace('-MAP.png', '-A.png'), f.replace('-MAP.png', '-B.png'), f, i))

    # train the model
    print('INFO: Training the model...')
    model.train(train_list, args.directory, args.steps)

def smooth_changes_simple(inp, num, default):
    def smoother(i):
        r = default
        c = [default]*num
        for e in i:
            c.append(e)
            while len(c) > num:
                c.pop(0)

            if c.count(e) == len(c):
                r = e

            yield r

    return list(smoother(inp))

def smooth_exponential(inp, alpha):
    def smoother(i):
        x = 0
        for e in i:
            x = (1-alpha)*x + alpha*e
            yield x

    return list(smoother(inp))

def eval_data_backend(data, simple_smooth_count, exponential_smooth_alpha): #data = [(class, spot, filename-map, jam_prob), ...]
                                                                            # -> (false_negatives, false_positives, true_negatives, true_positives)
    # order by spots and filenames
    data.sort(key=lambda z: (z[1], z[2]))

    ground_truth = [c for c,s,f,j in data]

    probabilities = [j for c,s,f,j in data]

    if exponential_smooth_alpha >= 0 and exponential_smooth_alpha < 1:
        probabilities = smooth_exponential(probabilities, exponential_smooth_alpha)

    classes = []
    for p in probabilities:
        if p > 0.5:
            classes.append('jam')
        else:
            classes.append('fluid')

    if simple_smooth_count > 0:
        classes = smooth_changes_simple(classes, simple_smooth_count, 'fluid')

    # do the evaluation
    assert len(ground_truth) == len(classes)

    # create statistics
    false_negatives = 0
    false_positives = 0
    true_negatives = 0
    true_positives = 0

    for i in range(0, len(ground_truth)):
        if classes[i] == 'jam' and ground_truth[i] == 'jam':
            true_positives = true_positives+1
        elif classes[i] == 'jam' and ground_truth[i] == 'fluid':
            false_positives = false_positives+1
        elif classes[i] == 'fluid' and ground_truth[i] == 'fluid':
            true_negatives = true_negatives+1
        elif classes[i] == 'fluid' and ground_truth[i] == 'jam':
            false_negatives = false_negatives+1
        else:
            assert False

    return (false_negatives, false_positives, true_negatives, true_positives)


def evaluate_data(data, simple_smooth_count, exponential_smooth_alpha): #data = [(class, spot, filename-map, jam_prob), ...]
    false_negatives, false_positives, true_negatives, true_positives = eval_data_backend(data, simple_smooth_count, exponential_smooth_alpha)

    print('')
    print('RAW EVAL DATA')
    print('=============')
    print('false negatives: {}'.format(false_negatives))
    print('false positives: {}'.format(false_positives))
    print('true negatives:  {}'.format(true_negatives))
    print('true positives:  {}'.format(true_positives))

    print('')
    print('EVALUATION METRICS')
    print('==================')

    try:
        accurracy = (true_negatives + true_positives) / (false_negatives + false_positives + true_negatives + true_positives)
    except ZeroDivisionError:
        accurracy = float('NaN')

    try:
        precision = (true_positives) / (true_positives + false_positives)
    except ZeroDivisionError:
        precision = float('NaN')

    try:
        recall = (true_positives) / (true_positives + false_negatives)
    except ZeroDivisionError:
        recall = float('NaN')

    try:
        f1 = 2*(precision * recall)/(precision + recall)
    except ZeroDivisionError:
        f1 = float('NaN')

    print('accurracy: {}'.format(accurracy))
    print('precision: {}'.format(precision))
    print('recall:    {}'.format(recall))
    print('f1:        {}'.format(f1))

def epsilon_if_zero(f):
    if f == 0:
        return sys.float_info.epsilon
    else:
        return f

def evaluate_cache_graph(data, title=''): #data = [(class, spot, filename-map, jam_prob), ...]
    print ('alpha,accurracy,precision,recall')

    x = []
    acclist = []
    preclist = []
    reclist = []
    f1list = []

    for i in range(0, 101):
        alpha = i / 100
        x.append(alpha)

        false_negatives, false_positives, true_negatives, true_positives = eval_data_backend(data, 0, alpha)

        #print(f'{alpha},{false_negatives},{false_positives},{true_negatives},{true_positives}')

        accurracy = (true_negatives + true_positives) / (false_negatives + false_positives + true_negatives + true_positives)
        precision = (true_positives) / epsilon_if_zero(true_positives + false_positives)
        recall    = (true_positives) / epsilon_if_zero(true_positives + false_negatives)
        f1        = 2*(precision * recall)/epsilon_if_zero(precision + recall)

        print(f'{alpha},{accurracy},{precision},{recall},{f1}')

        acclist.append(accurracy)
        preclist.append(precision)
        reclist.append(recall)
        f1list.append(f1)

    plt.plot(x, acclist, label='Accurracy')
    plt.plot(x, preclist, label='Precision')
    plt.plot(x, reclist, label='Recall')
    plt.plot(x, f1list, label='F1 Score')
    plt.ylim(0.7, 1.0)
    #plt.yscale('symlog', lintreshy=0.01, subsy=[-1, -2, -3, -4, 1,2,3,4,5,6,7,8,9,10])
    plt.grid(True)
    plt.xlabel('Glättungsfaktor $\\alpha$')
    plt.title(title)
    plt.legend(loc='lower right')
    plt.show()


def evaluate_data_jams(data, simple_smooth_count, exponential_smooth_alpha): #data = [(class, spot, filename-map, jam_prob), ...]
    # order by spots and filenames
    data.sort(key=lambda z: (z[1], z[2]))

    ground_truth = [c for c,s,f,j in data]

    probabilities = [j for c,s,f,j in data]

    if exponential_smooth_alpha > 0 and exponential_smooth_alpha < 1:
        probabilities = smooth_exponential(probabilities, exponential_smooth_alpha)

    classes = []
    for p in probabilities:
        if p > 0.5:
            classes.append('jam')
        else:
            classes.append('fluid')

    if simple_smooth_count > 0:
        classes = smooth_changes_simple(classes, simple_smooth_count, 'fluid')

    # do the evaluation
    assert len(ground_truth) == len(classes)

    # create statistics
    false_negatives = 0
    false_positives = 0
    true_negatives = 0
    true_positives = 0

    WINDOW = 5

    # first, detect jams
    ground_truth_jams = [] # list of (begin, end)
    classified_jams = []

    state = 'FLUID'
    jamstart = -1
    for i in range(0, len(ground_truth)):
        if state == 'FLUID':
            if ground_truth[i] == 'jam':
                jamstart = i
                state = 'JAM'
        if state == 'JAM':
            if ground_truth[i] == 'fluid':
                ground_truth_jams.append((jamstart, i))
                state = 'FLUID'

    state = 'FLUID'
    for i in range(0, len(classes)):
        if state == 'FLUID' and classes[i] == 'jam':
            jamstart = i
            state = 'JAM'
        if state == 'JAM' and classes[i] == 'fluid':
            classified_jams.append((jamstart, i))
            state = 'FLUID'

    # find positives in classified stream
    for start, end in classified_jams:
        for gstart, gend in ground_truth_jams:
            if abs(gstart-start) <= WINDOW and abs(gend-end) <= WINDOW:
                true_positives = true_positives + 1
                break
        else:
            false_positives = false_positives + 1

    # find missed jams
    for gstart, gend in ground_truth_jams:
        # ignore jams smaller than WINDOW
        if abs(gstart-gend) <= WINDOW:
            continue

        for start, end in classified_jams:
            if abs(gstart-start) <= WINDOW and abs(gend-end) <= WINDOW:
                break
        else:
            false_negatives = false_negatives + 1

    print('')
    print('RAW EVAL DATA')
    print('=============')
    print('false negatives: {}'.format(false_negatives))
    print('false positives: {}'.format(false_positives))
    print('true positives:  {}'.format(true_positives))

    print('')
    print('EVALUATION METRICS')
    print('==================')

    precision = (true_positives) / (true_positives + false_positives)
    recall    = (true_positives) / (true_positives + false_negatives)
    f1        = 2*(precision * recall)/(precision + recall)
    print('precision: {}'.format(precision))
    print('recall:    {}'.format(recall))
    print('f1:        {}'.format(f1))

def evaluate(args):
    model = importlib.import_module("models."+args.model)

    datalist = [] # [(class, spot, filename)]
    for i in ['jam', 'fluid']:
        # get spots
        for spotdir in glob.glob(os.path.join(args.datadir, 'eval', args.spot)):
            spotname = os.path.basename(spotdir)
            for f in glob.glob(os.path.join(spotdir, i, '*-MAP.png')):
                fname = os.path.basename(f)
                datalist.append((i, spotname, fname))

    datalist.sort(key=lambda z: (z[1], z[2]))

    imagelist = []
    for clazz, spot, filename in datalist:
        f = os.path.join(args.datadir, 'eval', spot, clazz, filename)
        imagelist.append((f.replace('-MAP.png', '-A.png'), f.replace('-MAP.png', '-B.png'), f))

    result = model.run(imagelist, args.directory)

    assert len(result) == len(imagelist)

    datacache = [x+(y,) for x,y in zip(datalist, result)]

    if args.write_cache is not None:
        pickle.dump(datacache, args.write_cache)

    evaluate_data(datacache, args.smooth_simple, args.smooth_exponential)

def eval_cache_jam(args):
    datacache = pickle.load(args.file)

    evaluate_data_jams(datacache, args.smooth_simple, args.smooth_exponential)

def eval_cache(args):
    datacache = pickle.load(args.file)

    evaluate_data(datacache, args.smooth_simple, args.smooth_exponential)

def eval_cache_graph(args):
    datacache = pickle.load(args.file)

    evaluate_cache_graph(datacache, args.title)


def run(args):
    model = importlib.import_module("models."+args.model)

    imagefile_a = args.imagefile_prefix + '-A.png'
    imagefile_b = args.imagefile_prefix + '-B.png'
    imagefile_map = args.imagefile_prefix + '-MAP.png'

    imglist = [(imagefile_a, imagefile_b, imagefile_map)]

    print(model.run(imglist, args.directory))

def export(args):
    model = importlib.import_module("models."+args.model)

    model.export(args.directory, args.output)

if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Model Runner")
    sap = ap.add_subparsers()
    sap.required=True
    sap.dest='command'

    ap_train = sap.add_parser('train')
    ap_train.add_argument('--model', required=True, help='Model to use')
    ap_train.add_argument('--datadir', required=True, help='Directory with data')
    ap_train.add_argument('--spot', required=True, help='Camera spot (or * for all spots)')
    ap_train.add_argument('--directory', required=True, help='Output directory for the model')
    ap_train.add_argument('--steps', type=int, default=-1, help='Override number of training steps')
    ap_train.set_defaults(func=train)

    ap_eval = sap.add_parser('eval')
    ap_eval.add_argument('--model', required=True, help='Model to use')
    ap_eval.add_argument('--datadir', required=True, help='Directory with data')
    ap_eval.add_argument('--spot', required=True, help='Camera spot (or * for all spots)')
    ap_eval.add_argument('--directory', required=True, help='Output directory for the model')
    ap_eval.add_argument('--smooth-simple', type=int, default=0, help='Only register after x consecutive changes')
    ap_eval.add_argument('--smooth-exponential', type=float, default=1, help='Exponential smoothing alpha value')
    ap_eval.add_argument('--write-cache', type=argparse.FileType('wb'), help='Write classifier result cache to this file')
    ap_eval.set_defaults(func=evaluate)

    ap_ceval = sap.add_parser('eval-cached')
    ap_ceval.add_argument('file', type=argparse.FileType('rb'), help='Load classifier result cache from this file')
    ap_ceval.add_argument('--smooth-simple', type=int, default=0, help='Only register after x consecutive changes')
    ap_ceval.add_argument('--smooth-exponential', type=float, default=1, help='Exponential smoothing alpha value')
    ap_ceval.set_defaults(func=eval_cache)

    ap_cjeval = sap.add_parser('eval-cached-jams')
    ap_cjeval.add_argument('file', type=argparse.FileType('rb'), help='Load classifier result cache from this file')
    ap_cjeval.add_argument('--smooth-simple', type=int, default=0, help='Only register after x consecutive changes')
    ap_cjeval.add_argument('--smooth-exponential', type=float, default=1, help='Exponential smoothing alpha value')
    ap_cjeval.set_defaults(func=eval_cache_jam)

    ap_geval = sap.add_parser('eval-graph')
    ap_geval.add_argument('file', type=argparse.FileType('rb'), help='Load classifier result cache from this file')
    ap_geval.add_argument('--title', type=str, default='Evaluation Results', help='Graph title')
    ap_geval.set_defaults(func=eval_cache_graph)

    ap_run = sap.add_parser('run')
    ap_run.add_argument('--model', required=True, help='Model to use')
    ap_run.add_argument('--directory', required=True, help='Directory containing the trained model')
    ap_run.add_argument('imagefile_prefix', help='Prefix of file to run model against')
    ap_run.set_defaults(func=run)

    ap_export = sap.add_parser('export')
    ap_export.add_argument('--model', required=True, help='Model to use')
    ap_export.add_argument('--directory', required=True, help='Directory containing the trained model')
    ap_export.add_argument('--output', '-o', required=True, help='Output directory')
    ap_export.set_defaults(func=export)

    args = ap.parse_args()
    args.func(args)
