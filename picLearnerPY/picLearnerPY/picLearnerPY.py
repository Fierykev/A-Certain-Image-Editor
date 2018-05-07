import tensorflow as tf
from tensorflow.python.framework import graph_util
import numpy as np
import cv2
import os
import sys
import random
import math
import time
import re

from tensorflow.python.tools.inspect_checkpoint import print_tensors_in_checkpoint_file

CHECK_DIR = "models"
TRAIN_DIR = "train/edges2shoes/train"
TEST_DIR = "test"
EXPORT_FILE = "file:///android_asset/input.jpg"
EXECUTION_TYPE = "export"

LEARNING_RATE = .0002
BETA = .5
MAX_EPOCHS = 200
SAVE_FREQ = 5000

SEP_CONV = False
NUM_GEN_FILTERS = 64
NUM_DISC_FILTERS = 64

IN_SIZE_W = 256
IN_SIZE_H = 256

OUT_SIZE_W = 256
OUT_SIZE_H = 256

SCALE_SIZE = 286
CROP_SIZE = 256

BATCH_SIZE = 1

GANW = 1.0
L1W = 100.0
EPSILON = 1e-12

seed = random.randint(0, 2 ** 32 - 1)

def inputTrans(image):
	# get rand offset
	if EXECUTION_TYPE == "train":
		image = tf.image.random_flip_left_right(image, seed=seed)
		image = tf.image.resize_images(image, [SCALE_SIZE, SCALE_SIZE], method=tf.image.ResizeMethod.AREA)
		off = tf.cast(tf.floor(tf.random_uniform([2], 0, SCALE_SIZE - CROP_SIZE + 1, seed=seed)), dtype=tf.int32)

		return tf.image.crop_to_bounding_box(image, off[0], off[1], CROP_SIZE, CROP_SIZE)
	else:
		return tf.image.resize_images(image, [CROP_SIZE, CROP_SIZE], method=tf.image.ResizeMethod.AREA)

