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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * put
 *
 * Stores text in a file.
 *
 * Arguments (passed in via HTTP POST):
 *
 * cmd : edit
 * target : hash of the file
 * content : new contents of the file
 * Response: An array of successfully uploaded files if success, an error otherwise.
 *
 * changed : (Array) of files that were successfully uploaded. Information about File/Directory
 *
 * Example:
 * cmd=put&target=l3_VEVTVC50eHQ&content=ZZZZZZZZ+and+more
 */
public class CmdPut {

    public static ByteArrayInputStream go(Map<String, String> params) {


        String target="";
        if (params.containsKey("target"))
            target = params.get("target");

        String httpIpPort = params.get("url");

        String content="";
        if (params.containsKey("content"))
            content = params.get("content");

        OmniFile targetFile = new OmniFile( target );
        boolean success = targetFile.writeFile( content);

        JSONArray changed = new JSONArray();
        JSONObject wrapper = new JSONObject();
        String error = "";
        try {
            if( success)
                changed.put( targetFile.getFileObject(httpIpPort));

            wrapper.put("changed", changed);

            LogUtil.log(LogUtil.LogType.CMD_PUT, targetFile.getName()+" updated: "+success);

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            error = "UnsupportedEncodingException error";
        } catch (JSONException e) {
            e.printStackTrace();
            error = "JSONException error";
        }
        try {
            return new ByteArrayInputStream(error.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
