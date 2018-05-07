import os
import numpy as np
import cv2
import random
import copy
from faceFinder import *
from learner import *

LOAD_DIR = 'orig'
OUT_DIR = 'out'

def swap(img, downSample, autoEnc):
	faces, landmarks = getFaces(img, 256)
	img = copy.copy(img)

	for face, landmark in zip(faces, landmarks):
		_, nImg, src, dst = randWarp(face)
		nImg = nImg / 255.0

		# use encoder
		feed = nImg.reshape((1,) + nImg.shape)
		out = autoEnc[1].predict(feed)
		
		createFace = np.clip(np.squeeze(out[0]) * 255.0, 0, 255.0).astype('uint8')
		inv = umeyama(dst, src, True)[0:2]

		outFace = blend(createFace, face, inv)

		# past face
		aligned = alignFace(landmark)
		aligned *= (256 - 2 * 48)
		aligned[:, 2] += 48
		alignInv = cv2.invertAffineTransform(aligned)

		img = blend(outFace, img, alignInv)

	return cv2.resize(img, (img.shape[1] // downSample, img.shape[0] // downSample))

def main():
	encoder, decoders, autoEnc = genEncoders()
	files = getImgs(LOAD_DIR)

	num = 0

	for file in files:
		img = cv2.imread(file)
		img = np.uint8(img)

		nImg = swap(img, 2, autoEnc)

		cv2.imshow('image', nImg)
		cv2.waitKey(0)
		cv2.destroyAllWindows()

		cv2.imwrite('final/' + str(num) + '.png', nImg)

		num += 1

if __name__ == '__main__':
	main()
