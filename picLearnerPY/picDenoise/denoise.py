import numpy as np
import cv2
import os
import sys
import random
import math
import time
import re

def sharpen(img):
	#mask = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	#mask = cv2.Canny(mask, 100, 200)
	#cv2.threshold(1.0 - mask, .9, 1.0, cv2.THRESH_BINARY_INV)
	#mask = cv2.GaussianBlur(mask, (21, 21), 11)
	
	#img = cv2.merge((b,g,r))
	
	kSize = 2
	kernel = np.ones((kSize,kSize),np.float32) / (kSize * kSize)
	img = cv2.filter2D(img,-1,kernel)
	
	img = np.float32(img) / 255.0
	sigma = 5
	mask = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	blur = cv2.GaussianBlur(mask, (0, 0), sigma, sigma)
	
	amount = 1.0
	b,g,r = cv2.split(img)
	b = b * (1 + amount) + blur * -amount
	g = g * (1 + amount) + blur * -amount
	r = r * (1 + amount) + blur * -amount
	
	img = cv2.merge((b,g,r))
	img = np.clip(img, 0, 1)
	
	return img

def contrast(img):
	alpha = 1.0
	img = img * alpha + (1 - alpha)
	img = np.clip(img, 0, 1)
	return img
	
def main():
	img = cv2.imread('test/test.png')
	
	denoise = cv2.fastNlMeansDenoisingColored(img,None,10,10,7,21)
	
	img = np.float32(img) / 255.0
	denoise = np.float32(denoise) / 255.0
	#blur = denoise = cv2.cvtColor(denoise, cv2.COLOR_BGR2GRAY)
	
	#blur = cv2.GaussianBlur(denoise, (0, 0), 1, 1)

	amount = -.5
	b,g,r = cv2.split(img)
	bD,gD,rD = cv2.split(denoise)
	b = b * (1 + amount) + bD * -amount
	g = g * (1 + amount) + gD * -amount
	r = r * (1 + amount) + rD * -amount
	
	img = cv2.merge((b,g,r))
	
	img = np.clip(img, 0, 1)
	img = np.uint8(img * 255.0)
	img = sharpen(img)
	
	img = contrast(img)
	
	cv2.imshow('mask', img)
	cv2.waitKey(0)
	cv2.destroyAllWindows()
	#cv2.imwrite('image.png', img)

if __name__ == "__main__":
	main()