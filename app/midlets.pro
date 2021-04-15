#
# This ProGuard configuration file illustrates how to process J2ME midlets.
# Usage:
#     java -jar proguard.jar @midlets.pro
#

-keep public class * extends javax.microedition.midlet.MIDlet

-microedition
-overloadaggressively
-optimizationpasses 10
-defaultpackage ''
-printseeds
-printmapping
-printconfiguration