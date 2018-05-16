#!/usr/bin/env python3

from datetime import datetime
import os, os.path
import re
from glob import glob
import sqlite3
from PIL import Image
import collections
import argparse

ap = argparse.ArgumentParser('camera pics importer')
ap.add_argument('--dbfile', help='sqlite database file', required=True)
ap.add_argument('--picdir', help='directory with the raw image data gathered by the scraper', required=True)
ap.add_argument('--since', help='only consider dates past this (format: YYYYMMDD)', default='00000000')

args = ap.parse_args()

db = sqlite3.connect(args.dbfile)
db.row_factory = sqlite3.Row

# config: in/out directories
days = sorted(x for x in os.listdir(args.picdir) if re.match('^[0-9]+$', x) and x >= args.since)
os.chdir(args.picdir)

PointCacheEntry = collections.namedtuple('PointCacheEntry', ['pointid', 'camera_a', 'camera_b', 'map_x', 'map_y'])

# cache camera locations
pointcache = []
for row in db.execute('select POINT_ID, CAMERA_A, CAMERA_B, MAP_X, MAP_Y from POINTS'):
    pointcache.append(PointCacheEntry._make(tuple(row)))

for day in days:
    print('Progress: {}'.format(day))

    with db:
        for hour in range(0, 24):
            for minute in range(0, 60):
                jpgglob = '{}{:02}{:02}*.jpg'.format(day, hour, minute)
                pngglob = '{}{:02}{:02}*.png'.format(day, hour, minute)

                kartefile = next(iter(glob(os.path.join(day, 'Verkehrskarte', pngglob))), None)
                karte = None
                if kartefile is not None:
                    try:
                        karte = Image.open(kartefile)
                        karte = karte.convert('RGBA')
                    except Exception as e:
                        print('ERROR while opening map: {}'.format(e))
                        karte = None

                for point in pointcache:
                    # find camera jpgs
                    file_a = next(iter(glob(os.path.join(day, point.camera_a, jpgglob))), None)
                    file_b = next(iter(glob(os.path.join(day, point.camera_b, jpgglob))), None)
                    if file_a is not None:
                        try:
                            ia = Image.open(file_a)
                            ia.load()
                        except Exception as e:
                            print('WARNING: Invalid image {}: {}'.format(file_a, e))
                            file_a = None
                    if file_b is not None:
                        try:
                            ia = Image.open(file_b)
                            ia.load()
                        except Exception as e:
                            print('WARNING: Invalid image {}: {}'.format(file_b, e))
                            file_b = None

                    if file_a is None and file_b is None:
                        continue

                    map_pixel = 0
                    if karte is not None:
                        r, g, b, a = karte.getpixel((point.map_x, point.map_y))
                        map_pixel = (r << 16) | (g << 8) | b

                    timestamp = datetime.strptime('{}{:02}{:02}'.format(day, hour, minute), '%Y%m%d%H%M')

                    try:
                        db.execute('insert into SHOTS (POINT_ID, TIMESTAMP, FILE_A, FILE_B, MAP_VAL) values (?,?,?,?,?)',
                                   (point.pointid, timestamp.isoformat(), file_a, file_b, map_pixel))
                    except sqlite3.IntegrityError as e:
                        pass # HACK: just ignore unique constraint violations
