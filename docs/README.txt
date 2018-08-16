------------------------------- Emulator Requirements for Enabling App Map Functions ------------------------------

The emulator's default 'Location Mode' settings use the 'Device Only' GPS. Since the phone does not have a real GPS function (it can be manually set to a location), this will result in a non-functional Google Maps API.

Solution: change 'Location Mode' from 'Device Only' to 'High Accuracy'

Directions: 
1. Go to emulator phone settings
2. Click Location
3. Click Mode
4. Select High Accuracy

NOTE:
 * If using the emulator, Create an Emulator from the API 26 image.
 *   (API27's doesn't/didn't support maps; nor will 24 or before I believe).
 * Accessing Google Maps requires an API key: You can request one for free (and should!)
 *   see /res/values/google_maps_api.xml
 * More notes at the end of this file.
 * Maps Android API: Google Maps Android API v2 only supports devices with OpenGL ES 2.0 and above
 *   Check OpenGL Version via a hardware info app. If OpenGL ES 1.0, Run on API 26 (not 27!)

------------------------------- User Guide for Monitored and Remove Monitored Person From the group ------------------------------
There are two features:
When user wants to see the list of people who the user is monitoring.
when user wants to remove the monitored person from the group.

1. Sign in the app as a User
2. Click <MONITORED BY ME>.
3. User will see the list of people who the user is monitoring. Each row in list includes name of person and Email.
4. Click a row of the list.
5. User will see a list of groups that the person who is monitored by User in.Each row in list includes groupDescription.
    a. If there is no group should be shown, User will see a sentence at center of screen "Monitored user in no group"
6. A)When User Short click a row of the list of groups. User will see all members of that group. Go 7)
   B)When User Long click a row of the list of groups. The monitored person will be removed from the group that was chosen.
7. Click a row of the member list. User will see the more information(name,email and contact info of person who monitord this member)

------------------------------- User Guide for Remove Member From Group As Leader ------------------------------
There is one feature:
Remove Member From Group As Leader
1. Sign in the app as a User
2. Click <MY GROUPS>.
3. User will see a list of groups that User is leading.
4. User click a row of the group list. User will see all members in that group.
5.A)When User Short click a row of the list of members. Go 6)
  B)When User Long click a row of the list of groups. The member will be removed from the group that was chosen.
6 Click a row of the member list. User will see the more information(name,email and contact info of person who monitord this member)




-------------------------------- User Guide for Rewards ----------------------------------------
How to obtain rewards:
1. start walk with a group
2. reach the destination area
3. wait for a toast to appear indicating that the destination has been reached
4. points have been added, you can press back

Stickers:
-Stickers are found on the MainMenuActivity. They are translucent to indicate "not obtained". Once
Sufficient points have been obtained, they become opaque.

Titles:
-Titles change as total points increase. User can view acquired titles by clicking on total points.

Icon Purchases:
-Icons are customizable by purchasing and selecting in the ShopActivity.