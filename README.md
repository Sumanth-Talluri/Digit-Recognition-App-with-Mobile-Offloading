# Digit-Recognition-App-with-Mobile-Offloading

## Introduction

In this project, we developed Android Applications (Master application and client application) which are used to capture an image of handwritten digits and divide it into 4 different parts and send to the client mobiles. Then we use the client mobiles to classify the image and determine the digit. The result is then sent back to master mobile and the image will be stored in a folder with the number that is predicted.

## Application Workflow

1. User opens the app CloudIt. When the user opens the app for the first time, a pop-up requesting camera access will appear on the screen, and when the agrees, the Main Activity page will be opened.
2. In the Main Activity Page, user needs to enter the ip address and port numbers of the 4 client devices for different quadrants. Then user can click on the Capture Image button to capture the hand written digit image, 3. When the user clicks the capture image button, the Camera page will be opened where the user has an option to take a photo.
3. When the User proceeds to upload the image, a new activity, View Page Activity, will be opened where he can view the image, analyze the image and save image.
4. While Analyzing the image, it will establish a connection between the main mobile and 4 client devices.
5. In the client devices, ip address is displayed and a port number needs to be entered along with the quadrant number, and there is a button to start the server.
6. The image is splitted into 4 different parts in the main mobile and then is sent to 4 different android devices for prediction if user clicks on analyze image button.
7. Each android device has a model which is trained using the respective part of the MNIST data set images to classify the different handwritten digit images parts which are sent.
8. The predictions from each client device are sent to the main device.
9. Based on the predictions received from the various client devices, the main device will store the image in the appropriate folder based on the final predicted number if the user clicks on the save image button.

<br>

<img alt="Application Workflow Diagram" src="./Workflow Diagram.png"  width="60%" height="60%">

<br>

## Technical Details

1. The dimensions of the image part on which the model is trained are (14,14) pixels, so we had to reshape the incoming image to this pixel range.
2. Since the model is trained on the MNIST dataset which consists of images with black
background, we implemented in such a way that if the incoming image has white
background with black text, even then our models predicts it accurately. We achieved
this by flipping the pixel values of the image in case the white pixel count exceeds black
pixel count.
3. The type of Deep Learning framework we used is Convolutional Neural Network (CNN)
and we achieved the accuracies of 78.6%, 86.8%, 84.5%, 80% for top left, top right,
bottom left and bottom right part of the image.
4. The main mobile connects to four client mobiles that acts as servers.
5. The main mobile sends the splitted image to each client mobile, and the 4 client mobiles
predict the image part and send their result to the main mobile.
6. The main mobile then computes based on frequency from all the 4 responses it received
and decides the final digit as output. If it receives four different digits from the client
mobiles, then it picks the second quadrant digit as it has greater accuracy(86.8%) when
compared to other quadrants.
7. Then, the classified image is stored in the respective folders on the main mobile.

## Client Mobile

1. The Client mobile acts as server and processes the HTTP POST requests using the
socket connection for image prediction.
2. The Home Page of the Client Mobile displays the network IP address of the device,
“START SERVER” button and two Text Boxs. One is to input the Quadrant Number (1, 2,
3, 4) and the other is to input the Port number for socket connections.
3. On clicking the start server button, the client mobile will listen on the specified PORT.
4. The four model files are loaded in the “/src/main/assets/*” directory.
5. On receiving the HTTP POST request, it will pick the corresponding model file based on
the input quadrant number and return the predicted number as the response.

## Master Mobile

1. The Home Page of the Master Mobile displays the Capture Image button and four texts
boxes to input the IP address and Port(IP:Port) of the four client devices.
2. On clicking the Capture Image button, it opens the camera to capture the image.
3. The next page after capturing the image will display the captured image and two buttons, Analyze Image and Save Image.
4. On Clicking the Analyze Image button, it will split the Image into four parts and each
quadrant of the image will be sent to the corresponding client mobile for analysis.
5. After receiving the responses from the four client devices, a message will be toasted with
“Analyze Completed”.
6. On Clicking the Save Image button, the four results from the client devices will be used
to predict the final output. It uses the maximum digit frequency to finalize the predicted
digit and is toasted with the message “Predicted number - {}”. Also, the image will be
saved to the corresponding digit folder in the android device.

## Collaborators

<a href="https://github.com/praveenchiliveri6">
  Chiliveri Praveen
</a>

<a href="https://github.com/udayadara28ts">
  udayadara28ts
</a>