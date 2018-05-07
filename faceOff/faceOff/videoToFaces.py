import cv2
import sys
import numpy as np
from umeyama import umeyama
from faceFinder import *

FILE = 'vids/Cage_Large.mp4'
SKIP = 4
SIZE = 256
MAX_FACES = 500

def main():
	video = cv2.VideoCapture(FILE)
	width = int(video.get(cv2.CAP_PROP_FRAME_WIDTH))
	height = int(video.get(cv2.CAP_PROP_FRAME_HEIGHT))
	fps = int(video.get(cv2.CAP_PROP_FPS))
	frames_count = int(video.get(cv2.CAP_PROP_FRAME_COUNT))

	i = 0
	foundFaces = 0

	while True:
		end, frame = video.read()

		if end == False:
			break
		
		if i % SKIP == 0:
			try:
				imgs, _ = getFaces(frame, SIZE)

				for j in range(0, len(imgs)):
					cv2.imwrite(
						'out/' +
						str(i) + '_' + str(j) + '.jpg',
						imgs[j])

					foundFaces += 1

				if MAX_FACES < foundFaces:
					break

			except:
				print('Error: ', sys.exc_info()[0])

		i += 1


if __name__ == '__main__':
	main()