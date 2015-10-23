import cv2
import sys
import mahotas as mh
#Import libraries for doing image analysis
from skimage.io import imread
from skimage.transform import resize
from sklearn.ensemble import RandomForestClassifier as RF
import glob
import os
from sklearn import cross_validation
from sklearn.cross_validation import StratifiedKFold as KFold
from sklearn.metrics import classification_report
from matplotlib import pyplot as plt
from matplotlib import colors
from pylab import cm
from skimage import segmentation
from skimage.morphology import watershed
from skimage import measure
from skimage import morphology
import numpy as np
import pandas as pd
from scipy import ndimage
from skimage.feature import peak_local_max
import warnings
warnings.filterwarnings("ignore")

# get the classnames from the directory structure
directory_names = list(set(glob.glob(os.path.join("competition_data","train", "*"))\
 ).difference(set(glob.glob(os.path.join("competition_data","train","*.*")))))

# Find the largest non-background region
def getLargestRegion(image):
    image = image.copy()
    # Threshold the image
    imagethres = np.where(image > np.mean(image),0.,1.0)

    # Dilate the image
    imdilated = morphology.dilation(imagethres, np.ones((4,4)))

    # Create the label list
    labelmap = measure.label(imdilated)
    labelmap = imagethres*labelmap
    labelmap = labelmap.astype(int)

    props = measure.regionprops(labelmap)
    regionmaxprop = None
    for regionprop in props:
        if sum(imagethres[labelmap == regionprop.label])*1.0/regionprop.area < 0.50:
            continue
        if regionmaxprop is None:
            regionmaxprop = regionprop
        if regionmaxprop.filled_area < regionprop.filled_area:
            regionmaxprop = regionprop
    return regionmaxprop

# Calculate the width by length ratio of the largest region
def getMinorMajorRatio(image):
    maxregion = getLargestRegion(image)

    # Handle segmentation fails
    ratio = 0.0
    if ((not maxregion is None) and  (maxregion.major_axis_length != 0.0)):
        ratio = 0.0 if maxregion is None else  maxregion.minor_axis_length*1.0 / maxregion.major_axis_length
    return ratio

def getSum(des):
    sum = [0] * 128
    if des is None:
        return sum
    for i in des:
        for j in range(len(i)):
            sum[j] += i[j]
    return sum

# Rescale the images and create the combined metrics and training labels
def getTrainImagesNum():
    #get the total training images
    numberofImages = 0
    for folder in directory_names:
        for fileNameDir in os.walk(folder):
            for fileName in fileNameDir[2]:
                 # Only read in the images
                if fileName[-4:] != ".jpg":
                    continue
                numberofImages += 1
    return numberofImages


def genFeature():
    maxPixel = 25   # Rescale the image to 25x25
    imageSize = maxPixel * maxPixel
    siftSize = 128
    pftasSize = 54
    num_rows = getTrainImagesNum() # one row for each image in the training dataset
    num_features = imageSize + 1 + siftSize + pftasSize # for our ratio

    # X is the feature vector with one row of features per image
    X = np.zeros((num_rows, num_features), dtype=float)
    # y is the numeric class label
    y = np.zeros((num_rows))

    files = []
    i = 0
    label = 0
    namesClasses = list()  #List of string of class names

    # Navigate through the list of directories
    for folder in directory_names:
        # Append the string class name for each class
        currentClass = folder.split(os.pathsep)[-1]
        namesClasses.append(currentClass)
        for fileNameDir in os.walk(folder):
            for fileName in fileNameDir[2]:
                # Only read in the images
                if fileName[-4:] != ".jpg":
                  continue

                # Read in the images and create the features
                nameFileImage = "{0}{1}{2}".format(fileNameDir[0], os.sep, fileName)
                image = imread(nameFileImage, as_grey=True)
                files.append(nameFileImage)
                axisratio = getMinorMajorRatio(image)
                image = resize(image, (maxPixel, maxPixel))

                img = cv2.imread(nameFileImage)
                gray= cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
                sift = cv2.SIFT()
                kp = sift.detect(gray,None)
                k, des = sift.detectAndCompute(gray,None)

                image2 = mh.imread(nameFileImage, as_grey=True)
                pftas = mh.features.pftas(image2)

                # Store the feature vector
                X[i, 0:imageSize] = np.reshape(image, (1, imageSize))
                X[i, imageSize] = axisratio
                X[i, imageSize + 1:imageSize + 1 + siftSize] = getSum(des)
                X[i, imageSize + 1 + siftSize:imageSize + 1 + siftSize + pftasSize] = pftas
                
                # Store the classlabel
                y[i] = label
                i += 1
        label += 1
    return X, y, namesClasses

def crossValidate(X, y, namesClasses):
    # n_estimators is the number of decision trees
    # max_features also known as m_try is set to the default value of the square root of the number of features
    clf = RF(n_estimators=300, n_jobs=3);
    scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
    print "Accuracy of all classes"
    print np.mean(scores)

    kf = KFold(y, n_folds=5)
    y_pred = y * 0
    for train, test in kf:
        X_train, X_test, y_train, y_test = X[train,:], X[test,:], y[train], y[test]
        clf = RF(n_estimators=300, n_jobs=3)
        clf.fit(X_train, y_train)
        y_pred[test] = clf.predict(X_test)
    print classification_report(y, y_pred, target_names=namesClasses)

def test(X, y, namesClasses):
    with open('test2feature', 'rb') as testfeature:
        feature_lis = [line.strip().split('\t') for line in testfeature]
    testfeature.close()

    with open('test2label', 'rb') as testlabel:
            label_lis = [line.strip().split('\t') for line in testlabel]
    testlabel.close()

    clf = RF(n_estimators=300, n_jobs=3)
    clf.fit(X, y)
    y_pred = clf.predict(feature_lis)
    count = 0
    total = 0
    print y_pred
    for i in range(len(label_lis)):
        pred = namesClasses[int(y_pred[i])].split('/')[2]
        act = label_lis[i][0].split('/')[2]
        if (pred == act):
            count = count + 1
        total = total + 1
    print float(count) / total


X, y, namesClasses = genFeature()
crossValidate(X, y, namesClasses)
test(X, y, namesClasses)