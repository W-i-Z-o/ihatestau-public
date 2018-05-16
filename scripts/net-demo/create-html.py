#!/usr/bin/env python3

import os, os.path
from datetime import datetime
from glob import glob
import shutil
from PIL import Image

CAMERADIR='/srv/zeugs/studienarbeit2'
MASKDIR='/srv/zeugs/studienarbeit-classified/masks'
TMPDIR=os.path.join(os.path.dirname(os.path.realpath(__file__)), 'tmp')

# static camera data
cameradata = [
    # (Description, Camera_A, Mask_A, Camera_B, Mask_B, Map_X, Map_Y)
    ('Bruchsal Fahrtrichtung Karlsruhe',
        'Bruchsal Fr Heidelberg', 'bruchsal-br-heidelberg-fr-karlsruhe.png',
        'Bruchsal Fr Karlsruhe', 'bruchsal-br-karlsruhe-fr-karlsruhe.png',
        720, 117),
    ('Bruchsal Fahrtrichtung Heidelberg',
        'Bruchsal Fr Karlsruhe', 'bruchsal-br-karlsruhe-fr-heidelberg.png',
        'Bruchsal Fr Heidelberg', 'bruchsal-br-heidelberg-fr-heidelberg.png',
        724, 125),
    ('KA Nord Fahrtrichtung Basel',
        'KA Nord Fr Heidelberg', 'kanord-br-heidelberg-fr-basel.png',
        'KA Nord Fr Basel', 'kanord-br-basel-fr-basel.png',
        495, 595),
    ('KA Nord Fahrtrichtung Heidelberg',
        'KA Nord Fr Basel', 'kanord-br-basel-fr-heidelberg.png',
        'KA Nord Fr Heidelberg', 'kanord-br-heidelberg-fr-heidelberg.png',
        490, 613),
    ('KA Mitte Fahrtrichtung Basel',
        'KA Mitte Fr Heidelberg', 'kamitte-br-heidelberg-fr-basel.png',
        'KA Mitte Fr Basel', 'kamitte-br-basel-fr-basel.png',
        410, 692),
    ('KA Mitte Fahrtrichtung Heidelberg',
        'KA Mitte Fr Basel', 'kamitte-br-basel-fr-heidelberg.png',
        'KA Mitte Fr Heidelberg', 'kamitte-br-heidelberg-fr-heidelberg.png',
        416, 711),
    ('Ettlingen Fahrtrichtung Basel',
        'Ettlingen Fr Heidelberg', 'ettlingen-br-heidelberg-fr-basel.png',
        'Ettlingen Fr Basel', 'ettlingen-br-basel-fr-basel.png',
        346, 814),
    ('Ettlingen Fahrtrichtung Heidelberg',
        'Ettlingen Fr Basel', 'ettlingen-br-basel-fr-heidelberg.png',
        'Ettlingen Fr Heidelberg', 'ettlingen-br-heidelberg-fr-heidelberg.png',
        324, 826)
]


today = '{:%Y%m%d}'.format(datetime.now())
for spotname, camera_a, mask_a, camera_b, mask_b, mapx, mapy in cameradata:
    mask_a = Image.open(os.path.join(MASKDIR, mask_a))
    mask_b = Image.open(os.path.join(MASKDIR, mask_b))

    file_a = next(reversed(sorted(glob(os.path.join(CAMERADIR, today, camera_a, '*.jpg')))), None)
    file_b = next(reversed(sorted(glob(os.path.join(CAMERADIR, today, camera_b, '*.jpg')))), None)
    
    #if file_a is None or file_b is None:
    #    continue
        
    try:
        img_a = Image.open(file_a).convert('RGBA')
    except OSError as e:
        img_a = None
        # Don't report it because almost every picture from Bruchsal is broken. So just don't bother.
        #print('ERROR opening {}: {}'.format(file_a, e))

    try:
        img_b = Image.open(file_b).convert('RGBA')
    except OSError as e:
        img_b = None
        #print('ERROR opening {}: {}'.format(file_b, e))

    if img_b is None or img_a is None:
        continue

    combined_a = Image.alpha_composite(img_a, mask_a)
    combined_b = Image.alpha_composite(img_b, mask_b)

    fname_a = os.path.join(TMPDIR, '{}-A.png'.format(spotname))
    fname_b = os.path.join(TMPDIR, '{}-B.png'.format(spotname))

    combined_a.save(fname_a)
    combined_b.save(fname_b)
    
# ask the neural net and generate HTML
os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
NETDIR = '/srv/zeugs/studienarbeit-net/model-multipic-conv-net-KAMitte-Fr-Basel/'
import second_try_net as net

HTMLFILE = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'demo.html')

with open(HTMLFILE, 'w') as f:
    f.write('<!DOCTYPE html>')
    f.write('<meta charset=UTF-8>')
    f.write('<style>td a img { display: block; }</style>')
    f.write('<title>iHateStau-Net-Demo</title>')
    
    f.write('Generated: {}'.format(datetime.now()))
    
    prefixes = []
    spots = []
    for spot, _, _, _, _, _, _ in cameradata:
        if os.path.isfile(os.path.join(TMPDIR, spot + '-A.png')) and os.path.isfile(os.path.join(TMPDIR, spot + '-B.png')):
            prefixes.append(os.path.join(TMPDIR, spot))
            spots.append(spot)
    

    for spot, result in zip(spots, net.run(NETDIR, prefixes)):
        f.write('<h1>{}</h1>'.format(spot))
        
        f.write('<table border><tr>')
        
        f.write('<td><a href="tmp/{}-A.png"><img src="tmp/{}-A.png" width=320 height=240></a>'.format(spot, spot))
        f.write('<td><a href="tmp/{}-B.png"><img src="tmp/{}-B.png" width=320 height=240></a>'.format(spot, spot))
        
        jam = result['probabilities'][0]
        fluid = result['probabilities'][1]
        
        if jam > fluid:
            bgcolor = 'red'
            color = 'white'
        else:
            bgcolor = 'green'
            color = 'black'
        
        f.write('<td style="color: {}; background-color: {};">jam={:.6f} fluid={:.6f}'.format(color, bgcolor, jam, fluid))

        f.write('</table>')