def setupBatch():
	if EXECUTION_TYPE == "train":
		inList = os.listdir(TRAIN_DIR)
	elif EXECUTION_TYPE == "test" or EXECUTION_TYPE == "export":
		inList = os.listdir(TEST_DIR)
		
	# Kevin - I don't see a need if we shuffle anyways
	#inList = sorted(inList, key=lambda x: int(os.path.splitext(x)[0]))

	if EXECUTION_TYPE == "train":
		inList = [TRAIN_DIR + "/" + i for i in inList]
	elif EXECUTION_TYPE == "test" or EXECUTION_TYPE == "export":
		inList = [TEST_DIR + "/" + i for i in inList]

	if EXECUTION_TYPE != "export":
		inQueue = tf.train.string_input_producer(inList, shuffle=True, seed=seed)
	else:
		inQueue = tf.train.string_input_producer(inList, shuffle=None)

	path, data = tf.WholeFileReader().read(inQueue)
	convData = tf.image.convert_image_dtype(tf.image.decode_jpeg(data), dtype=tf.float32)
	
	channelCheck = tf.assert_equal(tf.shape(convData)[2], 3, message="Image does not have 3 channels")
	with tf.control_dependencies([channelCheck]):
		tf.identity(convData)

	convData.set_shape([None, None, 3]) # force 3 chan

	# to [-1, 1] range
	w = tf.shape(convData)[1] # width

	# split down the middle and scale to [-1, 1]
	A = convData[:, : w // 2, :] * 2 - 1
	B = convData[:, w // 2 :, :] * 2 - 1

	# A -> B
	input = inputTrans(A)
	target = inputTrans(B)

	batchPath, batchIn, batchTarget = tf.train.batch([path, input, target], batch_size = BATCH_SIZE)
	steps = int(math.ceil(len(inList) / BATCH_SIZE))

	return [batchIn, batchTarget, steps]

def conv(input, outputChan):
	initializer = tf.random_normal_initializer(0, .02)
	
	return tf.layers.conv2d(input, outputChan, kernel_size=4, strides=(2, 2), padding="same", kernel_initializer=initializer)

def deconv(input, outputChan):
	initializer = tf.random_normal_initializer(0, .02)
	return tf.layers.conv2d_transpose(input, outputChan, kernel_size=4, strides=(2, 2), padding="same", kernel_initializer=initializer)

def disconv(input, outputChan, stride):
	pad = tf.pad(input, [[0, 0], [1, 1], [1, 1], [0, 0]], mode="CONSTANT")
	return tf.layers.conv2d(pad, outputChan, kernel_size=4, strides=(stride, stride), padding="valid", kernel_initializer=tf.random_normal_initializer(0, .02))

def activation(var, scalar):
	tf.identity(var)
	A = .5 * (1.0 - scalar)
	B = .5 * (1.0 + scalar)
	return tf.abs(var) * A + var * B

def norm(A):
	return tf.layers.batch_normalization(A, axis=3, epsilon=1e-5, momentum=.1, training=True, gamma_initializer=tf.random_normal_initializer(1.0, .02))

def gen(input, chan):
	depth = []

	encoders = [NUM_GEN_FILTERS,	 # 1
		NUM_GEN_FILTERS * 2, # 2
		NUM_GEN_FILTERS * 4, # 3
		NUM_GEN_FILTERS * 8, # 4
		NUM_GEN_FILTERS * 8, # 5
		NUM_GEN_FILTERS * 8, # 6
		NUM_GEN_FILTERS * 8, # 7
		NUM_GEN_FILTERS * 8, # 8
		]

	depth.append(conv(input, encoders[0]))
	
	for el in encoders[1:]:
		out = conv(activation(depth[-1], .2), el)
		depth.append(norm(out))

	decoder = [(NUM_GEN_FILTERS * 8, .5), # 8
		(NUM_GEN_FILTERS * 8, .5), # 7
		(NUM_GEN_FILTERS * 8, .5), # 6
		(NUM_GEN_FILTERS * 8, .0), # 5
		(NUM_GEN_FILTERS * 4, .0), # 4
		(NUM_GEN_FILTERS * 2, .0), # 3
		(NUM_GEN_FILTERS, .0)	  # 2
		]
	
	curDepth = depth[-1]

	encodingLen = len(depth)
	for i in range(0, len(decoder)):
		skip = encodingLen - i - 1

		if i == 0:
			curDepth = depth[-1]
		else:
			curDepth = tf.concat([depth[-1], depth[skip]], axis=3)

		relu = tf.nn.relu(curDepth)

		outChan, dropout = decoder[i]
		out = deconv(relu, outChan)
		
		out = norm(out)

		if .0 < dropout:
			out = tf.nn.dropout(out, keep_prob=1 - dropout)

		depth.append(out)

	# decode 1
	concat = tf.concat([depth[-1], depth[0]], axis=3)
	relu = tf.nn.relu(concat)
	out = deconv(relu, chan)
	depth.append(tf.tanh(out))

	return depth[-1]
	
def disc(input, target):
	depth = []

	newInput = tf.concat([input, target], axis=3)

	# 1
	conv = disconv(newInput, NUM_DISC_FILTERS, 2)

	act = activation(conv, .2)
	depth.append(act)

	# 2 -> 4
	nChan = 3
	for i in range(0, nChan):
		conv = disconv(depth[-1],
		   NUM_DISC_FILTERS * min(1 << (i + 1), 1 << 3),
		  stride = 2 if i < nChan - 1 else 1)

		n = norm(conv)
		act = activation(n, .2)
		depth.append(act)

	# 5
	conv = disconv(act, 1, 1)
	depth.append(tf.sigmoid(conv))

	return depth[-1]

def setupModel(input, target):
	chan = target.get_shape()[3]
	
	with tf.variable_scope("gen"):
		generator = gen(input, chan)

	with tf.variable_scope("disc"):
		predict = disc(input, target)

	with tf.variable_scope("disc", reuse=True):
		dummy = disc(input, generator)

	dLoss = tf.reduce_mean(-tf.log(predict + EPSILON) - tf.log(1.0 - dummy + EPSILON))

	ganLoss = tf.reduce_mean(-tf.log(dummy + EPSILON))
	l1Loss = tf.reduce_mean(tf.abs(target - generator))
	loss = ganLoss * GANW + l1Loss * L1W

	discVars = [v for v in tf.trainable_variables() if v.name.startswith("disc")]
	discOpt = tf.train.AdamOptimizer(LEARNING_RATE, BETA)
	discGrad = discOpt.compute_gradients(dLoss, discVars)
	discTrain = discOpt.apply_gradients(discGrad)

	with tf.control_dependencies([discTrain]):
		genVars = [v for v in tf.trainable_variables() if v.name.startswith("gen")]
		genOpt = tf.train.AdamOptimizer(LEARNING_RATE, BETA)
		genGrad = genOpt.compute_gradients(loss, genVars)
		genTrain = genOpt.apply_gradients(genGrad)

	move = tf.train.ExponentialMovingAverage(decay=.99)
	lossUpd = move.apply([dLoss, ganLoss, l1Loss])

	step = tf.train.get_or_create_global_step()
	n_step = tf.assign(step, step + 1)

	return [tf.group(lossUpd, n_step, genTrain),
		move.average(dLoss),
		move.average(ganLoss),
		move.average(l1Loss),
		generator]

def train(model, steps):
	save = tf.train.Saver(max_to_keep=1)
	sv = tf.train.Supervisor(logdir="log", save_summaries_secs=0, saver=None)
	
	with sv.managed_session() as sess:
		start = time.time()

		stepSize = steps * MAX_EPOCHS

		chkpt = tf.train.latest_checkpoint(CHECK_DIR)

		startStep = 0

		if chkpt != None:
			print("Restoring Vars")
			save.restore(sess, chkpt)
			print("Restored")

			parsed = [int(x.group()) for x in re.finditer(r'\d+', chkpt)]
			startStep = int(parsed[0])

		for step in range(startStep, stepSize):
			print("Step: " + str(step) + "/" + str(stepSize))

			request = {
				"train" : model[0],
				"disc" : model[1],
				"gan" : model[2],
				"l1" : model[3]
			}

			info = sess.run(request)

			print("Discriminator Loss: " + str(info["disc"]))
			print("GAN Loss: " + str(info["gan"]))
			print("L1 Loss: " + str(info["l1"]))

			print("-----------------------------------------")

			if step % SAVE_FREQ == 0:
				print("Model Saved")
				save.save(sess, "models/train", global_step=sv.global_step)

			if sv.should_stop():
				break

		# save finished product
		save.save(sess, "models/train", global_step=sv.global_step)

		print("Finished")

def imageConvert(image):
	return tf.image.convert_image_dtype(image, dtype = tf.uint8, saturate = True)

def createNet():
	out = setupBatch()
	model = setupModel(out[0], out[1])

	train(model, out[2])

def saveFile(image):
	with open("save/test.png", "wb") as file:
		file.write(image)

def tester():
	print("Testing")

	out = setupBatch()
	model = setupModel(out[0], out[1])

	#graph = tf.Graph()
	#with graph.as_default():
	#saver = tf.train.import_meta_graph(META_GRAPH)
	save = tf.train.Saver(max_to_keep=1)

	sv = tf.train.Supervisor(logdir="log", save_summaries_secs=0, saver=None)
	
	with sv.managed_session() as sess:
		sess.graph._unsafe_unfinalize()

		chkpt = tf.train.latest_checkpoint(CHECK_DIR)

		print("Restoring Vars")
		save.restore(sess, chkpt)
		print("Restored")

		generator = tf.get_default_graph().get_tensor_by_name("gen/Tanh:0")

		iConv = imageConvert((generator + 1) / 2)
		iOut = tf.image.convert_image_dtype(iConv, dtype=tf.uint8)[0]
		png = tf.image.encode_png(iOut)
			
		# run generator
		fetch = {
			"out" : png
		}
		print("Running")
		info = sess.run(fetch)
		print("DONE")

		saveFile(info["out"])

def getGraphData():
	tf.reset_default_graph()

	out = setupBatch()
	model = setupModel(out[0], out[1])

	save = tf.train.Saver(max_to_keep=1)

	sv = tf.train.Supervisor(logdir="log", save_summaries_secs=0, saver=None)
	
	with sv.managed_session() as sess:
		sess.graph._unsafe_unfinalize()

		chkpt = tf.train.latest_checkpoint(CHECK_DIR)

		print("Restoring Vars")
		save.restore(sess, chkpt)
		print("Restored")
		
		fetch = {}
		for i in tf.get_collection(tf.GraphKeys.GLOBAL_VARIABLES, scope='.'):
			fetch[i.name] = i
			
		return sess.run(fetch)
	
def createExportGraph(data):
	tf.reset_default_graph()

	pIn = tf.placeholder(tf.float32, shape=(1, None, None, 3), name="input")
	input = pIn * 2 - 1
		
	chan = input.get_shape()[3]
	
	with tf.variable_scope("gen"):
		generator = gen(input, chan)
	
	iConv = (generator + 1) / 2
	iOut = tf.clip_by_value(
		iConv,
		0,
		1,
		name=None
		)
	out = tf.identity(iOut, name="output")	
	
	sess = tf.Session()
	
	print("Transfering Values")
	for i in tf.get_collection(tf.GraphKeys.GLOBAL_VARIABLES, scope='.'):
		sess.run(i.assign(data[i.name]))
	print("Transfered")
	
	outGraph = graph_util.convert_variables_to_constants(sess, sess.graph_def, ['output'])
	
	print("Exporting")
	tf.train.write_graph(outGraph, 'export', "export.pb", False)
	print("Exported")
	
	print("Testing")
	img = cv2.imread('exTest/TEST.png')
	img = cv2.cvtColor(img, cv2.COLOR_RGBA2BGR)
	img = cv2.resize(img, (CROP_SIZE, CROP_SIZE), interpolation=cv2.INTER_AREA)
	img = np.float32(img) / 255.0;
	
	fetch = {
		'out' : out
	}
	testRun = sess.run(fetch, feed_dict={ pIn: img.reshape(1, CROP_SIZE, CROP_SIZE, 3) })
	
	newImg = testRun['out'].reshape(CROP_SIZE, CROP_SIZE, 3)
	newImg = cv2.cvtColor(newImg, cv2.COLOR_RGB2BGR)
	cv2.imshow('image', newImg)
	cv2.waitKey(0)
	#cv2.imwrite('image.png', newImg)

def export():
	data = getGraphData()
	createExportGraph(data)

def main():
	tf.set_random_seed(seed)
	np.random.seed(seed)
	random.seed(seed)
	
	#TMP()
	#exit()

	#from tensorflow.python.client import device_lib

	#local_device_protos = device_lib.list_local_devices()
	#print([x.name for x in local_device_protos if x.device_type == 'GPU'])
	#exit(0)
	if EXECUTION_TYPE == "train":
		#with tf.device('/device:GPU:0'):
		createNet()
	elif EXECUTION_TYPE == "test":
		tester()
	elif EXECUTION_TYPE == "export":
		export()
  
if __name__ == "__main__":
	main()