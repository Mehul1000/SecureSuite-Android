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

import com.nuvolect.securesuite.util.LogUtil;
import com.nuvolect.securesuite.util.OmniFile;
import com.nuvolect.securesuite.util.OmniFiles;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * duplicate
 *
 * Creates a copy of the directory / file.
 * Copy name is generated as follows: basedir_name_filecopy+serialnumber.extension (if any)
 *
 * Arguments:
 *
 * cmd : duplicate
 * current : hash of the directory in which to create a duplicate
 * target : hash of the directory / file that is being duplicated
 *
 * Response: Client-Server-API-2.1#open (if you copy a directory,
 * then open back up with a directory tree),
 * select - hash for the path of the duplicate.
 *
 * Example:
 * {
 * queryParameterStrings=cmd=duplicate
 * &targets%5B%5D=c0_L3RtcC9EQ0lN
 * &_=1459540733764,
 * _=1459540733764,
 * cmd=duplicate,
 * targets[]=c0_L3RtcC9EQ0lN
 * }
 * Example 2, duplicate two files
 * {
 * queryParameterStrings=cmd=duplicate
 * &targets%5B%5D=c0_L3RtcC9EQ0lNL0NhbWVyYS9JTUdfMjAxNjAxMjZfMTEyNDE0LmpwZw
 * &targets%5B%5D=c0_L3RtcC9EQ0lNL0NhbWVyYS9JTUdfMjAxNjAxMjZfMTEyNDIzLmpwZw
 * &_=1459540733766,
 * _=1459540733766,
 * cmd=duplicate,
 * targets[]=c0_L3RtcC9EQ0lNL0NhbWVyYS9JTUdfMjAxNjAxMjZfMTEyNDIzLmpwZw
 * }
 */
public class CmdDuplicate {

    private static boolean DEBUG = LogUtil.DEBUG;

    public static InputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");
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
        String suffix = "~";
        JSONArray added = new JSONArray();
        boolean success = true;

        /**
         * Iterate over the target files to be duplicated.
         * Duplicate each file and record file objects of files added.
         */
        for( int i = 0; i < targets.size(); i++){

            OmniFile fromFile = new OmniFile( targets.get(i));
            String toPath = fromFile.getPath();
            OmniFile toFile = null;
            for( int dupCount = 0; dupCount < 10; dupCount++){ // add no more than 10 tilda
                toFile = new OmniFile( fromFile.getVolumeId(), toPath);

                if( ! toFile.exists())
                    break;

                String extension = FilenameUtils.getExtension( toPath);// add ~ to filename, keep extension
                if( ! extension.isEmpty())
                    extension = "."+extension;
                toPath = FilenameUtils.removeExtension( toPath) + suffix;
                toPath = toPath + extension;
            }
            LogUtil.log(LogUtil.LogType.CMD_DUPLICATE, "file path: "+fromFile.getPath());
            LogUtil.log(LogUtil.LogType.CMD_DUPLICATE, "file exists: "+fromFile.exists());
            if( fromFile.isDirectory())
                success = OmniFiles.copyDirectory(fromFile, toFile);
            else
                success = OmniFiles.copyFile(fromFile, toFile);

            /**
             * Duplicate files have a new name, so update the modification time to present
             */
            toFile.setLastModified(System.currentTimeMillis()/1000);

            if( success )
                added.put( FileObj.makeObj(toFile, httpIpPort));// note: full depth of directory not added
            else
                LogUtil.log(LogUtil.LogType.CMD_DUPLICATE, "Duplicate failed: "+toFile.getPath());
        }

        try {
            JSONObject wrapper = new JSONObject();
            wrapper.put("added", added);

            if( ! success){

                JSONArray warning = new JSONArray();
                warning.put("errPerm");
                wrapper.put("warning", warning);
            }
            if( DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_DUPLICATE, "json result: "+wrapper.toString(2));

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
