/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.securesuite.webserver.connector;//

import android.content.Context;
import android.media.MediaScannerConnection;

import com.nuvolect.securesuite.util.OmniFile;
import com.nuvolect.securesuite.util.OmniImage;
import com.nuvolect.securesuite.util.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;


/**
 *<pre>
 *
 * rm
 *
 * Recursively removes files and directories.
 *
 * Arguments:
 *
 * cmd : rm
 * targets : (Array) array of file and directory hashes to delete
 * Response:
 *
 * removed : (Array) array of file and directory 'hashes' that were successfully deleted
 *
 * Example:( 5B == '[', 5D == ']'
 * http://hypweb.net/elFinder-nightly/demo/2.1/php/connector.minimal.php?
 * cmd=rm
 * &targets%5B%5D=l2_TmV3RmlsZS50eHQ
 * &targets%5B%5D=l2_TmV3RmlsZSBjb3B5IDEudHh0
 * &_=1459218951979
 *
 *</pre>
 */
public class CmdRm {

    // Keep list of removed files
    private static JSONArray removed;
    private static String httpIpPort;

    public static InputStream go(Context ctx, Map<String, String> params) {

        httpIpPort = params.get("url");

        ArrayList<String> targets = new ArrayList<>();

        /**
         * Params only has the first element of the targets[] array.
         * This is fine if there is only one target but an issue for multiple file operations.
         * Manually parse the query parameter strings to get all targets.
         */
        String[] qps = params.get("queryParameterStrings").split("&");

        for(String candidate : qps){

            if( candidate.contains("targets")){
                String[] parts = candidate.split("=");
                targets.add( parts[1]);
            }
        }

        removed = new JSONArray();
        boolean success = true;

        for(String target : targets ){

            OmniFile targetFile = OmniUtil.getFileFromHash(target);

            /**
             * Recursively delete files and folders adding each delete to an arraylist.
             */
            success = delete( ctx, targetFile);
            if( ! success)
                break;
        }

        JSONObject wrapper = new JSONObject();

        try {

            if( success ){
                wrapper.put("removed", removed);
            }

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Recursively deletes a directory and its contents.
     *
     * @param f The directory (or file) to delete
     * @return true if the delete succeeded, false otherwise
     */
    public static boolean delete(Context ctx, OmniFile f) {

        if (f.isDirectory()) {
            for (OmniFile child : f.listFiles()) {
                if (!delete(ctx, child)) {
                    return(false);
                }
            }
        }

        /**
         * Delete thumbnail, if there is one
         */
        OmniImage.deleteThumbnail( f );

        String hash = f.getHash();
        boolean success =f.delete();

        if( success){

            if( removed != null)
                removed.put( hash );
        }

        /**
         * The crypto storage does not use the media scanner.
         */
        if( success && f.isStd())
            MediaScannerConnection.scanFile(
                ctx,
                new String[]{f.getAbsolutePath()},
                null,
                null);

        /**
         * Note only the last success case is returned.
         */
        return(success);
    }
}
