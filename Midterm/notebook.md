
# Midterm Project Notebook

Eryue Chen (eryuec)

## Problem Statement

This project aims to classify microscopic, high-resolution images of planktons for better study in marine food webs, fisheries, ocean conservation, and more. The images need to be analyzed to assess species populations and distributions. An algorithm is designed to automate the image identification process. In this report, I will introduce the entire process of analysis, including data preparation, feature generation, data training and classification testing for this problem.

## Data Preparation

The dataset is splitted into two parts, 80% for training and 20% for testing.
The classnames can be retrieved from the data directory folder names.


```python
# get the classnames from the directory structure
directory_names = list(set(glob.glob(os.path.join("competition_data","train", "*"))\
).difference(set(glob.glob(os.path.join("competition_data","train","*.*")))))

```

To create features, the images need to be preprocessed. This includes thresholding the images to eliminate some of the backgroud, dilating the image to connect neighbor pixels and calculating the labels for connected regions. These calculations all help to reduce the noise of the images and improve the classification. The images are separated into different parts and the largest non-background region is used for retrieving the feature since it can work the best for discriminating different types of images. 

Then, the ratio of the width by length of the largest region is calculated. It will be used as a feature while applying machine learning algorithm.


```python
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
```

## Feature Selection

There are four types of features generated for the dataset. 

The first part of feature is the pixels of rescaled images. The images are rescaled to 25 x 25 pixels and the pixels are added to the feature vector. The size of rescaling is configurable for testing and accuracy tuning.

The second one is described in the previous section as the ratio of the width by length of the largest region to describe the image. It can somehow represent the shape of the image.

The third part of feature is extracted from the SIFT algorithm. SIFT can extract interesting points from the image, which can be used to identify the object with reliable recognition. These points are detectable under condition changes and invariant to uniform scaling, orientation, distortion and illumination changes. They usually lie on high-constrast regions of the image, such as edges.

The fourth part of feature is extracted from TAS (Threshold Adjacency Statistics). It was first proposed in application to classification of protein sub-cellular localization images. TAS removes the need for cropping of individual cells from images, and are an order of magnitude faster to calculate than other commonly used statistics while providing comparable or better classification accuracy, both essential requirements for application to large-scale approaches.


```python
def genFeature():
    maxPixel = 25   # Rescale the image to 25x25
    imageSize = maxPixel * maxPixel
    siftSize = 128
    pftasSize = 54
    num_rows = getTrainImagesNum() # one row for each image in the training dataset
    num_features = imageSize + 1 + siftSize + pftasSize 

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
```

## Classification Method

The random forest model is used to classify the images. Here, two parameters are used to configure the random forest. n_estimators is the number of decision trees used in the random forest and max_features is the number of random features that are used to build the decision tree. 

To validate the performance of the model, cross validation is used. First, the random forest is trained on all the available data and perform the 5-fold cross validation. Then the KFold method is used for cross validation, which splits the data into train and test sets, and a classification report. The classification report provides a list of metrics such as precision, recall to indicate the performance.


```python
def crossValidate(X, y, namesClasses):
    # n_estimators is the number of decision trees
    # max_features also known as m_try is set to the default value of the square root of the number of features
    clf = RF(n_estimators=200, n_jobs=3);
    scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
    print "Accuracy of all classes"
    print np.mean(scores)

    kf = KFold(y, n_folds=5)
    y_pred = y * 0
    for train, test in kf:
        X_train, X_test, y_train, y_test = X[train,:], X[test,:], y[train], y[test]
        clf = RF(n_estimators=200, n_jobs=3)
        clf.fit(X_train, y_train)
        y_pred[test] = clf.predict(X_test)
    print classification_report(y, y_pred, target_names=namesClasses)
```

## Test & Experiment

In the Random Forest Model, different number of decision trees are applied to achieve a better accuray.

    # of Trees           Precision           Recall           f1-score           Accuracy
    100                  0.50                0.52             0.48               0.529586286468
    200                  0.52                0.53             0.48               0.535684852481
    300                  0.52                0.53             0.49               0.543431679578 

Use different combination of features (using 200 trees).

    Feature Vector           Precision           Recall           f1-score           Accuracy
    pixel,ratio              0.44                0.47             0.41               0.467776495797
    pixel,ratio,SIFT         0.49                0.49             0.48               0.508158892369
    pixel,ratio,SIFT,TAS     0.52                0.53             0.48               0.535684852481



```python
def test(X, y, namesClasses):
    with open('testfeature', 'rb') as testfeature:
        feature_lis = [line.strip().split('\t') for line in testfeature]
    testfeature.close()

    with open('testlabel', 'rb') as testlabel:
            label_lis = [line.strip().split('\t') for line in testlabel]
    testlabel.close()

    clf = RF(n_estimators=200, n_jobs=3)
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
    print "Accuracy: "float(count) / total
```

## Difficulties & Error Analysis

The error rate of random forest depends on the correlation between any two trees in the forest and the strength of each individual tree in the forest. Increasing the correlation increases the forest error rate. Increasing the strength of the individual trees decreases the forest error rate.

The features used now covers only general cases and did not include special features which may also help to increase accuracy. Besides, some classes may have similar characteristics that cannot be separated very definitely using the features now. 

The images in different classes may be similar while images in the same classes may be very different with each other. Besides, there's some subclasses called other, which contains not uniform images in the class.

## Conclusion

The best accuracy I get is 54.34%. It used the feature vector of scaled pixels, width/length ratio, SIFT features and TAS features applying to a random forest model with 300 trees. It shows that these features all have positive effects on classification accuracy. 

## Future Work

More features for image classification can be developed and applied into the classification. The features can be improved to fit into the plankton images in the dataset in a more specific way. Besides, other classification algorithms can be experimented to see whether better accuracy can be achieved.
