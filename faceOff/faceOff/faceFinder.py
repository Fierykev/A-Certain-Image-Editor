import cv2
import dlib
import face_recognition
import face_recognition_models
import numpy as np
from umeyama import umeyama

facePredict = dlib.shape_predictor('shape_predictor_68_face_landmarks.dat')

mean_face_x = np.array([0.000213256, 0.0752622, 0.18113, 0.29077, 0.393397, 0.586856, 0.689483, 0.799124,
	0.904991, 0.98004, 0.490127, 0.490127, 0.490127, 0.490127, 0.36688, 0.426036,
	0.490127, 0.554217, 0.613373, 0.121737, 0.187122, 0.265825, 0.334606, 0.260918,
	0.182743, 0.645647, 0.714428, 0.793132, 0.858516, 0.79751, 0.719335, 0.254149,
	0.340985, 0.428858, 0.490127, 0.551395, 0.639268, 0.726104, 0.642159, 0.556721,
	0.490127, 0.423532, 0.338094, 0.290379, 0.428096, 0.490127, 0.552157, 0.689874,
	0.553364, 0.490127, 0.42689])

mean_face_y = np.array([0.106454, 0.038915, 0.0187482, 0.0344891, 0.0773906, 0.0773906, 0.0344891,
	0.0187482, 0.038915, 0.106454, 0.203352, 0.307009, 0.409805, 0.515625, 0.587326,
	0.609345, 0.628106, 0.609345, 0.587326, 0.216423, 0.178758, 0.179852, 0.231733,
	0.245099, 0.244077, 0.231733, 0.179852, 0.178758, 0.216423, 0.244077, 0.245099,
	0.780233, 0.745405, 0.727388, 0.742578, 0.727388, 0.745405, 0.780233, 0.864805,
	0.902192, 0.909281, 0.902192, 0.864805, 0.784792, 0.778746, 0.785343, 0.778746,
	0.784792, 0.824182, 0.831803, 0.824182])

landmarks_2D = np.stack([mean_face_x, mean_face_y], axis=1)

def alignFace(l):
	return umeyama(np.array([(p.x, p.y) for p in l.parts()][17:]),
					landmarks_2D,
					True)[0:2]

def trans(img, align, size):
	pad = 48
	align *= size - 2 * pad
	align[:, 2] += pad

	return cv2.warpAffine(img, align, (size, size))

def getFaces(img, size):
	locs = face_recognition.face_locations(img)

	locs_ = [dlib.rectangle(l[3], l[0], l[1], l[2]) for l in locs]
	landmarks = [facePredict(img, l) for l in locs_]
	
	faces = []

	for ((y, right, bot, x), l) in zip(locs, landmarks):
		if l == None:
			faces.append(cv2.resize(img[y : bot, x : right],
				   (size, size)))
		else:
			alignedFace = alignFace(l)

			faces.append(trans(img, alignedFace, size))

			#cv2.imshow('image', faces[-1])
			#cv2.waitKey(0)
			#cv2.destroyAllWindows()

	return faces, landmarks