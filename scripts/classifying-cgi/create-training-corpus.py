#!/usr/bin/env python3

import sys
import os
import os.path
import random
import urllib.parse
import sqlite3
import argparse
import shutil
import math
from PIL import Image
from datetime import datetime

ap = argparse.ArgumentParser('create training corpus from db')
ap.add_argument('--dbfile', help='sqlite db', required=True)
ap.add_argument('--cameradir', help='directory with camera pics', required=True)
ap.add_argument('--outdir', help='output directory', required=True)
ap.add_argument('--maskdir', help='directory with mask files', required=True)

args = ap.parse_args()

db = sqlite3.connect(args.dbfile)
db.row_factory = sqlite3.Row

with db:
    for spot in db.execute('''
                select POINTS.POINT_ID, POINTS.DESCRIPTION, SHOTS.HUMAN_EVAL,
                       POINTS.MASK_A, POINTS.MASK_B, COUNT(*) as NUM
                from SHOTS join POINTS on SHOTS.POINT_ID = POINTS.POINT_ID
                where SHOTS.HUMAN_EVAL in ('jam', 'fluid')
                group by POINTS.POINT_ID, POINTS.DESCRIPTION, SHOTS.HUMAN_EVAL,
                         POINTS.MASK_A, POINTS.MASK_B'''):

        odir_train = os.path.join(args.outdir, 'train', spot['DESCRIPTION'], spot['HUMAN_EVAL'])
        odir_eval  = os.path.join(args.outdir, 'eval',  spot['DESCRIPTION'], spot['HUMAN_EVAL'])

        os.makedirs(odir_train, exist_ok=True)
        os.makedirs(odir_eval, exist_ok=True)

        LIMIT = 600
        n_training = math.floor(min(int(spot['NUM']), LIMIT) * 2 / 3)

        mask_a = Image.open(os.path.join(args.maskdir, spot['MASK_A'])).convert('RGBA')
        mask_b = Image.open(os.path.join(args.maskdir, spot['MASK_B'])).convert('RGBA')


        i = 0
        for pic in db.execute('''
                    select SHOTS.TIMESTAMP, SHOTS.FILE_A, SHOTS.FILE_B, SHOTS.MAP_VAL
                    from SHOTS join POINTS on SHOTS.POINT_ID = POINTS.POINT_ID
                    where SHOTS.POINT_ID = ? and SHOTS.HUMAN_EVAL = ?
                    order by RANDOM() limit ?''', (spot['POINT_ID'], spot['HUMAN_EVAL'], LIMIT)):

            timestamp = datetime.strptime(pic['TIMESTAMP'], '%Y-%m-%dT%H:%M:%S')

            if i < n_training:
                odir = odir_train
            else:
                odir = odir_eval

            try:
                img_a = Image.open(os.path.join(args.cameradir, pic['FILE_A'])).convert('RGBA')
            except OSError as e:
                img_a = None
                print('ERROR opening {} for "{}" on "{}: {}'.format(pic['FILE_A'], spot['DESCRIPTION'], pic['TIMESTAMP'], e))

            try:
                img_b = Image.open(os.path.join(args.cameradir, pic['FILE_B'])).convert('RGBA')
            except OSError as e:
                img_b = None
                print('ERROR opening {} for "{}" on "{}: {}'.format(pic['FILE_B'], spot['DESCRIPTION'], pic['TIMESTAMP'], e))

            if img_b is None or img_a is None:
                continue

            combined_a = Image.alpha_composite(img_a, mask_a)
            combined_b = Image.alpha_composite(img_b, mask_b)

            fname_a = os.path.join(odir, '{:%Y%m%d-%H%M}-A.png'.format(timestamp))
            fname_b = os.path.join(odir, '{:%Y%m%d-%H%M}-B.png'.format(timestamp))
            fname_m = os.path.join(odir, '{:%Y%m%d-%H%M}-MAP.png'.format(timestamp))

            combined_a.save(fname_a)
            combined_b.save(fname_b)

            map_val = int(pic['MAP_VAL'])
            b = map_val & 0xFF
            g = (map_val >> 8) & 0xFF
            r = (map_val >> 16) & 0xFF

            img_map = Image.new('RGB', (1, 1))
            pixels = img_map.load()
            pixels[0, 0] = (r, g, b)
            img_map.save(fname_m)

            i = i + 1
