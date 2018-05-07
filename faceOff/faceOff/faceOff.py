import os
import numpy as np
import cv2
import random
from learner import *

TRAIN_DIR = ['imgsA', 'imgsB']
EPOCHS = 1000000
UPDATE = 1

seed = random.randint(0, 2 ** 32 - 1)
	
def main():
	np.random.seed(seed)
	random.seed(seed)

	files = [None, None]
	for i in range(0, 2):
		files[i] = getImgs(TRAIN_DIR[i])

	dMean = getDMean(files[0], files[1])

	encoder, decoders, autoEnc = genEncoders()

	for ep in range(0, EPOCHS):
		loss = [0, 0]

		warps = [None, None]
		targets = [None, None]
		for i in range(0, 2):
			warps[i], targets[i] = getTrain(files[i], dMean if i == 0 else 0, BATCH)
			loss[i] = autoEnc[i].train_on_batch(warps[i], targets[i])
		
		if ep % UPDATE == 0:
			tA = targets[0][0].reshape((1,) + targets[0][0].shape)
			tB = targets[1][0].reshape((1,) + targets[1][0].shape)

			for i in range(0, 2):
				outImg = autoEnc[i].predict(tA)[0]
				cv2.imwrite(str(i) + '-0.png', outImg * 255.0)

			for i in range(0, 2):
				outImg = autoEnc[i].predict(tB)[0]
				cv2.imwrite(str(i) + '-1.png', outImg * 255.0)
				
			save(encoder, decoders)

			print('EPOCH: ' + str(ep))
			print('\tLoss: ' + str(loss[0]) + ', ' + str(loss[1]))
			
if __name__ == '__main__':
	main()