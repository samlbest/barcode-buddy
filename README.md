barcode-buddy
=============

Android app for tracking product prices with the ability to look up product information by scanning barcodes.
Barcode Buddy

=====================================

This app helps users keep track of the price of past purchases of products. To make it easy to
store the products it uses a barcode scanner and looks up product information. It allows the user 
to view products that they have added. The user accounts are handled by Google’s authentication APIs 
which is built into the Android OS. The product list screen shows the last purchase price, and if 
you go into the details it shows the average price for that UPC aggregated across all users, and a 
photo if the user uploaded one.

## Products
- [App Engine][1]
- [Android][2]

## Language
- [Java][3]

## APIs
- [Google Cloud Endpoints][5]
- [Scandit Barcode Scanner SDK][6]

## Setup Instructions
The instruction below lists just some key steps.
For detailed setup instructions and documentation visit [Google App Engine developer site] (https://developers.google.com/cloud/samples/mbs).

1. Make sure you have Android SDK with Google APIs level 15 or above installed.

2. Import the project into Eclipse.

3. If you don't have Google API level 16 installed then in project properties,
   select Android and change Project Build Target to Google APIs with API Level 15 or above.

4. Update the value of `PROJECT_ID` in
   `src/com/google/cloud/backend/android/Consts.java` to the app_id of your
   deployed Mobile Backend [5]. Make sure that your Mobile Backend is configured
   with OPEN mode.

5. Run the application.

[1]: https://developers.google.com/appengine
[2]: http://developer.android.com/index.html
[3]: http://java.com/en/
[4]: https://developers.google.com/appengine/docs/java/endpoints/
[5]: https://github.com/GoogleCloudPlatform/solutions-mobile-backend-starter-java
[6]: http://www.scandit.com/

