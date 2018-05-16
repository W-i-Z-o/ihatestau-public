#!/usr/bin/env python3.6

import cgi
import cgitb; cgitb.enable()
import sys
import os
import os.path
import random
import urllib.parse
import sqlite3

import config

allowed_classes = {'fluid', 'jam', 'unsure'}

db = sqlite3.connect(config.dbfile)

form = cgi.FieldStorage()

count = int(form.getfirst('pic_count', '0'))

with db: 
    for i in range(0, count):
        shotid = form.getfirst('shotid_{}'.format(i))
        result = form.getfirst('human_eval_{}'.format(i))
        
        if result in allowed_classes:
            db.execute('update SHOTS set HUMAN_EVAL=? where SHOT_ID=?', (result, shotid))
    
print('Status: 303 See Other')
if form.getfirst('redirect_to') is not None:
    print('Location: {}'.format(urllib.parse.quote(form.getfirst('redirect_to'))))
else:
    print('Location: get-image-2.py?datefrom={}&pointid={}'.format(
        urllib.parse.quote(form.getfirst('datefrom')), urllib.parse.quote(form.getfirst('pointid'))))
print('Content-Type: text/html; charset=UTF-8')
print()
print('You should have been redirected automatically.')
