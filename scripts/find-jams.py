#!/usr/bin/env python3

# Little script to find images where the map indicates a traffic jam.

from PIL import Image
import sys
from glob import glob
import os
import os.path
import shutil
import re

# config: cameras and their map positions
cameras = {
    # spot / direction id: (map_x, map_y, Image Folder)
    'bruchsal-br-heidelberg-fr-karlsruhe': (720, 117, 'Bruchsal Fr Heidelberg'),
    'bruchsal-br-karlsruhe-fr-karlsruhe': (720, 117, 'Bruchsal Fr Karlsruhe'),
    'bruchsal-br-heidelberg-fr-heidelberg': (724, 125, 'Bruchsal Fr Heidelberg'),
    'bruchsal-br-karlsruhe-fr-heidelberg': (724, 125, 'Bruchsal Fr Karlsruhe'),
    'kanord-br-heidelberg-fr-basel': (495, 595, 'KA Nord Fr Heidelberg'),
    'kanord-br-heidelberg-fr-heidelberg': (500, 599, 'KA Nord Fr Heidelberg'),
    'kanord-br-basel-fr-basel': (483, 611, 'KA Nord Fr Basel'),
    'kanord-br-basel-fr-heidelberg': (490, 613, 'KA Nord Fr Basel'),
    'kamitte-br-heidelberg-fr-basel': (410, 692, 'KA Mitte Fr Heidelberg'),
    'kamitte-br-heidelberg-fr-heidelberg': (417, 697, 'KA Mitte Fr Heidelberg'),
    'kamitte-br-basel-fr-basel': (408, 710, 'KA Mitte Fr Basel'),
    'kamitte-br-basel-fr-heidelberg': (416, 711, 'KA Mitte Fr Basel'),
    'ettlingen-br-heidelberg-fr-basel': (346, 814, 'Ettlingen Fr Heidelberg'),
    'ettlingen-br-heidelberg-fr-heidelberg': (344, 822, 'Ettlingen Fr Heidelberg'),
    'ettlingen-br-basel-fr-basel': (323, 819, 'Ettlingen Fr Basel'),
    'ettlingen-br-basel-fr-heidelberg': (324, 826, 'Ettlingen Fr Basel')
}

# config: in/out directories
mydir = os.path.dirname(os.path.realpath(__file__))
indir = os.path.join(mydir, 'raw', 'studienarbeit2')
outdir = os.path.join(mydir, 'potential-jam')
days = sorted(x for x in os.listdir(indir) if re.match('^[0-9]+$', x))

for day in days:
    for fname in sorted(glob(os.path.join(indir, day, 'Verkehrskarte', '*.png'))):
        print('checking {!r}'.format(fname))
        i = Image.open(fname)
        i = i.convert('RGBA')

        for spot, (x, y, camera) in cameras.items():
            os.makedirs(os.path.join(outdir, spot), exist_ok=True)

            r, g, b, a = i.getpixel((x, y))

            if (r, g, b, a) == (255, 0, 0, 255):
                # jam!
                daytime = os.path.basename(fname)[0:12]

                for camerafile in glob(os.path.join(indir, day, camera, daytime+'*.jpg')):
                    shutil.copy2(camerafile, os.path.join(outdir, spot))
