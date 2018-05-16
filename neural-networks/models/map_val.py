
from PIL import Image

def train(data, directory):
    print("Nothing to train.")

def _run(imagelist):
    for a,b,m in imagelist:
        i = Image.open(m).convert('RGB')
        r,g,b = i.getpixel((0, 0))
        if (r,g,b) == (255, 0, 0):
            yield 1.0
        else:
            yield 0.0

def run(imagelist, directory):
    return list(_run(imagelist))
