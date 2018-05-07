import os
import numpy as np
import cv2
from umeyama import umeyama
from pixel_shuffler import PixelShuffler

from keras.models import Model
from keras.layers import Input, Dense, Flatten, Reshape
from keras.layers.convolutional import Conv2D
from keras.layers.advanced_activations import LeakyReLU
from keras.optimizers import Adam

BATCH = 64
IMG_SIZE = (64, 64, 3)
ENC_SIZE = 1 << 10

optimizer = Adam(lr=5e-5, beta_1=0.5, beta_2=0.999)

def getImgs(dir):
	inList = os.listdir(dir)
	return [dir + '/' + file for file in inList]

def up(x):
	def output(input):
		input = Conv2D(x * 4, kernel_size = 3, padding = 'same')(input)
		input = LeakyReLU(0.1)(input)
		input = PixelShuffler()(input)

		return input
	return output

def encode():
	input = Input(shape = IMG_SIZE)

	var = input
	for i in range(0, 4):
		off = 1 << (7 + i)
		conv = Conv2D(off, kernel_size = 5, strides = 2, padding = 'same')(var)
		var = LeakyReLU(.1)(conv)

	var = Dense(ENC_SIZE)(Flatten()(var))
	var = Dense(1 << 14)(var)
	var = Reshape((1 << 2, 1 << 2, 1 << 10))(var)
	var = up(1 << 9)(var)

	return Model(input, var)

def decode():
	input = Input(shape = (1 << 3, 1 << 3, 1 << 9))
	
	var = input
	for i in reversed(range(0, 3)):
		off = 1 << (6 + i)
		var = up(off)(var)

	var = Conv2D(3, kernel_size = 5, padding = 'same', activation = 'sigmoid')(var)

	return Model(input, var)

def genEncoders():
	input = Input(shape=IMG_SIZE)
	
	encoder = encode()
	decoders = [None, None]
	
	autoEnc = []
	for i in range(0, 2):
		decoders[i] = decode()

		autoEnc.append(Model(input,
				decoders[i](encoder(input))))
		autoEnc[-1].compile(optimizer = optimizer,
			loss = 'mean_absolute_error')
			
	# load
	try:
		encoder.load_weights('models/encoder.h5')
		decoders[0].load_weights('models/decoder_0.h5')
		decoders[1].load_weights('models/decoder_1.h5')
	except:
		pass

	return encoder, decoders, autoEnc

def randAlt(img, rotR, zoomR, transR, flip):
	height, width = img.shape[0 : 2]
	rot = np.random.uniform(-rotR, rotR)
	scale = np.random.uniform(1 - zoomR, 1 + zoomR)
	dx = np.random.uniform(-transR, transR) * width
	dy = np.random.uniform(-transR, transR) * height

	nImg = cv2.getRotationMatrix2D((width // 2, height // 2),
		rot,
		scale)

	# shift
	nImg[:, 2] += (dx, dy)
	out = cv2.warpAffine(img,
		nImg,
		(width, height),
		borderMode = cv2.BORDER_REPLICATE)

	# check for flip
	if flip < np.random.random():
		out = out[:, ::-1]

	return out

def randWarp(img):
	range = np.linspace(128 - 80, 128 + 80, 5)
	x = np.broadcast_to(range, (5, 5))
	y = x.T

	x = x + np.random.normal(size = (5, 5), scale = 5)
	y = y + np.random.normal(size = (5, 5), scale = 5)

	mapx = cv2.resize(x, (80, 80))[8:72, 8:72].astype('float32')
	mapy = cv2.resize(y, (80, 80))[8:72, 8:72].astype('float32')

	warp = cv2.remap(img, mapx, mapy, cv2.INTER_LINEAR)
	src = np.stack([x.ravel(), y.ravel()], axis = -1)
	dst = np.mgrid[0:65 : 16, 0 : 65 : 16].T.reshape(-1, 2)
	nImg = umeyama(src, dst, True)[0 : 2]
	nImg = cv2.warpAffine(img, nImg, (64, 64))

	return warp, nImg, src, dst

def getTrain(files, dMean, num):
	lookup = np.random.randint(len(files),  size = num)
	
	for i in range(0, num):
		path = files[lookup[i]]

		img = cv2.imread(path) / 255.0
		img += dMean
		
		img = randAlt(img, 10, .05, .05, .4)
		warpImg, nImg, _, _ = randWarp(img)

		if i == 0:
			warps = np.empty((BATCH,) + warpImg.shape, warpImg.dtype)
			targets = np.empty((BATCH,) + nImg.shape, warpImg.dtype)

		warps[i] = warpImg
		targets[i] = nImg

	return warps, targets

def getDMean(A, B):
	meanA = (0, 0, 0)

	for file in A:
		img = cv2.imread(file) / 255.0
		meanA += img.mean(axis = (0, 1, 2)) / len(A)

	meanB = (0, 0, 0)
	for file in B:
		img = cv2.imread(file) / 255.0
		meanB += img.mean(axis = (0, 1, 2)) / len(B)

	return meanB - meanA

def blend(src, dst, img):
	mask = np.ones(src.shape)
	mask[0, :, :] = mask[:, 0, :] = mask[-1, 0, :] = mask[:, -1, :] = 0

	warped = cv2.warpAffine(mask, img, (dst.shape[1], dst.shape[0]))[:, :, 0]

	sWarp = cv2.warpAffine(src, img, (dst.shape[1], dst.shape[0]))

	blend = warped * .95

	cpDst =  dst.copy()
	for chan in range(0, 3):
		cpDst[:, :, chan] = ((1.0 - blend) * dst[:, :, chan] + blend * sWarp[:, :, chan])

	return cpDst

def save(encoder, decoders):
	encoder.save_weights("models/encoder.h5")
	decoders[0].save_weights("models/decoder_0.h5")
	decoders[1].save_weights("models/decoder_1.h5")
