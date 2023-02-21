# -*- coding: utf-8 -*-
"""Untitled3.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1w7g-lPtay2kvk8Asr-Vsca_OXlhyM23-
"""

from matplotlib import pyplot as py
import numpy as np
from tensorflow.keras.datasets import mnist
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Flatten
from tensorflow.keras.optimizers import SGD
import tensorflow as tf
from tensorflow import keras,lite

def split_image(np_array):
    top_left = []
    top_right = []
    bottom_left = []
    bottom_right = []
    for index in range(np_array.shape[0]):
        arr = np.array(np_array[index])
        newarr = np.hsplit(arr, 2)
        left = np.vsplit(newarr[0],2)
        right = np.vsplit(newarr[1],2)
        top_left.append(left[0])
        bottom_left.append(left[1])
        top_right.append(right[0])
        bottom_right.append(right[1])

    top_left = np.asarray(top_left) 
    top_right = np.asarray(top_right)
    bottom_left = np.asarray(bottom_left)
    bottom_right = np.asarray(bottom_right)

    return top_left, top_right, bottom_left, bottom_right

# load train and test dataset
def load_dataset():
    (trainX, trainY), (testX, testY) = mnist.load_data()
    
    trainx = [None] * 4
    testx = [None] * 4

    trainx[0], trainx[1], trainx[2], trainx[3] = split_image(trainX)
    testx[0], testx[1], testx[2], testx[3] = split_image(testX)

    for index in range(4):
      trainx[index] = trainx[index].reshape((trainx[index].shape[0], 14, 14, 1))
      testx[index] = testx[index].reshape((testx[index].shape[0], 14, 14, 1))

    trainY = to_categorical(trainY)
    testY = to_categorical(testY)

    return trainx, trainY, testx, testY


# scale pixels
def prepare_pixels(train, test):
    train_pixels = train.astype('float32')
    test_pixels = test.astype('float32')
    train_pixels = train_pixels / 255.0
    test_pixels = test_pixels / 255.0
    return train_pixels, test_pixels


# define cnn model
def define_model():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', kernel_initializer='he_uniform', input_shape=(14, 14, 1)))
    model.add(MaxPooling2D((2, 2)))
    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(MaxPooling2D((2, 2)))
    model.add(Flatten())
    model.add(Dense(100, activation='relu', kernel_initializer='he_uniform'))
    model.add(Dense(10, activation='softmax'))
    opt = SGD(learning_rate=0.01, momentum=0.9)
    model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])
    return model


# This function is for evaluating a model
def run_test_harness():
    trainx, trainY, testx, testY = load_dataset()

    for index in range(4):
      trainX, testX = prepare_pixels(trainx[index], testx[index])
      model = define_model()
      model.fit(trainX, trainY, epochs=10, batch_size=32, verbose=0)
      model.save('final_model_{}.h5'.format(index+1))
      converter = lite.TFLiteConverter.from_keras_model(model)
      tfmodel = converter.convert()
      open("final_model_{}.tflite".format(index+1),"wb").write(tfmodel)
      _, acc = model.evaluate(testX, testY, verbose=0)
      print('> %.3f' % (acc * 100.0))


run_test_harness()

"""# New Section

# New Section
"""