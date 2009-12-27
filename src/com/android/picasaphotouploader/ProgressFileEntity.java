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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.FileEntity;

/**
 * Class to override FileEntity for HttpClient to progress upload monitoring
 * when uploading a file to Picasa
 *
 * @author Jan Peter Hooiveld
 */
public class ProgressFileEntity extends FileEntity
{
  /**
   * Upload notification
   */
  private UploadNotification notification;

  /**
   * Constructor
   * 
   * @param file File to user
   * @param contentType File mime-type
   * @param notification Upload notification
   */
  public ProgressFileEntity(final File file, final String contentType, UploadNotification notification)
  {
    super(file, contentType);
    
    this.notification = notification;
  }

  /**
   * Writes file to http client outputstream. We watch for progress here and
   * update upload notification by every 10 percent of increase
   *
   * @param outstream Http client outputstream
   * @throws IOException
   */
  @Override
  public void writeTo(final OutputStream outstream) throws IOException
  {
    // check if we have an outputstrean
    if (outstream == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }

    // create file input stream
    InputStream instream = new FileInputStream(this.file);
    
    try {
      // create vars
      byte[] tmp    = new byte[4096];
      int total     = (int)this.file.length();
      int progress  = 0;
      int increment = 10;
      int l;
      int percent;

      // read file and write to http output stream
      while ((l = instream.read(tmp)) != -1) {
        // check progress
        progress = progress + l;
        percent  = Math.round(((float)progress / (float)total) * 100);

        // if progress exceeds increment update status notification
        // and adjust increment
        if (percent > increment) {
          increment += 10;
          notification.update(progress);
        }

        // write to output stream
        outstream.write(tmp, 0, l);
      }

      // flush output stream
      outstream.flush();
    } finally {
      // close input stream
      instream.close();
    }
  }
}
