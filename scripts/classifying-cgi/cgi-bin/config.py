import collections
import os, os.path

mydir = os.path.dirname(os.path.realpath(__file__))
cameraurl = 'https://zeugs.genosse-einhorn.de/studienarbeit2'
maskurl = 'https://zeugs.genosse-einhorn.de/studienarbeit-classified/masks'
dbfile = os.path.join(mydir, '..', '..', 'studienarbeit-classified', 'camerapics.db')
