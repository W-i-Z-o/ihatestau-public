#!/usr/bin/env python3

import sqlite3

db = sqlite3.connect('./camerapics.db')

# create db structure
with db:
    db.execute('''
        create table POINTS (
            POINT_ID integer primary key,
            DESCRIPTION varchar,
            CAMERA_A varchar,
            MASK_A varchar,
            CAMERA_B varchar,
            MASK_B varchar,
            MAP_X integer,
            MAP_Y integer)
        ''')
    db.execute('''
        create table SHOTS (
            SHOT_ID integer primary key,
            POINT_ID integer,
            TIMESTAMP datetime,
            FILE_A varchar,
            FILE_B varchar,
            MAP_VAL integer,
            HUMAN_EVAL varchar,
            foreign key(POINT_ID) references POINTS(POINT_ID),
            unique(POINT_ID, TIMESTAMP))
        ''')
    db.execute('''
        create index SHOTS_DATE_IDX on SHOTS(TIMESTAMP)
        ''')

# inport static camera data
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


with db:
    db.executemany('''insert into POINTS (DESCRIPTION, CAMERA_A, MASK_A, CAMERA_B, MASK_B, MAP_X, MAP_Y)
                      values (?,?,?,?,?,?,?)''', cameradata)

