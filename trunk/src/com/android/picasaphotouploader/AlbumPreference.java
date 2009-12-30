/**
 * This file is part of Picasa Photo Uploader.
 *
 * Picasa Photo Uploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picasa Photo Uploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Picasa Photo Uploader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.android.picasaphotouploader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Override of class ListPreference to show list of Picasa album
 * in preferences to select album to upload images to
 *
 * @author Jan Peter Hooiveld
 */
public class AlbumPreference extends ListPreference
{
  /**
   * Constructor
   *
   * @param context Application context
   * @param attrs Attributes
   */
  public AlbumPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  /**
   * Constructor
   *
   * @param context Application context
   */
  public AlbumPreference(Context context)
  {
    super(context);
  }

  /**
   * User clicked on album preference in user preferences
   */
  @Override
  protected void onClick()
  {
    // get user preferences and then the user email and password
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    String email            = prefs.getString("email", "").trim();
    String password         = prefs.getString("password", "").trim();

    // if no email and password are set we can't authenticate with google to
    // retrieve list of albums
    if (email.length() == 0 || password.length() == 0) {
      Utils.textDialog(getContext(), "Notification", "Set e-mail and password first.");
      return;
    }

    // check if we have internet connection to retrieve albums
    if (!CheckInternet.getInstance().canConnect(getContext(), prefs)) {
      Utils.textDialog(getContext(), "Notification", "Can't connect to internet to get Picasa albums.\n\nEither internet is down or your connection in this application is set to allow Wi-Fi only.");
      return;
    }

    // authenticate with google and get new authentication string
    GoogleAuthentication google = new GoogleAuthentication(prefs);
    String auth                 = google.getAuthenticationString();

    // if authentication string is null it means we failed authentication
    if (auth == null) {
      Utils.textDialog(getContext(), "Notification", "Google authentication failed.\n\nCheck your e-mail and password.");
      return;
    }

    // get picasa album list
    AlbumList list = new AlbumList(auth, email);

    // check if any albums were found
    if (list.fetchAlbumList() == false) {
      Utils.textDialog(getContext(), "Notification", "No Picasa albums found. Create one first.");
      return;
    }

    // call parent functions to set values for the preference that
    // user can choose
    setEntries(list.getAlbumNames());
    setEntryValues(list.getAlbumIds());

    // call parent function to show preference dialog
    showDialog(null);
  }
}

