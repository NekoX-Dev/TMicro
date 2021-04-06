#
# This ProGuard configuration file illustrates how to process J2ME midlets.
# Usage:
#     java -jar proguard.jar @midlets.pro
#

-microedition
-overloadaggressively
-defaultpackage ''
-allowaccessmodification

-keep public class * extends javax.microedition.midlet.MIDlet
-printseeds
-printmapping
-printconfiguration